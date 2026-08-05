package com.disaster.service;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.RouteResponse;
import com.disaster.dto.routing.OptimizeResponse;
import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RouteOption;
import com.disaster.dto.routing.TrafficInfo;
import com.disaster.service.routing.GoogleMapsDirectionsProvider;
import com.disaster.service.routing.HaversineRouteProvider;
import com.disaster.service.routing.MapboxDirectionsProvider;
import com.disaster.service.routing.OpenRouteServiceProvider;
import com.disaster.service.routing.RoadClosureService;
import com.disaster.service.routing.RouteCache;
import com.disaster.service.routing.RouteContext;
import com.disaster.service.routing.RouteOrderOptimizer;
import com.disaster.service.routing.RouteProvider;
import com.disaster.service.routing.RoutingUnavailableException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the emergency navigation stack. Responsibilities:
 * <ul>
 *   <li>Multi-stop order optimization (TSP heuristic).</li>
 *   <li>Selecting the configured {@link RouteProvider}.</li>
 *   <li>Falling back to {@link HaversineRouteProvider} when the primary
 *       provider is not configured or fails.</li>
 *   <li>TTL-based caching of recent route results.</li>
 *   <li>Assembling the backward-compatible {@link RouteResponse}.</li>
 * </ul>
 */
@Service
public class RouteOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationService.class);

    private final OpenRouteServiceProvider openRouteServiceProvider;
    private final GoogleMapsDirectionsProvider googleMapsDirectionsProvider;
    private final MapboxDirectionsProvider mapboxDirectionsProvider;
    private final HaversineRouteProvider haversineRouteProvider;
    private final RoadClosureService roadClosureService;

    private final Map<String, RouteProvider> providers;
    private RouteCache cache;

    @Value("${app.routing.provider:openrouteservice}")
    private String activeProviderName;

    @Value("${app.routing.cache.ttl-minutes:10}")
    private long cacheTtlMinutes;

    public RouteOptimizationService(OpenRouteServiceProvider openRouteServiceProvider,
                                    GoogleMapsDirectionsProvider googleMapsDirectionsProvider,
                                    MapboxDirectionsProvider mapboxDirectionsProvider,
                                    HaversineRouteProvider haversineRouteProvider,
                                    RoadClosureService roadClosureService) {
        this.openRouteServiceProvider = openRouteServiceProvider;
        this.googleMapsDirectionsProvider = googleMapsDirectionsProvider;
        this.mapboxDirectionsProvider = mapboxDirectionsProvider;
        this.haversineRouteProvider = haversineRouteProvider;
        this.roadClosureService = roadClosureService;
        this.providers = Map.of(
                "openrouteservice", openRouteServiceProvider,
                "google", googleMapsDirectionsProvider,
                "google-maps", googleMapsDirectionsProvider,
                "mapbox", mapboxDirectionsProvider,
                "haversine", haversineRouteProvider);
    }

    @PostConstruct
    void initCache() {
        this.cache = new RouteCache(cacheTtlMinutes * 60_000L);
    }

    // ------------------------------------------------------------------
    // Route calculation
    // ------------------------------------------------------------------

    public RouteResponse calculateRoute(RouteRequest request) {
        List<RouteRequest.Point> ordered = buildOrderedStops(request);
        List<RoadClosure> closures = roadClosureService.getActiveClosures();
        RouteContext context = new RouteContext(request, ordered, closures);

        String cacheKey = cacheKey(request, ordered, closures);
        RouteResponse cached = cache.get(cacheKey, RouteResponse.class);
        if (cached != null) {
            return cached;
        }

        RouteProvider provider = selectProvider();
        List<RouteOption> options;
        String providerName;
        boolean fallback = false;
        String status = "OK";

        try {
            if (!provider.isConfigured()) {
                throw new RoutingUnavailableException(provider.getName() + " is not configured");
            }
            options = provider.calculateRoutes(context);
            providerName = provider.getName();
        } catch (RoutingUnavailableException ex) {
            log.warn("Routing provider '{}' unavailable ({}); using Haversine fallback",
                    provider.getName(), ex.getMessage());
            provider = haversineRouteProvider;
            options = haversineRouteProvider.calculateRoutes(context);
            providerName = "haversine";
            fallback = true;
            status = "FALLBACK";
        }

        if (!fallback && !closures.isEmpty() && request.isAvoidRoadClosures()) {
            status = "BLOCKED_AVOIDED";
        }

        RouteResponse response = buildResponse(request, ordered, context, options, providerName, fallback, status);
        cache.put(cacheKey, response);
        return response;
    }

    // ------------------------------------------------------------------
    // Multi-destination optimization
    // ------------------------------------------------------------------

    public OptimizeResponse optimizeOrder(RouteRequest request) {
        List<RouteRequest.Point> originalOrder = new ArrayList<>();
        if (request.getDestinations() != null) {
            originalOrder.addAll(request.getDestinations());
        }
        originalOrder.add(request.getEnd());

        List<RouteRequest.Point> optimized = RouteOrderOptimizer.optimizeOrder(request.getStart(), originalOrder);

        List<RouteRequest.Point> originalRoute = new ArrayList<>();
        originalRoute.add(request.getStart());
        originalRoute.addAll(originalOrder);

        List<RouteRequest.Point> optimizedRoute = new ArrayList<>();
        optimizedRoute.add(request.getStart());
        optimizedRoute.addAll(optimized);

        double originalKm = RouteOrderOptimizer.totalDistanceKm(originalRoute);
        double optimizedKm = RouteOrderOptimizer.totalDistanceKm(optimizedRoute);
        double savings = Math.max(0, originalKm - optimizedKm);

        OptimizeResponse response = new OptimizeResponse();
        response.setAlgorithm("nearest-neighbour + 2-opt");
        response.setOriginalDistanceKm(Math.round(originalKm * 100.0) / 100.0);
        response.setOptimizedDistanceKm(Math.round(optimizedKm * 100.0) / 100.0);
        response.setSavingsKm(Math.round(savings * 100.0) / 100.0);
        response.setSavingsPercent(originalKm > 0 ? Math.round(savings / originalKm * 10000.0) / 100.0 : 0.0);
        response.setOptimizedTimeMinutes(Math.round(optimizedKm / 40.0 * 60.0 * 100.0) / 100.0);
        response.setOptimizedOrder(optimized);

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < optimized.size(); i++) {
            boolean last = i == optimized.size() - 1;
            labels.add(last ? "Destination" : "Stop " + (i + 1));
        }
        response.setOrderLabels(labels);
        return response;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private RouteProvider selectProvider() {
        String name = activeProviderName == null ? "openrouteservice" : activeProviderName.trim().toLowerCase();
        RouteProvider provider = providers.get(name);
        if (provider == null) {
            log.warn("Unknown routing provider '{}'; using haversine", name);
            return haversineRouteProvider;
        }
        return provider;
    }

    private List<RouteRequest.Point> buildOrderedStops(RouteRequest request) {
        List<RouteRequest.Point> ordered = new ArrayList<>();
        ordered.add(request.getStart());
        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            if (request.isOptimizeOrder()) {
                List<RouteRequest.Point> stops = new ArrayList<>(request.getDestinations());
                stops.add(request.getEnd());
                ordered.addAll(RouteOrderOptimizer.optimizeOrder(request.getStart(), stops));
            } else {
                ordered.addAll(request.getDestinations());
                ordered.add(request.getEnd());
            }
        } else {
            ordered.add(request.getEnd());
        }
        return ordered;
    }

    private RouteResponse buildResponse(RouteRequest request, List<RouteRequest.Point> ordered,
                                        RouteContext context, List<RouteOption> options,
                                        String providerName, boolean fallback, String status) {
        RouteOption selected = selectRoute(request, options);

        RouteResponse response = new RouteResponse();
        response.setProvider(providerName);
        response.setFallback(fallback);
        response.setMode(request.getMode() == null ? "fastest" : request.getMode());
        response.setMessage(fallback
                ? "Routing provider unavailable; used Haversine fallback. Distance and ETA are estimates."
                : "Route calculated successfully using " + providerName);
        response.setAlternatives(options);
        response.setSelectedRoute(selected);
        response.setRoute(selected.getGeometry());
        response.setTotalDistanceKm(selected.getDistanceKm());
        response.setTotalTimeMinutes(selected.getTrafficTimeMinutes());
        response.setTotalTurns(selected.getTurnCount());
        response.setAverageSpeedKmph(selected.getAverageSpeedKmph());
        response.setTrafficAware(selected.isTrafficAware());
        response.setTraffic(buildTraffic(selected));
        response.setRouteStatus(status);
        response.setLastUpdated(Instant.now().toString());
        response.setAvoidedClosures(new ArrayList<>(context.getClosures()));

        List<String> waypoints = new ArrayList<>();
        waypoints.add("Start");
        for (int i = 1; i < ordered.size() - 1; i++) {
            waypoints.add("Stop " + i);
        }
        waypoints.add("Destination");
        response.setWaypoints(waypoints);
        return response;
    }

    private RouteOption selectRoute(RouteRequest request, List<RouteOption> options) {
        String preferred = request.getPreferredRoute();
        if (preferred != null && !preferred.isBlank()) {
            String p = preferred.trim().toLowerCase();
            for (RouteOption option : options) {
                if (option.getPreference().equalsIgnoreCase(p)
                        || option.getLabel().equalsIgnoreCase(p)
                        || String.valueOf(option.getIndex()).equals(p)) {
                    return option;
                }
            }
        }
        return options.get(0);
    }

    private TrafficInfo buildTraffic(RouteOption selected) {
        TrafficInfo traffic = new TrafficInfo();
        traffic.setAvailable(selected.isTrafficAware());
        traffic.setNormalTimeMinutes(selected.getTimeMinutes());
        traffic.setCurrentTimeMinutes(selected.getTrafficTimeMinutes());
        traffic.setDelayMinutes(selected.getDelayMinutes());
        if (selected.isTrafficAware()) {
            traffic.setLevel(selected.getDelayMinutes() < 5 ? "LOW"
                    : selected.getDelayMinutes() < 15 ? "MODERATE" : "HEAVY");
            traffic.setMessage("Estimated arrival time includes live traffic conditions");
        } else {
            traffic.setLevel("UNKNOWN");
            traffic.setMessage("Standard routing in use - real-time traffic data is not available for this provider");
        }
        return traffic;
    }

    private String cacheKey(RouteRequest request, List<RouteRequest.Point> ordered, List<RoadClosure> closures) {
        StringBuilder sb = new StringBuilder();
        for (RouteRequest.Point p : ordered) {
            sb.append(p.getLatitude()).append(',').append(p.getLongitude()).append(';');
        }
        sb.append(request.getMode() == null ? "fastest" : request.getMode()).append('|');
        sb.append(request.isEmergencyMode()).append('|');
        sb.append(request.isAvoidRoadClosures()).append('|');
        sb.append(request.getAlternativesCount()).append('|');
        sb.append(request.getProfile()).append('|');
        sb.append(activeProviderName).append('|');
        List<String> closureKeys = closures.stream()
                .map(c -> c.getId() + "@" + c.getLatitude() + "," + c.getLongitude() + ":" + c.getType())
                .sorted()
                .toList();
        sb.append(closureKeys);
        return sha256Hex(sb.toString());
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            return String.valueOf(input.hashCode());
        }
    }
}
