package com.disaster.service.routing;

import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RouteOption;
import com.disaster.geo.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Primary routing provider backed by the OpenRouteService Directions API
 * (free tier; an API key is recommended for production). Produces real road
 * routes with geometry, turn-by-turn instructions and multiple alternatives.
 * Attempts to avoid active road closures through the {@code avoid_polygons}
 * option; if the provider rejects that, it transparently retries without it.
 *
 * <p>All transport concerns (timeouts, retries, the API key header) live in
 * {@link OpenRouteServiceClient}; this class only builds the request body and
 * parses the response into {@link RouteOption}s.</p>
 *
 * <p>OpenRouteService does not expose real-time traffic, so this provider
 * reports traffic as unavailable; the response clearly marks standard routing
 * in that case.</p>
 */
@Service
public class OpenRouteServiceProvider implements RouteProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenRouteServiceProvider.class);

    private static final Set<String> ALLOWED_PROFILES = Set.of(
            "driving-car", "driving-hgv", "cycling-regular", "foot-walking");

    private final OpenRouteServiceClient client;
    private final PolylineCodec polylineCodec = new PolylineCodec();

    public OpenRouteServiceProvider(OpenRouteServiceClient client) {
        this.client = client;
    }

    @Override
    public String getName() {
        return "openrouteservice";
    }

    @Override
    public boolean isConfigured() {
        return client.isConfigured();
    }

    @Override
    public boolean supportsTraffic() {
        return false;
    }

    @Override
    public boolean supportsAlternatives() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RouteOption> calculateRoutes(RouteContext context) {
        if (!isConfigured()) {
            throw new RoutingUnavailableException("OpenRouteService API key is not configured");
        }

        Map<String, Object> baseBody = buildBody(context);
        String profile = resolveProfile(context.getRequest().getProfile());
        boolean avoidClosures = context.getRequest().isAvoidRoadClosures()
                && !context.getClosures().isEmpty();

        Map<String, Object> response = null;
        if (avoidClosures) {
            Map<String, Object> avoidPolygons = buildAvoidPolygons(context);
            if (avoidPolygons != null) {
                Map<String, Object> withAvoidance = new LinkedHashMap<>(baseBody);
                withAvoidance.put("avoid_polygons", avoidPolygons);
                try {
                    response = client.requestDirections(profile, withAvoidance);
                    log.info("Route calculated with {} avoidance polygon(s)", context.getClosures().size());
                } catch (RoutingUnavailableException ex) {
                    log.warn("OpenRouteService rejected avoid_polygons ({}); retrying without it", ex.getMessage());
                }
            }
        }

        if (response == null) {
            response = client.requestDirections(profile, baseBody);
        }

        if (response == null || response.get("routes") == null) {
            throw new RoutingUnavailableException("OpenRouteService returned no routes");
        }

        List<Map<String, Object>> rawRoutes = (List<Map<String, Object>>) response.get("routes");
        boolean preferenceShortest = "shortest".equalsIgnoreCase(context.getRequest().getMode());
        List<RouteOption> options = new ArrayList<>();

        for (int i = 0; i < rawRoutes.size(); i++) {
            options.add(parseRoute(rawRoutes.get(i), i, preferenceShortest));
        }

        if (options.isEmpty()) {
            throw new RoutingUnavailableException("OpenRouteService returned an empty route list");
        }
        return options;
    }

    private Map<String, Object> buildBody(RouteContext context) {
        var request = context.getRequest();
        String profile = resolveProfile(request.getProfile());

        List<List<Double>> coordinates = new ArrayList<>();
        for (var p : context.getOrderedStops()) {
            coordinates.add(List.of(p.getLongitude(), p.getLatitude()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("coordinates", coordinates);
        body.put("profile", profile);
        body.put("preference", "shortest".equalsIgnoreCase(request.getMode()) ? "shortest" : "fastest");
        body.put("units", "km");
        body.put("instructions", true);
        body.put("geometry", true);
        body.put("geometry_format", "encodedpolyline");

        int alternatives = request.getAlternativesCount();
        if (alternatives > 1) {
            Map<String, Object> alt = new LinkedHashMap<>();
            alt.put("target_count", Math.min(alternatives - 1, 3));
            alt.put("weight_factor", 1.4);
            body.put("alternative_routes", alt);
        }

        Map<String, Object> options = new LinkedHashMap<>();
        if (request.isEmergencyMode()) {
            options.put("avoid_features", List.of("fords", "steps"));
        }
        if (!options.isEmpty()) {
            body.put("options", options);
        }

        return body;
    }

    private String resolveProfile(String profile) {
        if (profile == null || !ALLOWED_PROFILES.contains(profile)) {
            return "driving-car";
        }
        return profile;
    }

    /** Polygon around each active closure so ORS can route around them. */
    private Map<String, Object> buildAvoidPolygons(RouteContext context) {
        List<List<List<List<Double>>>> polygons = new ArrayList<>();
        for (RoadClosure c : context.getClosures()) {
            if (c.getStatus() != null && !c.getStatus().equalsIgnoreCase("ACTIVE")) continue;
            polygons.add(buildSquare(c.getLatitude(), c.getLongitude(), c.getAvoidanceRadiusKm()));
        }
        if (polygons.isEmpty()) return null;

        Map<String, Object> poly = new LinkedHashMap<>();
        poly.put("type", "MultiPolygon");
        poly.put("coordinates", polygons);
        return poly;
    }

    /** A small square polygon around a point, used as an ORS avoidance zone. */
    private List<List<List<Double>>> buildSquare(double lat, double lon, double radiusKm) {
        double dLat = radiusKm / 111.32;
        double dLon = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));
        double[][] corners = {
                {lon - dLon, lat - dLat}, {lon + dLon, lat - dLat},
                {lon + dLon, lat + dLat}, {lon - dLon, lat + dLat},
                {lon - dLon, lat - dLat}
        };
        List<List<List<Double>>> ring = new ArrayList<>();
        List<List<Double>> polygon = new ArrayList<>();
        for (double[] c : corners) {
            polygon.add(List.of(c[0], c[1]));
        }
        ring.add(polygon);
        return ring;
    }

    @SuppressWarnings("unchecked")
    private RouteOption parseRoute(Map<String, Object> raw, int index, boolean preferenceShortest) {
        RouteOption option = new RouteOption();
        option.setIndex(index);
        option.setPreference(index == 0 ? (preferenceShortest ? "shortest" : "fastest") : "alternative");
        option.setLabel(index == 0
                ? (preferenceShortest ? "Shortest Route" : "Fastest Route")
                : "Alternative " + index);
        option.setTrafficAware(false);
        option.setDelayMinutes(0.0);

        Map<String, Object> summary = (Map<String, Object>) raw.get("summary");
        if (summary != null) {
            double distance = asDouble(summary.get("distance"));
            if (distance > 100000) distance = distance / 1000.0; // metres -> km
            double durationSec = asDouble(summary.get("duration"));
            double minutes = durationSec / 60.0;
            option.setDistanceKm(round(distance));
            option.setTimeMinutes(round(minutes));
            option.setTrafficTimeMinutes(round(minutes));
            option.setAverageSpeedKmph(round(minutes > 0 ? distance / (minutes / 60.0) : 0));
        }

        String encoded = (String) raw.get("geometry");
        if (encoded != null && !encoded.isBlank()) {
            List<double[]> geometry = polylineCodec.decode(encoded);
            option.setGeometry(geometry);
            if (option.getTurnCount() == 0) {
                option.setTurnCount(countTurnsFromGeometry(geometry));
            }
        }

        List<String> steps = new ArrayList<>();
        List<String> roadNames = new ArrayList<>();
        List<Map<String, Object>> segments = (List<Map<String, Object>>) raw.get("segments");
        if (segments != null) {
            for (Map<String, Object> segment : segments) {
                List<Map<String, Object>> rawSteps = (List<Map<String, Object>>) segment.get("steps");
                if (rawSteps == null) continue;
                for (Map<String, Object> step : rawSteps) {
                    Object instruction = step.get("instruction");
                    if (instruction != null) steps.add(String.valueOf(instruction));
                    Object name = step.get("name");
                    if (name != null && !String.valueOf(name).isBlank() && !roadNames.contains(String.valueOf(name))) {
                        roadNames.add(String.valueOf(name));
                    }
                }
            }
        }
        option.setSteps(steps);
        option.setSummary(buildSummary(roadNames, steps));
        option.setRecommended(index == 0);
        return option;
    }

    private String buildSummary(List<String> roadNames, List<String> steps) {
        if (!roadNames.isEmpty()) {
            List<String> top = roadNames.size() > 3 ? roadNames.subList(0, 3) : roadNames;
            return "Via " + String.join(", ", top);
        }
        if (steps.isEmpty()) return "Calculated route";
        return steps.get(0);
    }

    private int countTurnsFromGeometry(List<double[]> geometry) {
        int turns = 0;
        for (int i = 1; i < geometry.size() - 1; i++) {
            double b1 = GeoUtils.bearing(geometry.get(i - 1)[0], geometry.get(i - 1)[1],
                    geometry.get(i)[0], geometry.get(i)[1]);
            double b2 = GeoUtils.bearing(geometry.get(i)[0], geometry.get(i)[1],
                    geometry.get(i + 1)[0], geometry.get(i + 1)[1]);
            double delta = Math.abs(b2 - b1);
            if (delta > 180) delta = 360 - delta;
            if (delta > 25) turns++;
        }
        return turns;
    }

    private double asDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        try {
            return value == null ? 0.0 : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
