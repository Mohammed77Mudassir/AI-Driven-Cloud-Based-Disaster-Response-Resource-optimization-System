package com.disaster.service;

import com.disaster.config.ApiProperties;
import com.disaster.config.FireProperties;
import com.disaster.dto.FireDTO;
import com.disaster.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the NASA FIRMS fire module. No network access: every test
 * exercises the India filter, CSV response mapping (both VIIRS and MODIS column
 * layouts), the in-memory cache fallback, the stale-while-revalidate
 * background refresh, the bounded retry logic, the missing-key fallback and the
 * notification integration using a mocked {@link RestTemplate}.
 */
class FireServiceTest {

    private FireProperties properties;
    private ApiProperties apiProperties;
    private RestTemplate restTemplate;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private FireServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new FireProperties();
        apiProperties = new ApiProperties();
        apiProperties.getNasaFirms().setApiKey("test-key");
        restTemplate = mock(RestTemplate.class);
        notificationService = mock(NotificationService.class);
        notificationRepository = mock(NotificationRepository.class);
        service = new FireServiceImpl(properties, apiProperties, restTemplate,
                notificationService, notificationRepository);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    /** Expected external URL: base + key + source + India area + query window. */
    private String firmsUrl(String source) {
        return properties.getApiUrl() + "/test-key/" + source
                + "/bb_6.7,68.1,37.1,97.4/" + properties.getDay() + "/" + properties.getDayRange();
    }

    private String viiirsCsv(String rows) {
        return "latitude,longitude,bright_ti4,scan,track,acq_date,acq_time,satellite,instrument,"
                + "confidence,version,bright_ti5,frp,daynight\n" + rows;
    }

    private String modisCsv(String rows) {
        return "latitude,longitude,brightness,scan,track,acq_date,acq_time,satellite,instrument,"
                + "confidence,version,bright_ti5,frp,daynight\n" + rows;
    }

    private void singleSource() {
        properties.setSources(List.of("VIIRS_SNPP_NRT"));
    }

    // ------------------------------------------------------------------

