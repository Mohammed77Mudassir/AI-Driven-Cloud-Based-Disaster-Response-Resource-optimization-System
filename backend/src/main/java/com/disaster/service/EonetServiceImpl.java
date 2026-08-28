package com.disaster.service;

import com.disaster.config.EonetProperties;
import com.disaster.dto.EonetDTO;
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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
 * NASA EONET-backed {@link EonetService}.
 *
 * <p>Fetches the public NASA EONET v3 events feed
 * ({@code https://eonet.gsfc.nasa.gov/api/v3/events}), discards closed events,
 * keeps only events inside India's bounding box, maps them into
 * {@link EonetDTO}s and caches them in memory. A {@code @Scheduled} refresh
 * runs every {@code eonet.refresh.interval} milliseconds (default 10 minutes).</p>
 *
 * <p>The cache is served stale-while-revalidate: {@code getEvents()} always
 * answers from memory and, when the data is stale or missing, hands the NASA
 * refresh to a single-thread background executor instead of blocking the HTTP
 * request. Only one refresh runs at a time. Failure handling: every NASA call
 * is retried a bounded number of times; when the feed stays unavailable the
 * failure is logged and the previously cached response is served (an empty
 * list before the first successful fetch). The application never crashes and
 * {@code GET /api/eonet/events} always answers with HTTP 200 - including an
 * empty list when NASA simply has no active events inside India.</p>
 *
 * <p>Every newly detected event also generates a system notification for every
 * active user (saved to the database and broadcast over WebSocket). Each event
 * is notified once; no SMS or email is sent automatically.</p>
 */
@Service
public class EonetServiceImpl implements EonetService {

    private static final Logger log = LoggerFactory.getLogger(EonetServiceImpl.class);

    private static final String NOTIFICATION_TITLE_PREFIX = "NASA EONET Alert: ";
    private static final String NOTIFICATION_TYPE = "ALERT";

    /** Bounded retries for a temporary NASA API failure (backoff between attempts). */
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 200L;

    /**
     * India bounding box for the EONET {@code bbox} query parameter. EONET v3
     * expects the upper-left corner (lon,lat) followed by the lower-right corner
     * (lon,lat): {@code minLon,maxLat,maxLon,minLat} - so 68.1,37.1,97.4,6.7.
     * Mirrors {@link com.disaster.geo.GeoUtils} India bounds; the local
     * {@code GeoUtils.isInsideIndia()} filter remains as a final safety net.
     */
    private static final String INDIA_BBOX = "68.1,37.1,97.4,6.7";

    private final EonetProperties properties;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /** Single-thread executor so only one EONET refresh runs at a time. */
    private final ExecutorService refreshExecutor;

    private volatile List<EonetDTO> cached = List.of();
    private volatile long lastSuccessfulFetch = 0L;
    private final Set<String> notifiedEventIds = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);
    private volatile Future<?> refreshTask;

    public EonetServiceImpl(EonetProperties properties,
                            @Qualifier("externalApiRestTemplate") RestTemplate externalApiRestTemplate,
                            NotificationService notificationService,
                            NotificationRepository notificationRepository) {
        this.properties = properties;
        this.restTemplate = externalApiRestTemplate;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.refreshExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), EonetServiceImpl::newRefreshThread);
    }

    private static Thread newRefreshThread(Runnable task) {
        Thread thread = new Thread(task, "eonet-refresh");
        thread.setDaemon(true);
        return thread;
    }

    @Override
    public List<EonetDTO> getEvents() {
        if (isStale()) {
            triggerRefresh();
        }
        return new ArrayList<>(cached);
    }

    /** Runs automatically every {@code eonet.refresh.interval} ms (default 10 minutes). */
    @Scheduled(fixedDelayString = "${eonet.refresh.interval:600000}")
    public void scheduledRefresh() {
        triggerRefresh();
    }

    /**
     * Fetches a fresh EONET feed and atomically replaces the cache. Executed on
     * the single-thread background executor (and by the {@link #refresh()}
     * interface contract) so at most one refresh can be in flight at any time.
     * Any failure is logged and the previously cached data is kept.
     */
    @Override
    public synchronized void refresh() {
        try {
            List<EonetDTO> fresh = fetchWithRetry();
            cached = fresh;
            lastSuccessfulFetch = System.currentTimeMillis();
            log.info("NASA EONET events refreshed: {} active events inside India.", fresh.size());
            notifyForNewEvents(fresh);
        } catch (RestClientException | IllegalStateException ex) {
            log.warn("NASA EONET feed unavailable ({}); serving {} cached events.", ex.getMessage(), cached.size());
        } catch (Exception ex) {
            log.error("Unexpected error refreshing NASA EONET feed; keeping last cached data.", ex);
        }
    }

    /**
     * Schedules a single background refresh and returns immediately. If a
     * refresh is already pending the call is a no-op, so concurrent requests
     * can never trigger a stampede of external EONET calls. Never blocks the
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
                log.warn("NASA EONET refresh rejected by background executor: {}", ex.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        refreshExecutor.shutdown();
    }

    private boolean isStale() {
        long ttlMs = properties.getCacheMinutes() * 60_000L;
        return cached.isEmpty() || System.currentTimeMillis() - lastSuccessfulFetch > ttlMs;
    }

    /** Fetches the feed, retrying transient failures a bounded number of times. */
    private List<EonetDTO> fetchWithRetry() {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchFromEonet();
            } catch (Exception ex) {
                lastFailure = ex;
                if (attempt < MAX_ATTEMPTS) {
                    log.debug("NASA EONET attempt {}/{} failed ({}); backing off.", attempt, MAX_ATTEMPTS, ex.getMessage());
                    sleep(attempt * RETRY_BACKOFF_MS);
                }
            }
        }
        throw new IllegalStateException(
                "NASA EONET unavailable after " + MAX_ATTEMPTS + " attempts: "
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
     * NASA EONET events URL narrowed to the India bounding box so only events
     * with a datapoint inside India are downloaded. The base URL stays
     * configurable ({@code eonet.api.url}); any pre-existing query string is
     * preserved.
     */
    private String eventsUrl() {
        String base = properties.getApiUrl();
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "bbox=" + INDIA_BBOX;
    }

    /**
     * Calls the NASA EONET v3 feed (narrowed to the India bbox), drops closed
     * events, filters to India and maps the response into DTOs sorted
     * newest-first.
     */
    @SuppressWarnings("unchecked")
    private List<EonetDTO> fetchFromEonet() {
        Map<String, Object> body = restTemplate.getForObject(eventsUrl(), Map.class);
        if (body == null) {
            throw new IllegalStateException("NASA EONET returned an empty response");
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) body.get("events");
        if (events == null) {
            throw new IllegalStateException("NASA EONET response has no event list");
        }

        List<EonetDTO> result = new ArrayList<>();
        for (Map<String, Object> event : events) {
            if (event == null) continue;
            EonetDTO dto = mapEvent(event);
            if (dto != null && GeoUtils.isInsideIndia(dto.getLatitude(), dto.getLongitude())) {
                result.add(dto);
            }
        }

        result.sort(Comparator.comparing(EonetDTO::getEventDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @SuppressWarnings("unchecked")
    private EonetDTO mapEvent(Map<String, Object> event) {
        if (event.get("closed") != null) {
            return null; // only active (open) events are processed
        }

        double[] point = resolveCoordinates((List<Map<String, Object>>) event.get("geometry"));
        if (point == null) return null;

        List<Map<String, Object>> categories = (List<Map<String, Object>>) event.get("categories");
        String category = categories == null || categories.isEmpty() ? "Other"
                : String.valueOf(categories.get(0).get("title"));

        if (isExcludedCategory(category)) {
            return null; // volcano events are excluded by project requirements
        }

        List<Map<String, Object>> sources = (List<Map<String, Object>>) event.get("sources");
        String source = sources == null || sources.isEmpty() ? EonetDTO.SOURCE
                : String.valueOf(sources.get(0).get("id"));

        Long eventDate = parseEventDate((List<Map<String, Object>>) event.get("geometry"));

        EonetDTO dto = new EonetDTO();
        dto.setEventId(String.valueOf(event.get("id")));
        dto.setTitle(event.get("title") == null ? "Unknown event" : String.valueOf(event.get("title")));
        dto.setCategory(category);
        dto.setStatus("Open");
        dto.setLatitude(point[1]);
        dto.setLongitude(point[0]);
        dto.setEventDate(eventDate);
        dto.setSource(source);
        dto.setMapsUrl(EonetDTO.buildMapsUrl(point[1], point[0]));
        return dto;
    }

    /**
     * Excluded NASA EONET categories (e.g. Volcanoes are not part of the
     * India-focused disaster monitoring scope). Case-insensitive.
     */
    private boolean isExcludedCategory(String category) {
        String c = category == null ? "" : category.toLowerCase();
        return c.contains("volcano");
    }

    /**
     * Extracts the [longitude, latitude] pair from the event geometry. EONET
     * geometry is a list of GeoJSON entries; points are used directly while
     * polygons/lines fall back to their first ring's first coordinate pair.
     */
    @SuppressWarnings("unchecked")
    private double[] resolveCoordinates(List<Map<String, Object>> geometry) {
        if (geometry == null || geometry.isEmpty()) return null;

        double[] last = null;
        for (Map<String, Object> entry : geometry) {
            if (entry == null) continue;
            Object coords = entry.get("coordinates");
            if (coords instanceof List<?> list && !list.isEmpty()) {
                double[] p = flattenFirst(list);
                if (p != null) {
                    if ("Point".equals(entry.get("type"))) {
                        return p;
                    }
                    last = p;
                }
            }
        }
        return last;
    }

    /** GeoJSON coordinates are [lon, lat]; polygons nest additional levels. */
    @SuppressWarnings("unchecked")
    private double[] flattenFirst(List<?> coords) {
        Object first = coords.get(0);
        if (first instanceof Number) {
            if (coords.size() < 2) return null;
            Double lon = asDouble(coords.get(0));
            Double lat = asDouble(coords.get(1));
            if (lon == null || lat == null) return null;
            return new double[]{lon, lat};
        }
        if (first instanceof List<?> nested) {
            return flattenFirst(nested);
        }
        return null;
    }

    /** EONET geometry entries carry the event date (ISO-8601). */
    private Long parseEventDate(List<Map<String, Object>> geometry) {
        if (geometry == null || geometry.isEmpty()) return null;
        Object raw = geometry.get(0).get("date");
        if (raw == null) return null;
        try {
            return Instant.parse(String.valueOf(raw)).toEpochMilli();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Creates one in-app notification per newly detected event. Each event is
     * only notified once (tracked in memory and against the database via the
     * unique title that embeds the EONET event id). Never sends SMS or email.
     */
    private void notifyForNewEvents(List<EonetDTO> events) {
        for (EonetDTO event : events) {
            String title = notificationTitle(event);
            if (notifiedEventIds.contains(event.getEventId())
                    || notificationRepository.existsByTitle(title)) {
                continue;
            }
            try {
                notificationService.notifyAllUsers(title, notificationMessage(event), NOTIFICATION_TYPE);
                notifiedEventIds.add(event.getEventId());
                log.info("Created notifications for NASA EONET event {} ({})",
                        event.getEventId(), event.getTitle());
            } catch (Exception ex) {
                log.warn("Failed to create notifications for NASA EONET event {}: {}",
                        event.getEventId(), ex.getMessage());
            }
        }
    }

    private String notificationTitle(EonetDTO event) {
        return NOTIFICATION_TITLE_PREFIX + String.format("%s (%s)",
                event.getTitle(), event.getEventId());
    }

    private String notificationMessage(EonetDTO event) {
        return String.format("%s event detected in India (%s). Status: %s. See the live map for details.",
                event.getCategory(), event.getTitle(), event.getStatus());
    }

    private Double asDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
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

    /** Marks the cache as stale so the next {@link #getEvents()} triggers a background refresh. Test-only hook. */
    void markCacheStale() {
        lastSuccessfulFetch = 0L;
    }
}
