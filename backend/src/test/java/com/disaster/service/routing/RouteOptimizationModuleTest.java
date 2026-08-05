package com.disaster.service.routing;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.RouteResponse;
import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RoadClosureRequest;
import com.disaster.dto.routing.RouteOption;
import com.disaster.service.RouteOptimizationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    private RouteRequest basicRequest() {
        RouteRequest request = new RouteRequest();
        request.setStart(new RouteRequest.Point(28.6139, 77.2090));
        request.setEnd(new RouteRequest.Point(19.0760, 72.8777));
        request.setMode("fastest");
        return request;
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
        OpenRouteServiceProvider provider = new OpenRouteServiceProvider();
        ReflectionTestUtils.setField(provider, "apiKey", "");

        assertFalse(provider.isConfigured());
        assertThrows(RoutingUnavailableException.class,
                () -> provider.calculateRoutes(context(basicRequest(), List.of())));
    }

    @Test
    void serviceFallsBackToHaversineWhenProviderNotConfigured() {
        RouteOptimizationService service = new RouteOptimizationService(
                new OpenRouteServiceProvider(),
                new GoogleMapsDirectionsProvider(),
                new MapboxDirectionsProvider(),
                haversineProvider(),
                new InMemoryRoadClosureService());
        ReflectionTestUtils.invokeMethod(service, "initCache");

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
        RouteOptimizationService service = new RouteOptimizationService(
                new OpenRouteServiceProvider(),
                new GoogleMapsDirectionsProvider(),
                new MapboxDirectionsProvider(),
                haversineProvider(),
                new InMemoryRoadClosureService());
        ReflectionTestUtils.invokeMethod(service, "initCache");

        RouteResponse first = service.calculateRoute(basicRequest());
        RouteResponse second = service.calculateRoute(basicRequest());
        assertSame(first, second, "identical requests should hit the cache");
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
