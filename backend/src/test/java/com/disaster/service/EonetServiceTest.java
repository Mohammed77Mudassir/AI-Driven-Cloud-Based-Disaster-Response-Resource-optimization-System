package com.disaster.service;

import com.disaster.config.EonetProperties;
import com.disaster.dto.EonetDTO;
import com.disaster.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the NASA EONET module. No network access: every test
 * exercises the active-event filter, the India filter, response mapping, the
 * in-memory cache fallback, the stale-while-revalidate background refresh, the
 * bounded retry logic and the notification integration using a mocked
 * {@link RestTemplate}.
 */
class EonetServiceTest {

    private EonetProperties properties;
    private RestTemplate restTemplate;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private EonetServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new EonetProperties();
        restTemplate = mock(RestTemplate.class);
        notificationService = mock(NotificationService.class);
        notificationRepository = mock(NotificationRepository.class);
        service = new EonetServiceImpl(properties, restTemplate,
                notificationService, notificationRepository);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    /** Expected external URL: base feed narrowed to the India bounding box. */
    private String bboxUrl() {
        return properties.getApiUrl() + "?bbox=68.1,37.1,97.4,6.7";
    }

    private Map<String, Object> event(String id, String title, String category, double lat, double lon,
                                      String date, Object closed) {
        Map<String, Object> categoryMap = new LinkedHashMap<>();
        categoryMap.put("id", category.toLowerCase().replace(' ', '-'));
        categoryMap.put("title", category);

        Map<String, Object> sourceMap = new LinkedHashMap<>();
        sourceMap.put("id", "GDACS");

        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Point");
        geometry.put("date", date);
        geometry.put("coordinates", List.of(lon, lat));

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", id);
        event.put("title", title);
        event.put("categories", List.of(categoryMap));
        event.put("sources", List.of(sourceMap));
        event.put("geometry", List.of(geometry));
        event.put("closed", closed);
        return event;
    }

