package com.disaster.service;

import com.disaster.config.AirQualityProperties;
import com.disaster.config.ApiProperties;
import com.disaster.dto.AirQualityDTO;
import com.disaster.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OpenAQ air-quality module. No network access: every test
 * exercises the India filter, the v2 latest-measurements JSON mapping, the
 * derived AQI/category, the in-memory cache fallback, the stale-while-revalidate
 * background refresh, the bounded retry logic, the missing-key fallback and the
 * notification integration using a mocked {@link RestTemplate}.
 */
class AirQualityServiceTest {

    private AirQualityProperties properties;
    private ApiProperties apiProperties;
    private RestTemplate restTemplate;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private AirQualityServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new AirQualityProperties();
        apiProperties = new ApiProperties();
        apiProperties.getOpenaq().setApiKey("test-key");
        restTemplate = mock(RestTemplate.class);
        notificationService = mock(NotificationService.class);
        notificationRepository = mock(NotificationRepository.class);
        service = new AirQualityServiceImpl(properties, apiProperties, restTemplate,
                notificationService, notificationRepository);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    /** Expected external URL: configurable base + India country filter + result cap. */
    private String latestUrl() {
        return properties.getApiUrl() + "?country=IN&limit=1000";
    }

    private Map<String, Object> location(String name, double lat, double lon, List<Map<String, Object>> measurements) {
        Map<String, Object> result = new HashMap<>();
        result.put("location", name);
        result.put("coordinates", Map.of("latitude", lat, "longitude", lon));
        result.put("measurements", measurements);
        return result;
    }

    private Map<String, Object> measurement(String parameter, double value, String lastUpdated) {
        return Map.of("parameter", parameter, "value", value, "lastUpdated", lastUpdated);
    }

    private Map<String, Object> delhiBody() {
        return Map.of("results", List.of(location("Delhi - ITO", 28.62, 77.23, List.of(
                measurement("pm25", 210.5, "2024-01-01T10:00:00Z"),
                measurement("pm10", 320.0, "2024-01-01T10:00:00Z"),
                measurement("no2", 45.0, "2024-01-01T10:00:00Z"),
                measurement("o3", 30.0, "2024-01-01T10:00:00Z"),
                measurement("co", 1.5, "2024-01-01T10:00:00Z")))));
    }

