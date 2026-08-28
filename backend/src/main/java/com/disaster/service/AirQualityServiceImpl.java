package com.disaster.service;

import com.disaster.config.AirQualityProperties;
import com.disaster.config.ApiProperties;
import com.disaster.dto.AirQualityDTO;
import com.disaster.geo.GeoUtils;
import com.disaster.repository.NotificationRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
 * OpenAQ-backed {@link AirQualityService}.
 *
 * <p>Fetches the OpenAQ v2 latest-measurements feed
 * ({@code https://api.openaq.org/v2/latest}) narrowed to India
 * ({@code country=IN}), keeps only stations inside India's bounding box as a
 * final safety net, maps their latest pollutant readings (PM2.5, PM10, NO2,
 * O3, CO) into {@link AirQualityDTO}s and caches the result in memory. A
 * {@code @Scheduled} refresh runs every {@code air-quality.refresh.interval}
 * milliseconds (default 10 minutes). The {@code OPENAQ_API_KEY} is sent as the
 * {@code X-API-Key} header - it is never part of the URL, the response or the
 * logs.</p>
 *
 * <p>The cache is served stale-while-revalidate: {@code getAirQuality()} always
 * answers from memory and, when the data is stale or missing, hands the OpenAQ
 * refresh to a single-thread background executor instead of blocking the HTTP
 * request. Only one refresh runs at a time. Failure handling: every OpenAQ call
 * is retried a bounded number of times; when the feed stays unavailable (or no
 * {@code OPENAQ_API_KEY} is configured) the failure is logged and the previously
 * cached response is served (an empty list before the first successful fetch).
 * The application never crashes and {@code GET /api/air-quality} always answers
 * with HTTP 200.</p>
 *
 * <p>Hazardous readings (PM2.5 &ge; 121, i.e. "Very Poor" or worse on the
 * CPCB/NAQI scale) also generate a system notification for every active user
 * (saved to the database and broadcast over WebSocket). Each station is notified
 * once; no SMS or email is sent automatically.</p>
 */
@Service
public class AirQualityServiceImpl implements AirQualityService {

    private static final Logger log = LoggerFactory.getLogger(AirQualityServiceImpl.class);

    private static final String NOTIFICATION_TITLE_PREFIX = "Air Quality Alert: ";
    private static final String NOTIFICATION_TYPE = "ALERT";

    /** Bounded retries for a temporary OpenAQ failure (backoff between attempts). */
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 200L;

    /** OpenAQ results requested per page; the v2 API caps this at 1000. */
    private static final int MAX_RESULTS = 1000;

    /**
     * PM2.5 (ug/m3) threshold above which air quality is considered hazardous
     * (CPCB/NAQI category "Very Poor" starts at 121).
     */
    private static final double HAZARDOUS_PM25_THRESHOLD = 121.0;

    private final AirQualityProperties properties;
    private final ApiProperties apiProperties;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /** Single-thread executor so only one OpenAQ refresh runs at a time. */
    private final ExecutorService refreshExecutor;

    private volatile List<AirQualityDTO> cached = List.of();
    private volatile long lastSuccessfulFetch = 0L;
    private volatile boolean warnedUnconfigured = false;
    private final Set<String> notifiedStationIds = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);
    private volatile Future<?> refreshTask;

    public AirQualityServiceImpl(AirQualityProperties properties,
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
                new LinkedBlockingQueue<>(1), AirQualityServiceImpl::newRefreshThread);
    }

    private static Thread newRefreshThread(Runnable task) {
        Thread thread = new Thread(task, "openaq-refresh");
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public List<AirQualityDTO> getAirQuality() {
        if (isStale()) {
            triggerRefresh();
        }
        return new ArrayList<>(cached);
    }

    /** Runs automatically every {@code air-quality.refresh.interval} ms (default 10 minutes). */
    @Scheduled(fixedDelayString = "${air-quality.refresh.interval:600000}")
    public void scheduledRefresh() {
        triggerRefresh();
    }

    /**
     * Fetches fresh OpenAQ readings and atomically replaces the cache. Executed
     * on the single-thread background executor (and by the {@link #refresh()}
     * interface contract) so at most one refresh can be in flight at any time.
     * Any failure is logged and the previously cached data is kept. When no
     * {@code OPENAQ_API_KEY} is configured the refresh is skipped (warned once)
     * and the cache simply stays empty.
     */
    @Override
    public synchronized void refresh() {
        if (!isConfigured()) {
            if (!warnedUnconfigured) {
                log.warn("OpenAQ air quality is not configured (missing OPENAQ_API_KEY); serving {} cached stations.",
                        cached.size());
                warnedUnconfigured = true;
            }
            return;
        }
        try {
            List<AirQualityDTO> fresh = fetchWithRetry();
            cached = fresh;
            lastSuccessfulFetch = System.currentTimeMillis();
            log.info("OpenAQ air quality refreshed: {} stations inside India.", fresh.size());
            notifyForHazardousAirQuality(fresh);
        } catch (RestClientException | IllegalStateException ex) {
            log.warn("OpenAQ feed unavailable ({}); serving {} cached stations.", ex.getMessage(), cached.size());
        } catch (Exception ex) {
            log.error("Unexpected error refreshing OpenAQ feed; keeping last cached data.", ex);
        }
    }

    /**
     * Schedules a single background refresh and returns immediately. If a
     * refresh is already pending the call is a no-op, so concurrent requests
     * can never trigger a stampede of external OpenAQ calls. Never blocks the
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
                log.warn("OpenAQ refresh rejected by background executor: {}", ex.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        refreshExecutor.shutdown();
    }

    private boolean isConfigured() {
        return apiProperties.getOpenaq().isConfigured() && properties.isConfigured();
    }

    private boolean isStale() {
        long ttlMs = properties.getCacheMinutes() * 60_000L;
        return cached.isEmpty() || System.currentTimeMillis() - lastSuccessfulFetch > ttlMs;
    }

    /** Fetches the OpenAQ feed, retrying transient failures a bounded number of times. */
    private List<AirQualityDTO> fetchWithRetry() {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchFromOpenAq();
            } catch (Exception ex) {
                lastFailure = ex;
                if (attempt < MAX_ATTEMPTS) {
                    log.debug("OpenAQ attempt {}/{} failed ({}); backing off.", attempt, MAX_ATTEMPTS, ex.getMessage());
                    sleep(attempt * RETRY_BACKOFF_MS);
                }
            }
        }
        throw new IllegalStateException(
                "OpenAQ unavailable after " + MAX_ATTEMPTS + " attempts: "
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
     * OpenAQ v2 latest-measurements URL narrowed to India. The base URL stays
     * configurable ({@code air-quality.api.url}); the API key is never placed
     * in the query string - it travels in the {@code X-API-Key} request header.
     */
    private String latestUrl() {
        String base = properties.getApiUrl().replaceAll("/+$", "");
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "country=IN&limit=" + MAX_RESULTS;
    }

    /**
     * Calls the OpenAQ v2 latest feed (narrowed to India), keeps only stations
     * inside India and maps the response into DTOs sorted by AQI descending
     * (most hazardous first).
     */
    @SuppressWarnings("unchecked")
    private List<AirQualityDTO> fetchFromOpenAq() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiProperties.getOpenaq().getApiKey());
        ResponseEntity<Map> response = restTemplate.exchange(
                latestUrl(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("OpenAQ returned an empty response");
        }

        Object resultsRaw = body.get("results");
        if (!(resultsRaw instanceof List<?> results)) {
            throw new IllegalStateException("OpenAQ response has no result list");
        }

        List<AirQualityDTO> result = new ArrayList<>();
        for (Object item : results) {
            if (!(item instanceof Map<?, ?>)) {
                continue;
            }
            AirQualityDTO dto = mapLocation((Map<String, Object>) item);
            if (dto != null) {
                result.add(dto);
            }
        }

        result.sort(Comparator.comparing(AirQualityDTO::getAqi,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * Maps a single OpenAQ location result into an {@link AirQualityDTO};
     * returns null when the location has no usable coordinates or lies outside
     * India. Each pollutant is read from the location's latest measurements by
     * parameter code; the timestamp is the newest measurement update.
     */
    @SuppressWarnings("unchecked")
    private AirQualityDTO mapLocation(Map<String, Object> location) {
        Map<String, Object> coordinates = (Map<String, Object>) location.get("coordinates");
        if (coordinates == null) {
            return null;
        }
        Double latitude = asDouble(coordinates.get("latitude"));
        Double longitude = asDouble(coordinates.get("longitude"));
        if (latitude == null || longitude == null
                || !GeoUtils.isInsideIndia(latitude, longitude)) {
            return null;
        }

        String stationName = location.get("location") == null
                ? "Unknown station"
                : String.valueOf(location.get("location"));

        AirQualityDTO dto = new AirQualityDTO();
        dto.setStationName(stationName);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setSource(AirQualityDTO.SOURCE);
        dto.setMapsUrl(AirQualityDTO.buildMapsUrl(latitude, longitude));

        Object measurementsRaw = location.get("measurements");
        Long newest = null;
        if (measurementsRaw instanceof List<?> measurements) {
            for (Object m : measurements) {
                if (!(m instanceof Map<?, ?>)) {
                    continue;
                }
                Map<String, Object> measurement = (Map<String, Object>) m;
                String parameter = measurement.get("parameter") == null
                        ? ""
                        : String.valueOf(measurement.get("parameter")).toLowerCase(Locale.ROOT);
                Double value = asDouble(measurement.get("value"));
                switch (parameter) {
                    case "pm25" -> dto.setPm25(value);
                    case "pm10" -> dto.setPm10(value);
                    case "no2" -> dto.setNo2(value);
                    case "o3" -> dto.setO3(value);
                    case "co" -> dto.setCo(value);
                    default -> { }
                }
                Long ts = parseTimestamp(measurement.get("lastUpdated"));
                if (ts != null && (newest == null || ts > newest)) {
                    newest = ts;
                }
            }
        }

        dto.setAqi(AirQualityDTO.aqi(dto.getPm25()));
        dto.setCategory(AirQualityDTO.aqiCategory(dto.getPm25()));
        dto.setTimestamp(newest);
        return dto;
    }

    private Double asDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    /** OpenAQ timestamps are ISO-8601 (UTC or with an offset); a malformed value yields null. */
    private Long parseTimestamp(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text).toEpochMilli();
        } catch (DateTimeParseException ex) {
            try {
                return OffsetDateTime.parse(text).toInstant().toEpochMilli();
            } catch (DateTimeParseException ex2) {
                return null;
            }
        }
    }

    /**
     * Creates one in-app notification per newly detected hazardous station.
     * Each station is only notified once (tracked in memory and against the
     * database via the unique title that embeds the station id). Never sends
     * SMS or email.
     */
    private void notifyForHazardousAirQuality(List<AirQualityDTO> stations) {
        for (AirQualityDTO station : stations) {
            if (!isHazardous(station)) {
                continue;
            }
            String title = notificationTitle(station);
            if (notifiedStationIds.contains(stationId(station))
                    || notificationRepository.existsByTitle(title)) {
                continue;
            }
            try {
                notificationService.notifyAllUsers(title, notificationMessage(station), NOTIFICATION_TYPE);
                notifiedStationIds.add(stationId(station));
                log.info("Created notifications for hazardous air quality at {} ({})",
                        station.getStationName(), stationId(station));
            } catch (Exception ex) {
                log.warn("Failed to create notifications for air quality at {}: {}",
                        station.getStationName(), ex.getMessage());
            }
        }
    }

    private boolean isHazardous(AirQualityDTO station) {
        Double pm25 = station.getPm25();
        return pm25 != null && pm25 >= HAZARDOUS_PM25_THRESHOLD;
    }

    private String stationId(AirQualityDTO station) {
        return String.format(Locale.ROOT, "openaq-%s-%.4f-%.4f",
                station.getStationName(), station.getLatitude(), station.getLongitude());
    }

    private String notificationTitle(AirQualityDTO station) {
        return NOTIFICATION_TITLE_PREFIX + String.format("%s (%s)",
                station.getStationName(), stationId(station));
    }

    private String notificationMessage(AirQualityDTO station) {
        return String.format("Air quality in %s (India) is %s (PM2.5 %.1f ug/m3, AQI %d). "
                        + "Limit outdoor exposure. See the live map for details.",
                station.getStationName(), station.getCategory(),
                station.getPm25() == null ? 0.0 : station.getPm25(),
                station.getAqi() == null ? 0 : station.getAqi());
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

    /** Marks the cache as stale so the next {@link #getAirQuality()} triggers a background refresh. Test-only hook. */
    void markCacheStale() {
        lastSuccessfulFetch = 0L;
    }
}
