package com.disaster.service;

import com.disaster.config.EarthquakeProperties;
import com.disaster.dto.EarthquakeDTO;
import com.disaster.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the USGS earthquake module. No network access: every test
 * exercises the India filter, GeoJSON mapping, risk-level rules, the in-memory
 * cache fallback and the high/critical notification integration using a mocked
 * {@link RestTemplate}.
 */
class EarthquakeServiceTest {

    private EarthquakeProperties properties;
    private RestTemplate restTemplate;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private EarthquakeServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new EarthquakeProperties();
        restTemplate = mock(RestTemplate.class);
        notificationService = mock(NotificationService.class);
        notificationRepository = mock(NotificationRepository.class);
        service = new EarthquakeServiceImpl(properties, restTemplate,
                notificationService, notificationRepository);
    }

    private Map<String, Object> feature(String id, double mag, double lat, double lon,
                                        double depth, long time, String place) {
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", List.of(lon, lat, depth));

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("mag", mag);
        props.put("place", place);
        props.put("time", time);

        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", id);
        feature.put("properties", props);
        feature.put("geometry", geometry);
        return feature;
    }

    private Map<String, Object> feed(Map<String, Object>... features) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "FeatureCollection");
        body.put("features", List.of(features));
        return body;
    }

    // ------------------------------------------------------------------

    @Test
    void filtersFeedToIndiaOnly() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(
                        feature("us-delhi", 4.2, 28.6139, 77.2090, 10.0, 1700000000000L, "Delhi, India"),
                        feature("us-cali", 5.8, 36.7783, -119.4179, 8.0, 1700000000001L, "California, USA"),
                        feature("us-okinawa", 6.1, 26.3344, 127.8056, 20.0, 1700000000002L, "Okinawa, Japan"),
                        feature("us-kashmir", 3.6, 34.0837, 74.7973, 15.0, 1700000000003L, "Kashmir, India")));

        service.refresh();

        List<EarthquakeDTO> result = service.getEarthquakes();
        assertEquals(2, result.size(), "only events inside the India bounding box may be returned");
        assertTrue(result.stream().allMatch(e -> e.getLongitude() >= 68.0 && e.getLongitude() <= 97.5));
        assertTrue(result.stream().noneMatch(e -> e.getId().equals("us-cali")));
        assertTrue(result.stream().noneMatch(e -> e.getId().equals("us-okinawa")));
    }

    @Test
    void mapsGeoJsonFieldsCorrectly() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(feature("us7000abc", 6.8, 26.1445, 91.7362, 12.5,
                        1700000000000L, "45 km N of Guwahati, Assam")));

        service.refresh();

        EarthquakeDTO dto = service.getEarthquakes().get(0);
        assertEquals("us7000abc", dto.getId());
        assertEquals(6.8, dto.getMagnitude());
        assertEquals("45 km N of Guwahati, Assam", dto.getLocation());
        assertEquals(26.1445, dto.getLatitude());
        assertEquals(91.7362, dto.getLongitude());
        assertEquals(12.5, dto.getDepth());
        assertEquals(1700000000000L, dto.getTime());
        assertEquals("Critical", dto.getRiskLevel());
        assertEquals("USGS", dto.getSource());
        assertTrue(dto.getMapsUrl().contains("26.1445"));
        assertTrue(dto.getMapsUrl().contains("91.7362"));
    }

    @Test
    void sortsNewestFirst() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(
                        feature("us-old", 4.0, 28.0, 77.0, 5.0, 1000L, "Old quake"),
                        feature("us-new", 4.5, 28.0, 77.0, 5.0, 9999L, "New quake")));

        service.refresh();

        List<EarthquakeDTO> result = service.getEarthquakes();
        assertEquals("us-new", result.get(0).getId());
    }

    @Test
    void skipsEventsWithoutMagnitudeOrCoordinates() {
        Map<String, Object> noMag = feature("us-nomag", Double.NaN, 28.0, 77.0, 5.0, 1000L, "No mag");
        ((Map<String, Object>) noMag.get("properties")).put("mag", null);

        Map<String, Object> noGeom = feature("us-nogeom", 4.0, 28.0, 77.0, 5.0, 1000L, "No geom");
        noGeom.put("geometry", null);

        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(
                        feature("us-ok", 4.1, 28.0, 77.0, 5.0, 1000L, "OK"),
                        noMag,
                        noGeom));

        service.refresh();

        assertEquals(1, service.getEarthquakes().size());
        assertEquals("us-ok", service.getEarthquakes().get(0).getId());
    }

    @Test
    void riskLevelBoundariesMatchSpecification() {
        assertEquals("Low", EarthquakeDTO.determineRiskLevel(3.4));
        assertEquals("Moderate", EarthquakeDTO.determineRiskLevel(3.5));
        assertEquals("Moderate", EarthquakeDTO.determineRiskLevel(5.4));
        assertEquals("High", EarthquakeDTO.determineRiskLevel(5.5));
        assertEquals("High", EarthquakeDTO.determineRiskLevel(6.4));
        assertEquals("Critical", EarthquakeDTO.determineRiskLevel(6.5));
        assertEquals("Critical", EarthquakeDTO.determineRiskLevel(7.8));
    }

    @Test
    void apiFailureKeepsServingLastCachedData() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(feature("us-cached", 5.1, 28.0, 77.0, 6.0, 1000L, "Cached quake")));
        service.refresh();
        assertFalse(service.getEarthquakes().isEmpty());

        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenThrow(new ResourceAccessException("USGS down"));

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getEarthquakes().size(), "cached data must survive an API outage");
        assertEquals("us-cached", service.getEarthquakes().get(0).getId());
    }

    @Test
    void apiFailureWithEmptyCacheReturnsEmptyListInsteadOfCrashing() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenThrow(new ResourceAccessException("USGS down"));

        assertDoesNotThrow(() -> service.getEarthquakes());
        assertTrue(service.getEarthquakes().isEmpty());
    }

    @Test
    void malformedResponseIsHandledGracefully() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(null);

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getEarthquakes().isEmpty());
    }

    @Test
    void createsNotificationsForHighAndCriticalOnly() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(
                        feature("us-high", 5.8, 28.0, 77.0, 6.0, 1000L, "High quake"),
                        feature("us-critical", 6.9, 26.0, 91.0, 6.0, 2000L, "Critical quake"),
                        feature("us-moderate", 4.2, 28.0, 77.0, 6.0, 3000L, "Moderate quake"),
                        feature("us-low", 2.5, 28.0, 77.0, 6.0, 4000L, "Low quake")));

        service.refresh();

        verify(notificationService, times(2)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
        verify(notificationService, never()).notifyAllUsers(argThat(t -> t.contains("Moderate")), anyString(), anyString());
    }

    @Test
    void doesNotNotifyTheSameEventTwice() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(feature("us-high", 6.0, 28.0, 77.0, 6.0, 1000L, "High quake")));

        service.refresh();
        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void failedNotificationCreationDoesNotBreakRefresh() {
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(feature("us-high", 5.9, 28.0, 77.0, 6.0, 1000L, "High quake")));
        doThrow(new RuntimeException("db down"))
                .when(notificationService).notifyAllUsers(anyString(), anyString(), anyString());

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getEarthquakes().size());
    }

    @Test
    void emptyFeatureListIsAccepted() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed());
        assertDoesNotThrow(service::refresh);
        assertTrue(service.getEarthquakes().isEmpty());
    }

    @Test
    void getEarthquakesTriggersLazyRefreshWhenCacheIsEmpty() {
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class))
                .thenReturn(feed(feature("us-lazy", 4.4, 28.0, 77.0, 6.0, 1000L, "Lazy quake")));

        List<EarthquakeDTO> result = service.getEarthquakes();

        assertEquals(1, result.size(), "first read populates the cache");
        verify(restTemplate, times(1)).getForObject(properties.getApiUrl(), Map.class);
    }

    @Test
    void largeResultListIsNotMutatedByCallers() {
        List<Map<String, Object>> features = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            features.add(feature("us-" + i, 4.0 + i * 0.1, 28.0, 77.0, 6.0, 1000L + i, "Quake " + i));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("features", features);
        when(restTemplate.getForObject(properties.getApiUrl(), Map.class)).thenReturn(body);

        service.refresh();
        List<EarthquakeDTO> copy = service.getEarthquakes();
        copy.clear();

        assertEquals(5, service.getEarthquakes().size(), "callers must not corrupt the cache");
    }
}
