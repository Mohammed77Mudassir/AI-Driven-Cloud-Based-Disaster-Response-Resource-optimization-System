package com.disaster.service;

import com.disaster.config.ApiProperties;
import com.disaster.config.FireProperties;
import com.disaster.dto.FireDTO;
import com.disaster.geo.GeoUtils;
import com.disaster.repository.NotificationRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NASA FIRMS-backed {@link FireService}.
 *
 * <p>Fetches the NASA FIRMS active-fire area CSV feed
 * ({@code https://firms.modaps.eosdis.nasa.gov/api/area/csv/{key}/{source}/{area}/{day}/{dayRange}})
 * for each configured dataset (default {@code VIIRS_SNPP_NRT} and
 * {@code MODIS_NRT}), narrows the query to the India bounding box, keeps only
 * detections inside India as a final safety net, maps them into
 * {@link FireDTO}s and caches the merged result in memory. A {@code @Scheduled}
 * refresh runs every {@code fire.refresh.interval} milliseconds (default 10
 * minutes).</p>
 *
 * <p>The cache is served stale-while-revalidate: {@code getFires()} always
 * answers from memory and, when the data is stale or missing, hands the FIRMS
 * refresh to a single-thread background executor instead of blocking the HTTP
 * request. Only one refresh runs at a time. Failure handling: every FIRMS call
 * is retried a bounded number of times; when the feed stays unavailable (or no
 * {@code NASA_FIRMS_API_KEY} is configured) the failure is logged and the
 * previously cached response is served (an empty list before the first
 * successful fetch). The application never crashes and {@code GET /api/fires}
 * always answers with HTTP 200.</p>
 *
 * <p>High-confidence detections (confidence &ge; 80) also generate a system
 * notification for every active user (saved to the database and broadcast over
 * WebSocket). Each detection is notified once; no SMS or email is sent
 * automatically.</p>
 */
@Service
public class FireServiceImpl implements FireService {

    private static final Logger log = LoggerFactory.getLogger(FireServiceImpl.class);

    private static final String NOTIFICATION_TITLE_PREFIX = "Fire Alert: ";
    private static final String NOTIFICATION_TYPE = "ALERT";

    /** Bounded retries for a temporary NASA FIRMS failure (backoff between attempts). */
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 200L;

    /**
     * India bounding box for the FIRMS {@code area} query parameter. FIRMS
     * expects {@code bb_<minLat>,<minLon>,<maxLat>,<maxLon>} - so 6.7,68.1,
     * 37.1,97.4. Mirrors {@link com.disaster.geo.GeoUtils} India bounds; the
     * local {@code GeoUtils.isInsideIndia()} filter remains as a final safety
     * net.
     */
    private static final String INDIA_AREA = "bb_6.7,68.1,37.1,97.4";

    /** Confidence threshold (0-100) above which a detection is "high confidence". */
    private static final double HIGH_CONFIDENCE_THRESHOLD = 80.0;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final FireProperties properties;
    private final ApiProperties apiProperties;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /** Single-thread executor so only one FIRMS refresh runs at a time. */
    private final ExecutorService refreshExecutor;

    private volatile List<FireDTO> cached = List.of();
    private volatile long lastSuccessfulFetch = 0L;
    private volatile boolean warnedUnconfigured = false;
    private final Set<String> notifiedFireIds = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);
    private volatile Future<?> refreshTask;

    public FireServiceImpl(FireProperties properties,
                           ApiProperties apiProperties,
                           @Qualifier("externalApiRestTemplate") RestTemplate externalApiRestTemplate,
                           NotificationService notificationService,
                           NotificationRepository notificationRepository) {
        this.properties = properties;
        this.apiProperties = apiProperties;
        this.restTemplate = externalApiRestTemplate;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.refreshExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), FireServiceImpl::newRefreshThread);
    }

    private static Thread newRefreshThread(Runnable task) {
        Thread thread = new Thread(task, "firms-refresh");
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public List<FireDTO> getFires() {
        if (isStale()) {
            triggerRefresh();
        }
        return new ArrayList<>(cached);
    }

    /** Runs automatically every {@code fire.refresh.interval} ms (default 10 minutes). */
    @Scheduled(fixedDelayString = "${fire.refresh.interval:600000}")
    public void scheduledRefresh() {
        triggerRefresh();
    }

    /**
     * Fetches fresh FIRMS detections and atomically replaces the cache.
     * Executed on the single-thread background executor (and by the
     * {@link #refresh()} interface contract) so at most one refresh can be in
     * flight at any time. Any failure is logged and the previously cached data
     * is kept. When no {@code NASA_FIRMS_API_KEY} is configured the refresh is
     * skipped (warned once) and the cache simply stays empty.
     */
    @Override
    public synchronized void refresh() {
        if (!isConfigured()) {
            if (!warnedUnconfigured) {
                log.warn("NASA FIRMS is not configured (missing NASA_FIRMS_API_KEY); serving {} cached fires.",
                        cached.size());
                warnedUnconfigured = true;
            }
            return;
        }
        try {
            List<FireDTO> fresh = fetchWithRetry();
            cached = fresh;
            lastSuccessfulFetch = System.currentTimeMillis();
            log.info("NASA FIRMS fires refreshed: {} active fires inside India.", fresh.size());
            notifyForHighConfidenceFires(fresh);
        } catch (RestClientException | IllegalStateException ex) {
            log.warn("NASA FIRMS feed unavailable ({}); serving {} cached fires.", ex.getMessage(), cached.size());
        } catch (Exception ex) {
            log.error("Unexpected error refreshing NASA FIRMS feed; keeping last cached data.", ex);
        }
    }

    /**
     * Schedules a single background refresh and returns immediately. If a
     * refresh is already pending the call is a no-op, so concurrent requests
     * can never trigger a stampede of external FIRMS calls. Never blocks the
     * calling (HTTP request) thread.
     */
    private void triggerRefresh() {
        if (refreshPending.compareAndSet(false, true)) {
            try {
                refreshTask = refreshExecutor.submit(() -> {
                    try {
                        refresh();
                    } finally {
                        refreshPending.set(false);
                    }
                });
            } catch (RejectedExecutionException ex) {
                refreshPending.set(false);
                log.warn("NASA FIRMS refresh rejected by background executor: {}", ex.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        refreshExecutor.shutdown();
    }

    private boolean isConfigured() {
        return apiProperties.getNasaFirms().isConfigured() && properties.isConfigured();
    }

    private boolean isStale() {
        long ttlMs = properties.getCacheMinutes() * 60_000L;
        return cached.isEmpty() || System.currentTimeMillis() - lastSuccessfulFetch > ttlMs;
    }

    /** Fetches the FIRMS feeds, retrying transient failures a bounded number of times. */
    private List<FireDTO> fetchWithRetry() {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchFromFirms();
            } catch (Exception ex) {
                lastFailure = ex;
                if (attempt < MAX_ATTEMPTS) {
                    log.debug("NASA FIRMS attempt {}/{} failed ({}); backing off.", attempt, MAX_ATTEMPTS, ex.getMessage());
                    sleep(attempt * RETRY_BACKOFF_MS);
                }
            }
        }
        throw new IllegalStateException(
                "NASA FIRMS unavailable after " + MAX_ATTEMPTS + " attempts: "
                        + (lastFailure == null ? "unknown error" : lastFailure.getMessage()), lastFailure);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Polls every configured FIRMS dataset, merges the parsed detections and
     * de-duplicates them (the same detection can be served by more than one
     * dataset), then sorts newest-first.
     */
    private List<FireDTO> fetchFromFirms() {
        Map<String, FireDTO> merged = new LinkedHashMap<>();
        for (String source : sources()) {
            List<FireDTO> detections = parseCsv(restTemplate.getForObject(firmsUrl(source), String.class));
            for (FireDTO fire : detections) {
                merged.putIfAbsent(fire.getFireId(), fire);
            }
        }

        List<FireDTO> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparing(FireDTO::getAcquisitionDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FireDTO::getBrightness, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * FIRMS area CSV URL for one dataset, narrowed to the India bounding box.
     * The base URL stays configurable ({@code fire.api.url}); the API key is
     * read from the shared {@link ApiProperties} holder - never hardcoded.
     */
    private String firmsUrl(String source) {
        String base = properties.getApiUrl().replaceAll("/+$", "");
        return String.format(Locale.ROOT, "%s/%s/%s/%s/%d/%d", base,
                apiProperties.getNasaFirms().getApiKey(), source, INDIA_AREA,
                properties.getDay(), properties.getDayRange());
    }

    /** FIRMS datasets to poll; blank/empty entries are ignored. */
    private List<String> sources() {
        List<String> configured = properties.getSources();
        if (configured == null || configured.isEmpty()) {
            return List.of("VIIRS_SNPP_NRT");
        }
        List<String> result = configured.stream()
                .filter(source -> source != null && !source.isBlank())
                .toList();
        return result.isEmpty() ? List.of("VIIRS_SNPP_NRT") : result;
    }

    /**
     * Parses a FIRMS area CSV body. The first line is the header; rows are
     * mapped by column name so both VIIRS ({@code bright_ti4}) and MODIS
     * ({@code brightness}) layouts work from the same parser. Malformed rows
     * and detections outside India are dropped.
     */
    private List<FireDTO> parseCsv(String csv) {
        List<FireDTO> result = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return result;
        }

        String[] lines = csv.split("\r?\n");
        if (lines.length == 0) {
            return result;
        }

        Map<String, Integer> header = new HashMap<>();
        String[] headerCells = lines[0].split(",");
        for (int i = 0; i < headerCells.length; i++) {
            header.put(headerCells[i].trim().toLowerCase(Locale.ROOT), i);
        }

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            FireDTO dto = mapRow(line.split(","), header);
            if (dto != null && GeoUtils.isInsideIndia(dto.getLatitude(), dto.getLongitude())) {
                result.add(dto);
            }
        }
        return result;
    }

    /** Maps a single CSV data row into a {@link FireDTO}; returns null when no coordinates are present. */
    private FireDTO mapRow(String[] cells, Map<String, Integer> header) {
        Double latitude = cellDouble(cells, header, "latitude");
        Double longitude = cellDouble(cells, header, "longitude");
        if (latitude == null || longitude == null) {
            return null;
        }

        Double brightness = firstNonNull(
                cellDouble(cells, header, "bright_ti4"),
                cellDouble(cells, header, "brightness"));

        String date = cell(cells, header, "acq_date");
        String time = cell(cells, header, "acq_time");
        String satellite = cell(cells, header, "satellite");
        String instrument = cell(cells, header, "instrument");

        FireDTO dto = new FireDTO();
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setBrightness(brightness);
        dto.setConfidence(cellDouble(cells, header, "confidence"));
        dto.setAcquisitionDate(parseAcquisition(date, time));
        dto.setSatellite(satellite == null || satellite.isBlank() ? "Unknown" : satellite);
        dto.setInstrument(instrument == null || instrument.isBlank() ? "Unknown" : instrument);
        dto.setFrp(cellDouble(cells, header, "frp"));
        dto.setDayNight(cell(cells, header, "daynight"));
        dto.setSource(FireDTO.SOURCE);
        dto.setMapsUrl(FireDTO.buildMapsUrl(latitude, longitude));
        dto.setFireId(fireId(dto, date, time));
        return dto;
    }

    /**
     * Stable per-detection identifier built from satellite + acquisition
     * date/time + rounded coordinates. Used for notification de-duplication.
     */
    private String fireId(FireDTO dto, String date, String time) {
        return String.format(Locale.ROOT, "firms-%s-%s-%s-%.4f-%.4f",
                dto.getSatellite(), date, time, dto.getLatitude(), dto.getLongitude());
    }

    /**
     * Combines the FIRMS acquisition date ({@code yyyy-MM-dd}) and UTC time
     * ({@code HHmm}) into epoch millis. A missing/invalid time defaults to
     * midnight UTC; a malformed row yields null so it can still be displayed
     * without a timestamp.
     */
    private Long parseAcquisition(String date, String time) {
        try {
            LocalDate day = date == null || date.isBlank()
                    ? LocalDate.ofEpochDay(0)
                    : LocalDate.parse(date.trim(), DATE_FORMAT);
            LocalTime tod = time == null || time.isBlank()
                    ? LocalTime.MIDNIGHT
                    : LocalTime.parse(time.trim(), TIME_FORMAT);
            return LocalDateTime.of(day, tod).toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String cell(String[] cells, Map<String, Integer> header, String name) {
        Integer index = header.get(name);
        if (index == null || index >= cells.length) {
            return null;
        }
        String value = cells[index];
        return value == null ? null : value.trim();
    }

    private Double cellDouble(String[] cells, Map<String, Integer> header, String name) {
        String value = cell(cells, header, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double firstNonNull(Double first, Double second) {
        return first != null ? first : second;
    }

    /**
     * Creates one in-app notification per newly detected high-confidence fire.
     * Each detection is only notified once (tracked in memory and against the
     * database via the unique title that embeds the fire id). Never sends SMS
     * or email.
     */
    private void notifyForHighConfidenceFires(List<FireDTO> fires) {
        for (FireDTO fire : fires) {
            if (!isHighConfidence(fire)) {
                continue;
            }
            String title = notificationTitle(fire);
            if (notifiedFireIds.contains(fire.getFireId())
                    || notificationRepository.existsByTitle(title)) {
                continue;
            }
            try {
                notificationService.notifyAllUsers(title, notificationMessage(fire), NOTIFICATION_TYPE);
                notifiedFireIds.add(fire.getFireId());
                log.info("Created notifications for high-confidence fire {} ({}, {})",
                        fire.getFireId(), fire.getLatitude(), fire.getLongitude());
            } catch (Exception ex) {
                log.warn("Failed to create notifications for fire {}: {}", fire.getFireId(), ex.getMessage());
            }
        }
    }

    private boolean isHighConfidence(FireDTO fire) {
        Double confidence = fire.getConfidence();
        return confidence != null && confidence >= HIGH_CONFIDENCE_THRESHOLD;
    }

    private String notificationTitle(FireDTO fire) {
        return NOTIFICATION_TITLE_PREFIX + String.format(Locale.ROOT, "Fire near %.2f, %.2f (%s)",
                fire.getLatitude(), fire.getLongitude(), fire.getFireId());
    }

    private String notificationMessage(FireDTO fire) {
        return String.format(Locale.ROOT,
                "High-confidence fire detected in India at (%.2f, %.2f). Brightness %.1f K, FRP %.1f MW (%s / %s). "
                        + "See the live map for details.",
                fire.getLatitude(), fire.getLongitude(),
                fire.getBrightness() == null ? 0.0 : fire.getBrightness(),
                fire.getFrp() == null ? 0.0 : fire.getFrp(),
                fire.getSatellite(), fire.getInstrument());
    }

    /**
     * Blocks until the most recently triggered background refresh completes.
     * Test-only hook so assertions on the refresh outcome are deterministic.
     */
    void awaitBackgroundRefresh() throws Exception {
        Future<?> task = refreshTask;
        if (task != null) {
            task.get(5, TimeUnit.SECONDS);
        }
    }

    /** Marks the cache as stale so the next {@link #getFires()} triggers a background refresh. Test-only hook. */
    void markCacheStale() {
        lastSuccessfulFetch = 0L;
    }
}