    private void stubOk(Map<String, Object> body) {
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class))).thenReturn(ResponseEntity.ok(body));
    }

    // ------------------------------------------------------------------

    @Test
    void filtersFeedToIndiaOnly() {
        stubOk(Map.of("results", List.of(
                location("Delhi - ITO", 28.62, 77.23, List.of(measurement("pm25", 100.0, "2024-01-01T10:00:00Z"))),
                location("Los Angeles", 34.05, -118.24, List.of(measurement("pm25", 80.0, "2024-01-01T10:00:00Z"))),
                location("Tokyo", 35.68, 139.65, List.of(measurement("pm25", 70.0, "2024-01-01T10:00:00Z"))))));

        service.refresh();

        List<AirQualityDTO> result = service.getAirQuality();
        assertEquals(1, result.size(), "only stations inside the India bounding box may be returned");
        assertEquals("Delhi - ITO", result.get(0).getStationName());
    }

    @Test
    void mapsPollutantFieldsCorrectly() {
        stubOk(delhiBody());

        service.refresh();

        AirQualityDTO dto = service.getAirQuality().get(0);
        assertEquals("Delhi - ITO", dto.getStationName());
        assertEquals(28.62, dto.getLatitude());
        assertEquals(77.23, dto.getLongitude());
        assertEquals(210.5, dto.getPm25());
        assertEquals(320.0, dto.getPm10());
        assertEquals(45.0, dto.getNo2());
        assertEquals(30.0, dto.getO3());
        assertEquals(1.5, dto.getCo());
        assertEquals(370, dto.getAqi());
        assertEquals("Very Poor", dto.getCategory());
        assertEquals(1704103200000L, dto.getTimestamp());
        assertEquals(AirQualityDTO.SOURCE, dto.getSource());
        assertTrue(dto.getMapsUrl().contains("28.62"));
        assertTrue(dto.getMapsUrl().contains("77.23"));
    }

    @Test
    void sortsMostHazardousFirst() {
        stubOk(Map.of("results", List.of(
                location("Mumbai", 19.076, 72.878, List.of(measurement("pm25", 60.0, "2024-01-01T10:00:00Z"))),
                location("Delhi - ITO", 28.62, 77.23, List.of(measurement("pm25", 300.0, "2024-01-01T10:00:00Z"))),
                location("Chennai", 13.08, 80.27, List.of(measurement("pm25", 20.0, "2024-01-01T10:00:00Z"))))));

        service.refresh();

        List<AirQualityDTO> result = service.getAirQuality();
        assertEquals(3, result.size());
        assertEquals("Delhi - ITO", result.get(0).getStationName(), "worst AQI must come first");
        assertEquals("Mumbai", result.get(1).getStationName());
        assertEquals("Chennai", result.get(2).getStationName());
    }

    @Test
    void missingMeasurementsDoNotBreakMapping() {
        stubOk(Map.of("results", List.of(location("Mumbai", 19.076, 72.878, List.of()))));

        service.refresh();

        AirQualityDTO dto = service.getAirQuality().get(0);
        assertEquals("Mumbai", dto.getStationName());
        assertNull(dto.getPm25());
        assertNull(dto.getAqi());
        assertNull(dto.getCategory());
    }

    @Test
    void locationWithoutCoordinatesIsSkipped() {
        Map<String, Object> noCoords = new HashMap<>();
        noCoords.put("location", "No coords station");
        noCoords.put("coordinates", null);
        noCoords.put("measurements", List.of(measurement("pm25", 100.0, "2024-01-01T10:00:00Z")));
        stubOk(Map.of("results", List.of(noCoords)));

        service.refresh();

        assertTrue(service.getAirQuality().isEmpty());
    }

    @Test
    void apiFailureKeepsServingLastCachedData() {
        stubOk(delhiBody());
        service.refresh();
        assertFalse(service.getAirQuality().isEmpty());

        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("OpenAQ down"));

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getAirQuality().size(), "cached data must survive an API outage");
    }

    @Test
    void apiFailureWithEmptyCacheReturnsEmptyListInsteadOfCrashing() throws Exception {
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("OpenAQ down"));

        assertDoesNotThrow(() -> service.getAirQuality());
        assertTrue(service.getAirQuality().isEmpty());
        assertDoesNotThrow(service::awaitBackgroundRefresh);
        assertTrue(service.getAirQuality().isEmpty(), "a failed background refresh must keep the empty cache");
    }

    @Test
    void missingApiKeyIsGracefulFallback() {
        apiProperties.getOpenaq().setApiKey("");

        assertDoesNotThrow(() -> service.getAirQuality());
        assertTrue(service.getAirQuality().isEmpty());
        verify(restTemplate, never()).exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void retriesThenSucceedsOnTransientFailure() {
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("temporary hiccup"))
                .thenReturn(ResponseEntity.ok(delhiBody()));

        service.refresh();

        assertEquals(1, service.getAirQuality().size(), "a transient failure must be retried transparently");
        verify(restTemplate, times(2)).exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void nullBodyIsHandledGracefully() {
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class))).thenReturn(ResponseEntity.ok(null));

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getAirQuality().isEmpty());
    }

    @Test
    void emptyResultsIsAcceptedAndReturnsEmptyResult() {
        stubOk(Map.of("results", List.of()));

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getAirQuality().isEmpty(), "no stations inside India is not an error");
    }

    @Test
    void createsNotificationsOnlyForHazardousStations() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        stubOk(Map.of("results", List.of(
                location("Delhi - ITO", 28.62, 77.23, List.of(measurement("pm25", 210.5, "2024-01-01T10:00:00Z"))),
                location("Mumbai", 19.076, 72.878, List.of(measurement("pm25", 50.0, "2024-01-01T10:00:00Z"))))));

        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void doesNotNotifyTheSameStationTwice() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        stubOk(delhiBody());

        service.refresh();
        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void failedNotificationCreationDoesNotBreakRefresh() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        stubOk(delhiBody());
        doThrow(new RuntimeException("db down"))
                .when(notificationService).notifyAllUsers(anyString(), anyString(), anyString());

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getAirQuality().size());
    }

    @Test
    void getAirQualityWithEmptyCacheReturnsEmptyImmediatelyAndRefreshesInBackground() throws Exception {
        stubOk(delhiBody());

        List<AirQualityDTO> result = service.getAirQuality();

        assertTrue(result.isEmpty(), "an empty cache must return immediately without waiting on OpenAQ");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getAirQuality().size(), "the background refresh must eventually populate the cache");
        verify(restTemplate, times(1)).exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void getAirQualityWithWarmCacheReturnsCachedDataImmediately() {
        stubOk(delhiBody());
        service.refresh();

        List<AirQualityDTO> result = service.getAirQuality();

        assertEquals(1, result.size(), "a warm cache must be served without re-fetching");
        verify(restTemplate, times(1)).exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void staleCacheReturnsCachedDataWithoutSynchronousExternalCall() throws Exception {
        stubOk(delhiBody());
        service.refresh();
        assertEquals(1, service.getAirQuality().size());
        service.markCacheStale();

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class))).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return ResponseEntity.ok(Map.of("results", List.of(
                    location("Delhi - ITO", 28.62, 77.23,
                            List.of(measurement("pm25", 300.0, "2024-01-01T11:00:00Z"))))));
        });

        long start = System.nanoTime();
        List<AirQualityDTO> result = service.getAirQuality();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(1, result.size(), "a stale cache must still be served from memory");
        assertEquals(210.5, result.get(0).getPm25());
        assertTrue(elapsedMs < 1000, "getAirQuality() must not block on the external OpenAQ API");
        assertTrue(entered.await(2, TimeUnit.SECONDS), "a background refresh must have been triggered");

        release.countDown();
        service.awaitBackgroundRefresh();
        assertEquals(300.0, service.getAirQuality().get(0).getPm25(),
                "the background refresh must replace the stale cache once it completes");
    }

    @Test
    void failedBackgroundRefreshKeepsExistingCache() throws Exception {
        stubOk(delhiBody());
        service.refresh();
        service.markCacheStale();

        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("OpenAQ down"));

        List<AirQualityDTO> result = assertDoesNotThrow(() -> service.getAirQuality());
        assertEquals(1, result.size(), "stale cached data must still be served");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getAirQuality().size(), "a failed background refresh must keep the previous cache");
        assertEquals("Delhi - ITO", service.getAirQuality().get(0).getStationName());
    }

    @Test
    void concurrentGetAirQualityCallsTriggerOnlyOneRefresh() throws Exception {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class))).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return ResponseEntity.ok(delhiBody());
        });

        List<AirQualityDTO> first = service.getAirQuality();
        assertTrue(first.isEmpty());
        assertTrue(entered.await(2, TimeUnit.SECONDS), "the first refresh must start in the background");

        List<AirQualityDTO> second = service.getAirQuality();
        assertTrue(second.isEmpty(), "concurrent callers must get the current cache, not a new fetch");

        release.countDown();
        service.awaitBackgroundRefresh();

        verify(restTemplate, times(1)).exchange(eq(latestUrl()), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void largeResultListIsNotMutatedByCallers() {
        List<Map<String, Object>> stations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            stations.add(location("Station " + i, 20.0 + i * 0.1, 78.0,
                    List.of(measurement("pm25", 30.0 + i, "2024-01-01T10:00:00Z"))));
        }
        stubOk(Map.of("results", stations));

        service.refresh();
        List<AirQualityDTO> copy = service.getAirQuality();
        copy.clear();

        assertEquals(5, service.getAirQuality().size(), "callers must not corrupt the cache");
    }

    // ------------------------------------------------------------------
    // AQI / category derivation (rule-based, no ML)
    // ------------------------------------------------------------------

    @Test
    void derivesNumericAqiFromPm25() {
        assertEquals(50, AirQualityDTO.aqi(30.0));
        assertEquals(51, AirQualityDTO.aqi(31.0));
        assertEquals(101, AirQualityDTO.aqi(61.0));
        assertEquals(201, AirQualityDTO.aqi(91.0));
        assertEquals(301, AirQualityDTO.aqi(121.0));
        assertEquals(500, AirQualityDTO.aqi(251.0));
        assertEquals(500, AirQualityDTO.aqi(400.0));
        assertNull(AirQualityDTO.aqi(null));
    }

    @Test
    void derivesCategoryFromPm25() {
        assertEquals("Good", AirQualityDTO.aqiCategory(10.0));
        assertEquals("Satisfactory", AirQualityDTO.aqiCategory(45.0));
        assertEquals("Moderately Polluted", AirQualityDTO.aqiCategory(75.0));
        assertEquals("Poor", AirQualityDTO.aqiCategory(100.0));
        assertEquals("Very Poor", AirQualityDTO.aqiCategory(150.0));
        assertEquals("Severe", AirQualityDTO.aqiCategory(300.0));
        assertNull(AirQualityDTO.aqiCategory(null));
    }
}
