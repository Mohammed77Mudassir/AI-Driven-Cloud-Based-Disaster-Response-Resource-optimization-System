package com.disaster.service;

import com.disaster.config.EarthquakeProperties;
import com.disaster.dto.EarthquakeDTO;
import com.disaster.geo.GeoUtils;
import com.disaster.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * USGS-backed {@link EarthquakeService}.
 *
 * <p>Fetches the official USGS GeoJSON feed
 * ({@code https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson}),
 * keeps only events inside India's bounding box, maps them into
 * {@link EarthquakeDTO}s and caches them in memory. A {@code @Scheduled}
 * refresh runs every {@code earthquake.refresh.interval} milliseconds (default
 * 5 minutes).</p>
 *
 * <p>Failure handling: every USGS call is wrapped so an unavailable feed is
 * logged and the previously cached response is served. The application never
 * crashes and the {@code GET /api/earthquakes} endpoint always answers.</p>
 *
 * <p>High / Critical events also generate a system notification for every
 * active user (saved to the database and broadcast over WebSocket). No SMS or
 * email is sent automatically.</p>
 */
@Service
public class EarthquakeServiceImpl implements EarthquakeService {

    private static final Logger log = LoggerFactory.getLogger(EarthquakeServiceImpl.class);

    private static final String NOTIFICATION_TITLE_PREFIX = "Earthquake Alert: ";
    private static final String NOTIFICATION_TYPE = "ALERT";

    private final EarthquakeProperties properties;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    private volatile List<EarthquakeDTO> cached = List.of();
    private volatile long lastSuccessfulFetch = 0L;
    private final Set<String> notifiedEventIds = ConcurrentHashMap.newKeySet();

    public EarthquakeServiceImpl(EarthquakeProperties properties,
                                 @Qualifier("externalApiRestTemplate") RestTemplate externalApiRestTemplate,
                                 NotificationService notificationService,
                                 NotificationRepository notificationRepository) {
        this.properties = properties;
        this.restTemplate = externalApiRestTemplate;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<EarthquakeDTO> getEarthquakes() {
        if (isStale()) {
            refresh();
        }
        return new ArrayList<>(cached);
    }

    /** Runs automatically every {@code earthquake.refresh.interval} ms (default 5 minutes). */
    @Scheduled(fixedDelayString = "${earthquake.refresh.interval:300000}")
    @Override
    public synchronized void refresh() {
        try {
            List<EarthquakeDTO> fresh = fetchFromUsgs();
            cached = fresh;
            lastSuccessfulFetch = System.currentTimeMillis();
            log.info("Earthquake feed refreshed: {} events inside India.", fresh.size());
            notifyForHighRiskEvents(fresh);
        } catch (RestClientException | IllegalStateException | ClassCastException ex) {
            log.warn("USGS earthquake feed unavailable ({}); serving {} cached events.", ex.getMessage(), cached.size());
        } catch (Exception ex) {
            log.error("Unexpected error refreshing earthquake feed; keeping last cached data.", ex);
        }
    }

    private boolean isStale() {
        long ttlMs = properties.getCacheMinutes() * 60_000L;
        return cached.isEmpty() || System.currentTimeMillis() - lastSuccessfulFetch > ttlMs;
    }

    /**
     * Calls the USGS feed, filters to India and maps the response into DTOs
     * sorted newest-first.
     */
    @SuppressWarnings("unchecked")
    private List<EarthquakeDTO> fetchFromUsgs() {
        Map<String, Object> body = restTemplate.getForObject(properties.getApiUrl(), Map.class);
        if (body == null) {
            throw new IllegalStateException("USGS returned an empty response");
        }

        List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");
        if (features == null) {
            throw new IllegalStateException("USGS response has no feature list");
        }

        List<EarthquakeDTO> result = new ArrayList<>();
        for (Map<String, Object> feature : features) {
            if (feature == null) continue;
            EarthquakeDTO dto = mapFeature(feature);
            if (dto != null && GeoUtils.isInsideIndia(dto.getLatitude(), dto.getLongitude())) {
                result.add(dto);
            }
        }

        result.sort(Comparator.comparing(EarthquakeDTO::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @SuppressWarnings("unchecked")
    private EarthquakeDTO mapFeature(Map<String, Object> feature) {
        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
        if (geometry == null) return null;

        List<Object> coordinates = (List<Object>) geometry.get("coordinates");
        if (coordinates == null || coordinates.size() < 2) return null;

        Double longitude = asDouble(coordinates.get(0));
        Double latitude = asDouble(coordinates.get(1));
        if (longitude == null || latitude == null) return null;

        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
        if (properties == null) return null;

        Double magnitude = asDouble(properties.get("mag"));
        if (magnitude == null) return null;

        Double depth = coordinates.size() > 2 ? asDouble(coordinates.get(2)) : null;
        Long time = asLong(properties.get("time"));

        EarthquakeDTO dto = new EarthquakeDTO();
        dto.setId(String.valueOf(feature.get("id")));
        dto.setMagnitude(magnitude);
        dto.setLocation(properties.get("place") == null ? "Unknown location" : String.valueOf(properties.get("place")));
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setDepth(depth == null ? 0.0 : depth);
        dto.setTime(time == null ? 0L : time);
        dto.setRiskLevel(EarthquakeDTO.determineRiskLevel(magnitude));
        dto.setSource(EarthquakeDTO.SOURCE);
        dto.setMapsUrl(EarthquakeDTO.buildMapsUrl(latitude, longitude));
        return dto;
    }

    /**
     * Creates one in-app notification per high/critical event per user. Each
     * event is only notified once (tracked in memory and against the database
     * via the unique title that embeds the USGS event id). Never sends SMS or
     * email.
     */
    private void notifyForHighRiskEvents(List<EarthquakeDTO> events) {
        for (EarthquakeDTO event : events) {
            if (!"High".equals(event.getRiskLevel()) && !"Critical".equals(event.getRiskLevel())) {
                continue;
            }
            String title = notificationTitle(event);
            if (notifiedEventIds.contains(event.getId())
                    || notificationRepository.existsByTitle(title)) {
                continue;
            }
            try {
                notificationService.notifyAllUsers(title, notificationMessage(event), NOTIFICATION_TYPE);
                notifiedEventIds.add(event.getId());
                log.info("Created notifications for {} earthquake {} ({})",
                        event.getRiskLevel(), event.getId(), event.getLocation());
            } catch (Exception ex) {
                log.warn("Failed to create notifications for earthquake {}: {}",
                        event.getId(), ex.getMessage());
            }
        }
    }

    private String notificationTitle(EarthquakeDTO event) {
        return NOTIFICATION_TITLE_PREFIX + String.format("M%s %s (%s)",
                event.getMagnitude(), event.getLocation(), event.getId());
    }

    private String notificationMessage(EarthquakeDTO event) {
        return String.format("%s-risk earthquake detected in India. Magnitude %s at %s. "
                        + "Depth %.1f km. See the live map for details.",
                event.getRiskLevel(), event.getMagnitude(), event.getLocation(), event.getDepth());
    }

    private Double asDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