    @Test
    void filtersFeedToIndiaOnly() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(
                        "19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D\n"
                                + "36.7783,-119.4179,331.0,1,1,2024-01-01,0200,NPP,VIIRS,85,2,300.5,9.2,D\n"
                                + "35.6762,139.6503,329.0,1,1,2024-01-01,0300,NPP,VIIRS,80,2,298.5,7.1,D"));

        service.refresh();

        List<FireDTO> result = service.getFires();
        assertEquals(1, result.size(), "only detections inside the India bounding box may be returned");
        assertEquals(19.076, result.get(0).getLatitude());
        assertEquals(72.878, result.get(0).getLongitude());
    }

    @Test
    void mapsViiirsFieldsCorrectly() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));

        service.refresh();

        FireDTO dto = service.getFires().get(0);
        assertEquals(19.076, dto.getLatitude());
        assertEquals(72.878, dto.getLongitude());
        assertEquals(345.2, dto.getBrightness());
        assertEquals(90.0, dto.getConfidence());
        assertEquals(1704070920000L, dto.getAcquisitionDate());
        assertEquals("NPP", dto.getSatellite());
        assertEquals("VIIRS", dto.getInstrument());
        assertEquals(12.4, dto.getFrp());
        assertEquals("D", dto.getDayNight());
        assertEquals(FireDTO.SOURCE, dto.getSource());
        assertTrue(dto.getMapsUrl().contains("19.076"));
        assertTrue(dto.getMapsUrl().contains("72.878"));
        assertTrue(dto.getFireId().startsWith("firms-"));
    }

    @Test
    void readsModisColumnLayout() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(modisCsv("13.0827,80.2707,311.0,1,1,2024-01-01,0300,Terra,MODIS,75,6.1,298.5,5.2,D"));

        service.refresh();

        FireDTO dto = service.getFires().get(0);
        assertEquals(311.0, dto.getBrightness(), "MODIS brightness column must be read");
        assertEquals("Terra", dto.getSatellite());
        assertEquals("MODIS", dto.getInstrument());
        assertEquals(75.0, dto.getConfidence());
        assertEquals(5.2, dto.getFrp());
    }

    @Test
    void sortsNewestFirst() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(
                        "20.0,78.0,330.0,1,1,2024-01-01,0102,NPP,VIIRS,80,2,300.5,5.0,D\n"
                                + "21.0,79.0,340.0,1,1,2024-01-02,0300,NPP,VIIRS,90,2,310.5,9.0,D"));

        service.refresh();

        List<FireDTO> result = service.getFires();
        assertEquals(2, result.size());
        assertEquals(21.0, result.get(0).getLatitude(), "newest acquisition must come first");
    }

    @Test
    void mergesAcrossConfiguredSources() {
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        when(restTemplate.getForObject(firmsUrl("MODIS_NRT"), String.class))
                .thenReturn(modisCsv("19.076,72.878,311.0,1,1,2024-01-01,0102,Terra,MODIS,75,6.1,298.5,5.2,D"));

        service.refresh();

        assertEquals(2, service.getFires().size(), "detections from every configured source must be merged");
        verify(restTemplate).getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class);
        verify(restTemplate).getForObject(firmsUrl("MODIS_NRT"), String.class);
    }

    @Test
    void deduplicatesIdenticalRowsWithinASource() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(
                        "19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D\n"
                                + "19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));

        service.refresh();

        assertEquals(1, service.getFires().size(), "identical duplicate detections must be de-duplicated");
    }

    @Test
    void apiFailureKeepsServingLastCachedData() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        service.refresh();
        assertFalse(service.getFires().isEmpty());

        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenThrow(new ResourceAccessException("NASA FIRMS down"));

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getFires().size(), "cached data must survive an API outage");
    }

    @Test
    void apiFailureWithEmptyCacheReturnsEmptyListInsteadOfCrashing() throws Exception {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenThrow(new ResourceAccessException("NASA FIRMS down"));

        assertDoesNotThrow(() -> service.getFires());
        assertTrue(service.getFires().isEmpty());
        assertDoesNotThrow(service::awaitBackgroundRefresh);
        assertTrue(service.getFires().isEmpty(), "a failed background refresh must keep the empty cache");
    }

    @Test
    void missingApiKeyIsGracefulFallback() {
        apiProperties.getNasaFirms().setApiKey("");
        singleSource();

        assertDoesNotThrow(() -> service.getFires());
        assertTrue(service.getFires().isEmpty());
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

    @Test
    void retriesThenSucceedsOnTransientFailure() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenThrow(new ResourceAccessException("temporary hiccup"))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));

        service.refresh();

        assertEquals(1, service.getFires().size(), "a transient failure must be retried transparently");
        verify(restTemplate, times(2)).getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class);
    }

    @Test
    void malformedCsvRowsAreSkippedGracefully() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(
                        "19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D\n"
                                + ",,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D\n"
                                + "20.0,78.0,330.0,1,1,2024-01-01,0102,NPP,VIIRS,abc,2,300.5,5.0,D\n"
                                + "\n"));

        assertDoesNotThrow(service::refresh);
        assertEquals(2, service.getFires().size(), "rows with missing coords or bad numbers must be dropped");
        assertNull(service.getFires().get(1).getConfidence(), "non-numeric confidence must map to null");
    }

    @Test
    void emptyCsvIsAcceptedAndReturnsEmptyResult() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(""));

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getFires().isEmpty(), "no fires inside India is not an error");
    }

    @Test
    void nullResponseIsHandledGracefully() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(null);

        assertDoesNotThrow(service::refresh);
        assertTrue(service.getFires().isEmpty());
    }

    @Test
    void createsNotificationsOnlyForHighConfidenceFires() {
        singleSource();
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(
                        "19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D\n"
                                + "20.0,78.0,330.0,1,1,2024-01-01,0102,NOAA-20,VIIRS,40,2,300.5,5.0,D"));

        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void doesNotNotifyTheSameFireTwice() {
        singleSource();
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));

        service.refresh();
        service.refresh();

        verify(notificationService, times(1)).notifyAllUsers(anyString(), anyString(), eq("ALERT"));
    }

    @Test
    void failedNotificationCreationDoesNotBreakRefresh() {
        singleSource();
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        doThrow(new RuntimeException("db down"))
                .when(notificationService).notifyAllUsers(anyString(), anyString(), anyString());

        assertDoesNotThrow(service::refresh);
        assertEquals(1, service.getFires().size());
    }

    @Test
    void getFiresWithEmptyCacheReturnsEmptyImmediatelyAndRefreshesInBackground() throws Exception {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));

        List<FireDTO> result = service.getFires();

        assertTrue(result.isEmpty(), "an empty cache must return immediately without waiting on FIRMS");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getFires().size(), "the background refresh must eventually populate the cache");
        verify(restTemplate, times(1)).getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class);
    }

    @Test
    void getFiresWithWarmCacheReturnsCachedDataImmediately() {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        service.refresh();

        List<FireDTO> result = service.getFires();

        assertEquals(1, result.size(), "a warm cache must be served without re-fetching");
        verify(restTemplate, times(1)).getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class);
    }

    @Test
    void staleCacheReturnsCachedDataWithoutSynchronousExternalCall() throws Exception {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        service.refresh();
        assertEquals(1, service.getFires().size());
        service.markCacheStale();

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class)).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return viiirsCsv("20.0,78.0,340.0,1,1,2024-01-02,0300,NPP,VIIRS,90,2,310.5,9.0,D");
        });

        long start = System.nanoTime();
        List<FireDTO> result = service.getFires();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(1, result.size(), "a stale cache must still be served from memory");
        assertEquals(19.076, result.get(0).getLatitude());
        assertTrue(elapsedMs < 1000, "getFires() must not block on the external FIRMS API");
        assertTrue(entered.await(2, TimeUnit.SECONDS), "a background refresh must have been triggered");

        release.countDown();
        service.awaitBackgroundRefresh();
        assertEquals(20.0, service.getFires().get(0).getLatitude(),
                "the background refresh must replace the stale cache once it completes");
    }

    @Test
    void failedBackgroundRefreshKeepsExistingCache() throws Exception {
        singleSource();
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D"));
        service.refresh();
        service.markCacheStale();

        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenThrow(new ResourceAccessException("NASA FIRMS down"));

        List<FireDTO> result = assertDoesNotThrow(() -> service.getFires());
        assertEquals(1, result.size(), "stale cached data must still be served");
        service.awaitBackgroundRefresh();
        assertEquals(1, service.getFires().size(), "a failed background refresh must keep the previous cache");
        assertEquals(19.076, service.getFires().get(0).getLatitude());
    }

    @Test
    void concurrentGetFiresCallsTriggerOnlyOneRefresh() throws Exception {
        singleSource();
        when(notificationRepository.existsByTitle(anyString())).thenReturn(false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class)).thenAnswer(inv -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return viiirsCsv("19.076,72.878,345.2,1,1,2024-01-01,0102,NPP,VIIRS,90,2,310.5,12.4,D");
        });

        List<FireDTO> first = service.getFires();
        assertTrue(first.isEmpty());
        assertTrue(entered.await(2, TimeUnit.SECONDS), "the first refresh must start in the background");

        List<FireDTO> second = service.getFires();
        assertTrue(second.isEmpty(), "concurrent callers must get the current cache, not a new fetch");

        release.countDown();
        service.awaitBackgroundRefresh();

        verify(restTemplate, times(1)).getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class);
    }

    @Test
    void largeResultListIsNotMutatedByCallers() {
        singleSource();
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            csv.append(String.format(java.util.Locale.ROOT, "%.3f,78.0,330.0,1,1,2024-01-01,0102,NPP,VIIRS,80,2,300.5,5.0,D\n",
                    20.0 + i * 0.1));
        }
        when(restTemplate.getForObject(firmsUrl("VIIRS_SNPP_NRT"), String.class))
                .thenReturn(viiirsCsv(csv.toString()));

        service.refresh();
        List<FireDTO> copy = service.getFires();
        copy.clear();

        assertEquals(5, service.getFires().size(), "callers must not corrupt the cache");
    }
}
