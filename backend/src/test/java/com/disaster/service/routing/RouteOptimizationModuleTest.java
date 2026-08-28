package com.disaster.service.routing;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.RouteResponse;
import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RoadClosureRequest;
import com.disaster.dto.routing.RouteOption;
import com.disaster.service.RouteOptimizationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the enterprise routing module. No network access: every test
 * exercises the offline/Haversine engine, the provider fallback and the
 * order optimizer so the module can be verified without an API key.
 */
class RouteOptimizationModuleTest {

    private HaversineRouteProvider haversineProvider() {
        HaversineRouteProvider provider = new HaversineRouteProvider();
        ReflectionTestUtils.setField(provider, "normalSpeedKmph", 40.0);
        ReflectionTestUtils.setField(provider, "emergencySpeedKmph", 55.0);
        return provider;
    }

    /** A client with no API key: the provider must fall back without any network call. */
    private OpenRouteServiceClient unconfiguredClient() {
        return new OpenRouteServiceClient((RestTemplate) null, "",
                "https://api.openrouteservice.org/v2/directions", 1000, 0);
    }

    private OpenRouteServiceProvider orsProvider(OpenRouteServiceClient client) {
        return new OpenRouteServiceProvider(client);
    }

    /** Service wired to the Haversine fallback with a deterministic cache TTL. */
    private RouteOptimizationService haversineService() {
        RouteOptimizationService service = new RouteOptimizationService(
                orsProvider(unconfiguredClient()),
                new GoogleMapsDirectionsProvider(),
                new MapboxDirectionsProvider(),
                haversineProvider(),
                new InMemoryRoadClosureService());
        ReflectionTestUtils.setField(service, "cacheTtlMinutes", 10L);
        ReflectionTestUtils.invokeMethod(service, "initCache");
        return service;
    }

    private RouteRequest basicRequest() {
        RouteRequest request = new RouteRequest();
        request.setStart(new RouteRequest.Point(28.6139, 77.2090));
        request.setEnd(new RouteRequest.Point(19.0760, 72.8777));
        request.setMode("fastest");
        return request;
    }

    private Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<RouteRequest>> violations(RouteRequest request) {
        return validator().validate(request);
    }

    // ------------------------------------------------------------------
    // Coordinate validation

    @Test
    void validIndianCoordinatesPassValidation() {
        assertTrue(violations(basicRequest()).isEmpty(),
                "Delhi -> Mumbai coordinates must be accepted");
    }