    private Map<String, Object> feed(Map<String, Object>... events) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("events", List.of(events));
        return body;
    }

    // ------------------------------------------------------------------

    @Test
    void filtersFeedToIndiaOnly() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-in", "Storm near Mumbai", "Severe Storms", 19.0760, 72.8777,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-ca", "Wildfire in California", "Wildfires", 36.7783, -119.4179,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-jp", "Typhoon near Tokyo", "Severe Storms", 35.6762, 139.6503,
                                "2024-01-01T00:00:00Z", null)));

        service.refresh();

        List<EonetDTO> result = service.getEvents();
        assertEquals(1, result.size(), "only events inside the India bounding box may be returned");
        assertEquals("eonet-in", result.get(0).getEventId());
    }

    @Test
    void externalRequestUsesIndiaBbox() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(feed(
                event("eonet-bbox", "Storm in India", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));

        service.refresh();

        verify(restTemplate).getForObject(contains("?bbox=68.1,37.1,97.4,6.7"), eq(Map.class));
        assertEquals(1, service.getEvents().size(), "events returned by the bbox request must still be parsed");
        assertEquals("eonet-bbox", service.getEvents().get(0).getEventId());
    }

    @Test
    void discardsVolcanoEvents() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-volcano", "Volcano near Andaman", "Volcanoes", 12.9, 92.9,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-storm", "Storm near Mumbai", "Severe Storms", 19.0760, 72.8777,
                                "2024-01-01T00:00:00Z", null)));

        service.refresh();

        List<EonetDTO> result = service.getEvents();
        assertEquals(1, result.size(), "volcano events must never be returned");
        assertEquals("eonet-storm", result.get(0).getEventId());
    }

    @Test
    void discardsClosedEvents() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-open", "Storm near Mumbai", "Severe Storms", 19.0760, 72.8777,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-closed", "Flood near Delhi", "Floods", 28.6139, 77.2090,
                                "2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z")));

        service.refresh();

        List<EonetDTO> result = service.getEvents();
        assertEquals(1, result.size(), "closed events must never be returned");
        assertEquals("eonet-open", result.get(0).getEventId());
    }

    @Test
    void mapsEventFieldsCorrectly() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("EONET_5634", "Severe storm near Chennai", "Severe Storms",
                        13.0827, 80.2707, "2024-01-01T00:00:00Z", null)));

        service.refresh();

        EonetDTO dto = service.getEvents().get(0);
        assertEquals("EONET_5634", dto.getEventId());
        assertEquals("Severe storm near Chennai", dto.getTitle());
        assertEquals("Severe Storms", dto.getCategory());
        assertEquals("Open", dto.getStatus());
        assertEquals(13.0827, dto.getLatitude());
        assertEquals(80.2707, dto.getLongitude());
        assertEquals(1704067200000L, dto.getEventDate());
        assertEquals("GDACS", dto.getSource());
        assertTrue(dto.getMapsUrl().contains("13.0827"));
        assertTrue(dto.getMapsUrl().contains("80.2707"));
    }

    @Test
    void defaultsSourceAndCategoryWhenMissing() {
        Map<String, Object> raw = event("EONET_9", "Unknown event", "Other", 20.0, 78.0,
                "2024-01-01T00:00:00Z", null);
        raw.remove("sources");
        raw.remove("categories");

        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(raw));

        service.refresh();

        EonetDTO dto = service.getEvents().get(0);
        assertEquals("Other", dto.getCategory());
        assertEquals(EonetDTO.SOURCE, dto.getSource());
    }

    @Test
    void sortsNewestFirst() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-old", "Old storm", "Severe Storms", 20.0, 78.0,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-new", "New storm", "Severe Storms", 20.0, 78.0,
                                "2024-01-02T00:00:00Z", null)));

        service.refresh();

        List<EonetDTO> result = service.getEvents();
        assertEquals("eonet-new", result.get(0).getEventId());
    }

    @Test
    void skipsEventsWithoutGeometry() {
        Map<String, Object> noGeom = event("eonet-nogeom", "No geometry", "Other", 20.0, 78.0,
                "2024-01-01T00:00:00Z", null);
        noGeom.put("geometry", null);

        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-ok", "OK", "Other", 20.0, 78.0, "2024-01-01T00:00:00Z", null),
                        noGeom));

        service.refresh();

        assertEquals(1, service.getEvents().size());
        assertEquals("eonet-ok", service.getEvents().get(0).getEventId());
    }

    @Test
    void apiFailureKeepsServingLastCachedData() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-cached", "Cached storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));
        service.refresh();
        assertFalse(service.getEvents().isEmpty());

        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenThrow(new ResourceAccessException("NASA EONET down"));

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getEvents().size(), "cached data must survive an API outage");
        assertEquals("eonet-cached", service.getEvents().get(0).getEventId());
    }

    @Test
    void apiFailureWithEmptyCacheReturnsEmptyListInsteadOfCrashing() throws Exception {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenThrow(new ResourceAccessException("NASA EONET down"));

        assertDoesNotThrow(() -> service.getEvents());
        assertTrue(service.getEvents().isEmpty());
        assertDoesNotThrow(service::awaitBackgroundRefresh);
        assertTrue(service.getEvents().isEmpty(), "a failed background refresh must keep the empty cache");
    }

    @Test
    void retriesThenSucceedsOnTransientFailure() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenThrow(new ResourceAccessException("temporary hiccup"))
                .thenReturn(feed(event("eonet-recovered", "Recovered storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));

        service.refresh();

        List<EonetDTO> result = service.getEvents();
        assertEquals(1, result.size(), "a transient failure must be retried transparently");
        assertEquals("eonet-recovered", result.get(0).getEventId());
        verify(restTemplate, times(2)).getForObject(bboxUrl(), Map.class);
    }

    @Test
    void malformedResponseIsHandledGracefully() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(null);

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getEvents().isEmpty());
    }

    @Test
    void emptyEventListIsAcceptedAndReturnsEmptyResult() {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed());

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getEvents().isEmpty(), "no events inside India is not an error");
    }

    @Test
    void createsNotificationsForEveryNewEvent() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(
                        event("eonet-1", "Storm near Mumbai", "Severe Storms", 19.0760, 72.8777,
                                "2024-01-01T00:00:00Z", null),
                        event("eonet-2", "Flood near Assam", "Floods", 26.2000, 92.5000,
                                "2024-01-01T00:00:00Z", null)));

        service.refresh();

        verify(notificationService, times(2)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void doesNotNotifyTheSameEventTwice() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-1", "Storm near Mumbai", "Severe Storms",
                        19.0760, 72.8777, "2024-01-01T00:00:00Z", null)));

        service.refresh();
        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void failedNotificationCreationDoesNotBreakRefresh() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-1", "Storm near Mumbai", "Severe Storms",
                        19.0760, 72.8777, "2024-01-01T00:00:00Z", null)));
        doThrow(new RuntimeException("db down"))
                .when(notificationService).notifyAllUsers(anyString(), anyString(), anyString());

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getEvents().size());
    }

    @Test
    void getEventsWithEmptyCacheReturnsEmptyImmediatelyAndRefreshesInBackground() throws Exception {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-lazy", "Lazy storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));

        List<EonetDTO> result = service.getEvents();

        assertTrue(result.isEmpty(), "an empty cache must return immediately without waiting on NASA");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getEvents().size(), "the background refresh must eventually populate the cache");
        verify(restTemplate, times(1)).getForObject(bboxUrl(), Map.class);
    }

    @Test
    void getEventsWithWarmCacheReturnsCachedDataImmediately() throws Exception {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-warm", "Warm storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));
        service.refresh();

        List<EonetDTO> result = service.getEvents();

        assertEquals(1, result.size(), "a warm cache must be served without re-fetching");
        assertEquals("eonet-warm", result.get(0).getEventId());
        verify(restTemplate, times(1)).getForObject(bboxUrl(), Map.class);
    }

    @Test
    void staleCacheReturnsCachedDataWithoutSynchronousExternalCall() throws Exception {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-cached", "Cached storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));
        service.refresh();
        assertEquals(1, service.getEvents().size());
        service.markCacheStale();

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.getForObject(bboxUrl(), Map.class)).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return feed(event("eonet-fresh", "Fresh storm", "Severe Storms", 20.0, 78.0,
                    "2024-01-01T00:00:00Z", null));
        });

        long start = System.nanoTime();
        List<EonetDTO> result = service.getEvents();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(1, result.size(), "a stale cache must still be served from memory");
        assertEquals("eonet-cached", result.get(0).getEventId());
        assertTrue(elapsedMs < 1000, "getEvents() must not block on the external EONET API");
        assertTrue(entered.await(2, TimeUnit.SECONDS), "a background refresh must have been triggered");

        release.countDown();
        service.awaitBackgroundRefresh();
        assertEquals("eonet-fresh", service.getEvents().get(0).getEventId(),
                "the background refresh must replace the stale cache once it completes");
    }

    @Test
    void failedBackgroundRefreshKeepsExistingCache() throws Exception {
        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenReturn(feed(event("eonet-cached", "Cached storm", "Severe Storms", 20.0, 78.0,
                        "2024-01-01T00:00:00Z", null)));
        service.refresh();
        service.markCacheStale();

        when(restTemplate.getForObject(bboxUrl(), Map.class))
                .thenThrow(new ResourceAccessException("NASA EONET down"));

        List<EonetDTO> result = assertDoesNotThrow(() -> service.getEvents());
        assertEquals(1, result.size(), "stale cached data must still be served");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getEvents().size(), "a failed background refresh must keep the previous cache");
        assertEquals("eonet-cached", service.getEvents().get(0).getEventId());
    }

    @Test
    void concurrentGetEventsCallsTriggerOnlyOneRefresh() throws Exception {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.getForObject(bboxUrl(), Map.class)).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return feed(event("eonet-1", "Storm", "Severe Storms", 20.0, 78.0,
                    "2024-01-01T00:00:00Z", null));
        });

        List<EonetDTO> first = service.getEvents();
        assertTrue(first.isEmpty());
        assertTrue(entered.await(2, TimeUnit.SECONDS), "the first refresh must start in the background");

        List<EonetDTO> second = service.getEvents();
        assertTrue(second.isEmpty(), "concurrent callers must get the current cache, not a new fetch");

        release.countDown();
        service.awaitBackgroundRefresh();

        verify(restTemplate, times(1)).getForObject(bboxUrl(), Map.class);
    }

    @Test
    void largeResultListIsNotMutatedByCallers() {
        List<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(event("eonet-" + i, "Event " + i, "Other", 20.0 + i * 0.1, 78.0,
                    "2024-01-01T00:00:00Z", null));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("events", events);
        when(restTemplate.getForObject(bboxUrl(), Map.class)).thenReturn(body);

        service.refresh();
        List<EonetDTO> copy = service.getEvents();
        copy.clear();

        assertEquals(5, service.getEvents().size(), "callers must not corrupt the cache");
    }
}