    @Test
    void invalidStartLatitudeIsRejected() {
        RouteRequest request = basicRequest();
        request.setStart(new RouteRequest.Point(999, 77.2090));

        Set<ConstraintViolation<RouteRequest>> violations = violations(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("start.latitude")),
                "invalid start latitude must be reported");
    }

    @Test
    void invalidEndLongitudeIsRejected() {
        RouteRequest request = basicRequest();
        request.setEnd(new RouteRequest.Point(19.0760, 999));

        Set<ConstraintViolation<RouteRequest>> violations = violations(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("end.longitude")),
                "invalid end longitude must be reported");
    }

    @Test
    void invalidCoordinatesOnAnyMultiStopPointAreRejected() {
        RouteRequest request = basicRequest();
        request.setDestinations(List.of(
                new RouteRequest.Point(12.9716, 77.5946),
                new RouteRequest.Point(-120, 40.0)));

        Set<ConstraintViolation<RouteRequest>> violations = violations(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("destinations[1].latitude")),
                "invalid multi-stop point must be reported");
    }

    @Test
    void boundaryCoordinatesAreAccepted() {
        RouteRequest request = basicRequest();
        request.setStart(new RouteRequest.Point(-90, -180));
        request.setEnd(new RouteRequest.Point(90, 180));

        assertTrue(violations(request).isEmpty(),
                "boundary latitude/longitude values must be accepted");
    }

    private RouteContext context(RouteRequest request, List<RoadClosure> closures) {
        List<RouteRequest.Point> stops = new ArrayList<>();
        stops.add(request.getStart());
        stops.add(request.getEnd());
        return new RouteContext(request, stops, closures == null ? List.of() : closures);
    }

    // ------------------------------------------------------------------

    @Test
    void optimizerReducesDistanceForSuboptimalOrder() {
        RouteRequest.Point start = new RouteRequest.Point(0.0, 0.0);
        List<RouteRequest.Point> stops = new ArrayList<>(List.of(
                new RouteRequest.Point(10.0, 0.0),
                new RouteRequest.Point(5.0, 1.0),
                new RouteRequest.Point(0.1, 0.1)
        ));

        List<RouteRequest.Point> original = new ArrayList<>(List.of(start, stops.get(0), stops.get(1), stops.get(2)));
        List<RouteRequest.Point> optimized = new ArrayList<>(List.of(start));
        optimized.addAll(RouteOrderOptimizer.optimizeOrder(start, stops));

        double before = RouteOrderOptimizer.totalDistanceKm(original);
        double after = RouteOrderOptimizer.totalDistanceKm(optimized);
        assertTrue(after <= before + 1e-6, "optimized route should not be longer");
        assertEquals(4, optimized.size());
        assertEquals(stops.get(2).getLatitude(), optimized.get(1).getLatitude(), 1e-9);
    }

    @Test
    void haversineProviderReturnsUsableFastestRoute() {
        HaversineRouteProvider provider = haversineProvider();
        List<RouteOption> options = provider.calculateRoutes(context(basicRequest(), List.of()));

        assertFalse(options.isEmpty());
        RouteOption fastest = options.get(0);
        assertEquals("fastest", fastest.getPreference());
        assertTrue(fastest.getDistanceKm() > 0);
        assertTrue(fastest.getTimeMinutes() > 0);
        assertFalse(fastest.getGeometry().isEmpty());
        assertTrue(fastest.isRecommended());
        assertFalse(fastest.isTrafficAware());
    }

    @Test
    void haversineProviderGeneratesAlternatives() {
        HaversineRouteProvider provider = haversineProvider();
        RouteRequest request = basicRequest();
        request.setAlternativesCount(3);
        List<RouteOption> options = provider.calculateRoutes(context(request, List.of()));

        assertEquals(3, options.size());
        assertEquals("alternative", options.get(2).getPreference());
        assertNotEquals(options.get(0).getDistanceKm(), options.get(2).getDistanceKm(), 0.5);
    }

    @Test
    void haversineProviderDetoursAroundRoadClosure() {
        HaversineRouteProvider provider = haversineProvider();

        RoadClosure closure = new RoadClosure();
        closure.setType("FLOODED");
        closure.setStatus("ACTIVE");
        closure.setLatitude(28.6);
        closure.setLongitude(77.2);
        closure.setAvoidanceRadiusKm(5.0);

        List<RouteOption> options = provider.calculateRoutes(context(basicRequest(), List.of(closure)));
        assertTrue(options.get(0).isBlockedRoadsAvoided());
        assertTrue(options.get(0).getGeometry().size() > 2, "detour should add geometry points");
    }

    @Test
    void openRouteServiceProviderThrowsWhenNotConfigured() {
        OpenRouteServiceProvider provider = orsProvider(unconfiguredClient());

        assertFalse(provider.isConfigured());
        assertThrows(RoutingUnavailableException.class,
                () -> provider.calculateRoutes(context(basicRequest(), List.of())));
    }

    @Test
    void serviceFallsBackToHaversineWhenProviderNotConfigured() {
        RouteOptimizationService service = haversineService();

        RouteResponse response = service.calculateRoute(basicRequest());

        assertTrue(response.isFallback());
        assertEquals("haversine", response.getProvider());
        assertEquals("FALLBACK", response.getRouteStatus());
        assertTrue(response.getTotalDistanceKm() > 0);
        assertNotNull(response.getAlternatives());
        assertFalse(response.getAlternatives().isEmpty());
        assertFalse(response.isTrafficAware());
        assertNotNull(response.getTraffic());
        assertFalse(response.getTraffic().isAvailable());
        assertTrue(response.getTraffic().getMessage().startsWith("Standard routing in use"));
        assertNotNull(response.getLastUpdated());
    }

    @Test
    void serviceCachesIdenticalRequests() {
        RouteOptimizationService service = haversineService();

        RouteResponse first = service.calculateRoute(basicRequest());
        RouteResponse second = service.calculateRoute(basicRequest());
        assertSame(first, second, "identical requests should hit the cache");
    }

    @Test
    void responseIncludesStraightLineDistance() {
        RouteOptimizationService service = haversineService();

        RouteResponse response = service.calculateRoute(basicRequest());

        assertTrue(response.getStraightLineDistanceKm() > 0,
                "straight-line distance between Delhi and Mumbai must be positive");
        assertTrue(response.getStraightLineDistanceKm() <= response.getTotalDistanceKm(),
                "road distance should never be shorter than the straight-line distance");
    }

    @Test
    void clientRetriesTransientFailuresAndSucceeds() {
        RestTemplate rest = mock(RestTemplate.class);
        ResponseEntity<String> ok = new ResponseEntity<>("{\"routes\":[]}", HttpStatus.OK);
        when(rest.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenReturn(ok);

        OpenRouteServiceClient client = new OpenRouteServiceClient(
                rest, "test-key", "https://api.openrouteservice.org/v2/directions", 500, 2);
        client.setRetryBackoffMs(0);

        Map<String, Object> result = client.requestDirections("driving-car", new LinkedHashMap<>());

        assertTrue(result.containsKey("routes"));
        verify(rest, times(3)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void clientGivesUpAfterRetriesAreExhausted() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("down"));

        OpenRouteServiceClient client = new OpenRouteServiceClient(
                rest, "test-key", "https://api.openrouteservice.org/v2/directions", 500, 2);
        client.setRetryBackoffMs(0);

        assertThrows(RoutingUnavailableException.class,
                () -> client.requestDirections("driving-car", new LinkedHashMap<>()));
        verify(rest, times(3)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void clientSkipsNetworkWhenKeyMissing() {
        RestTemplate rest = mock(RestTemplate.class);
        OpenRouteServiceClient client = unconfiguredClient();

        assertFalse(client.isConfigured());
        assertThrows(RoutingUnavailableException.class,
                () -> client.requestDirections("driving-car", new LinkedHashMap<>()));
        verify(rest, never()).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void closureServiceAddsAndClearsClosures() {
        InMemoryRoadClosureService service = new InMemoryRoadClosureService();

        RoadClosureRequest request = new RoadClosureRequest();
        request.setType("LANDSLIDE");
        request.setLatitude(28.5);
        request.setLongitude(77.2);
        request.setLocationName("Mountain Road");
        request.setDescription("Rocks on road");
        request.setAvoidanceRadiusKm(2.0);

        RoadClosure closure = service.addClosure(request);
        assertNotNull(closure.getId());
        assertEquals("ACTIVE", closure.getStatus());
        assertEquals("LANDSLIDE", closure.getType());
        assertEquals(1, service.getActiveClosures().size());

        RoadClosure removed = service.removeClosure(closure.getId());
        assertNotNull(removed);
        assertTrue(service.getActiveClosures().isEmpty());
    }

    @Test
    void closureServiceRejectsUnknownType() {
        InMemoryRoadClosureService service = new InMemoryRoadClosureService();
        RoadClosureRequest request = new RoadClosureRequest();
        request.setType("TSUNAMI");
        request.setLatitude(10.0);
        request.setLongitude(10.0);
        assertThrows(IllegalArgumentException.class, () -> service.addClosure(request));
    }

    @Test
    void polylineDecoderHandlesEmptyAndValidInput() {
        PolylineCodec codec = new PolylineCodec();
        assertTrue(codec.decode(null).isEmpty());
        assertTrue(codec.decode("").isEmpty());

        List<double[]> points = codec.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
        assertFalse(points.isEmpty());
        assertTrue(points.get(0).length == 2);
    }
}
