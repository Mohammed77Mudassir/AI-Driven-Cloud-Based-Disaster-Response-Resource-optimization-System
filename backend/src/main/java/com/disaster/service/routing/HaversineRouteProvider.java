package com.disaster.service.routing;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RouteOption;
import com.disaster.geo.GeoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Always-available offline routing engine used as a fallback when no external
 * provider is configured or reachable. Computes great-circle routes between
 * the ordered stops, detours around active road closures, and produces up to
 * three alternatives (fastest / shortest / an offset "alternative" leg) so the
 * comparison UI keeps working even without connectivity.
 */
@Service
public class HaversineRouteProvider implements RouteProvider {

    @Value("${app.routing.normal-speed-kmph:40}")
    private double normalSpeedKmph;

    @Value("${app.routing.emergency-speed-kmph:55}")
    private double emergencySpeedKmph;

    @Override
    public String getName() {
        return "haversine";
    }

    @Override
    public boolean isConfigured() {
        return true;
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
    public List<RouteOption> calculateRoutes(RouteContext context) {
        List<RouteRequest.Point> stops = context.getOrderedStops();
        List<RoadClosure> closures = context.getRequest().isAvoidRoadClosures()
                ? context.getClosures() : List.of();
        boolean emergency = context.getRequest().isEmergencyMode();
        double speed = emergency ? emergencySpeedKmph : normalSpeedKmph;

        List<RouteOption> options = new ArrayList<>();

        List<double[]> direct = stopsToPoints(stops);
        List<double[]> geometry = applyDetours(direct, closures);

        RouteOption fastest = buildOption(0, "Fastest Route", "fastest", geometry,
                speed, true, !closures.isEmpty());
        options.add(fastest);

        int count = context.getRequest().getAlternativesCount();
        if (count >= 2) {
            RouteOption shortest = buildOption(1, "Shortest Route", "shortest",
                    applyDetours(direct, closures), normalSpeedKmph, false,
                    !closures.isEmpty());
            options.add(shortest);
        }
        if (count >= 3) {
            List<double[]> offset = offsetDetourGeometry(direct);
            RouteOption alternative = buildOption(2, "Alternative Route", "alternative",
                    applyDetours(offset, closures), normalSpeedKmph, false, !closures.isEmpty());
            options.add(alternative);
        }
        return options;
    }

    private RouteOption buildOption(int index, String label, String preference,
                                    List<double[]> geometry, double speed,
                                    boolean recommended, boolean blockedAvoided) {
        double distance = routeLengthKm(geometry);
        double minutes = (distance / speed) * 60.0;
        int turns = countTurns(geometry);

        RouteOption option = new RouteOption();
        option.setIndex(index);
        option.setLabel(label);
        option.setPreference(preference);
        option.setDistanceKm(round(distance));
        option.setTimeMinutes(round(minutes));
        option.setTrafficTimeMinutes(round(minutes));
        option.setDelayMinutes(0.0);
        option.setTrafficAware(false);
        option.setTurnCount(turns);
        option.setGeometry(geometry);
        option.setSteps(buildSteps(geometry));
        option.setSummary(preference.equals("alternative") ? "Via offset bypass" : "Direct offline route");
        option.setRecommended(recommended);
        option.setBlockedRoadsAvoided(blockedAvoided);
        option.setAverageSpeedKmph(round(speed));
        return option;
    }

    private List<double[]> stopsToPoints(List<RouteRequest.Point> stops) {
        List<double[]> points = new ArrayList<>();
        for (RouteRequest.Point p : stops) {
            points.add(new double[]{p.getLatitude(), p.getLongitude()});
        }
        return points;
    }

    /** Insert detour arcs around active closures that sit close to each leg. */
    private List<double[]> applyDetours(List<double[]> geometry, List<RoadClosure> closures) {
        if (closures.isEmpty()) return geometry;

        List<double[]> result = new ArrayList<>();
        result.add(geometry.get(0));
        for (int i = 1; i < geometry.size(); i++) {
            double[] a = geometry.get(i - 1);
            double[] b = geometry.get(i);
            for (RoadClosure c : closures) {
                if (!isActive(c)) continue;
                double[] center = {c.getLatitude(), c.getLongitude()};
                double[] foot = closestPointOnSegment(a, b, center);
                double clearance = c.getAvoidanceRadiusKm() + 0.5;
                if (distance(foot, center) <= clearance) {
                    double heading = GeoUtils.bearing(a[0], a[1], b[0], b[1]);
                    for (int k = 0; k <= 8; k++) {
                        double t = k / 8.0;
                        double angle = heading + 90 + 180 * t;
                        double[] p = GeoUtils.destination(foot[0], foot[1], angle, clearance);
                        result.add(p);
                    }
                }
            }
            result.add(b);
        }
        return result;
    }

    /** Generate a visually distinct "alternative" leg by offsetting each midpoint. */
    private List<double[]> offsetDetourGeometry(List<double[]> geometry) {
        List<double[]> out = new ArrayList<>();
        out.add(geometry.get(0));
        for (int i = 1; i < geometry.size(); i++) {
            double[] a = geometry.get(i - 1);
            double[] b = geometry.get(i);
            double[] mid = GeoUtils.interpolate(a[0], a[1], b[0], b[1], 0.5);
            double legKm = distance(a, b);
            double offsetKm = Math.max(1.2, legKm * 0.12);
            double heading = GeoUtils.bearing(a[0], a[1], b[0], b[1]);
            double[] off = GeoUtils.destination(mid[0], mid[1], heading + 90, offsetKm);
            out.add(off);
            out.add(b);
        }
        return out;
    }

    private List<String> buildSteps(List<double[]> geometry) {
        List<String> steps = new ArrayList<>();
        if (geometry.size() < 2) return steps;
        steps.add("Depart heading " + direction(GeoUtils.bearing(geometry.get(0)[0], geometry.get(0)[1],
                geometry.get(1)[0], geometry.get(1)[1])));
        for (int i = 1; i < geometry.size() - 1; i++) {
            double[] a = geometry.get(i - 1);
            double[] b = geometry.get(i);
            double[] c = geometry.get(i + 1);
            double b1 = GeoUtils.bearing(a[0], a[1], b[0], b[1]);
            double b2 = GeoUtils.bearing(b[0], b[1], c[0], c[1]);
            double delta = Math.abs(b2 - b1);
            if (delta > 180) delta = 360 - delta;
            if (delta > 30) {
                steps.add("Turn " + (delta > 90 ? "sharply " : "") + direction(b2) + " after "
                        + round(distance(a, b)) + " km");
            } else {
                steps.add("Continue " + direction(b2) + " for " + round(distance(a, b)) + " km");
            }
        }
        steps.add("Arrive at destination");
        return steps;
    }

    private int countTurns(List<double[]> geometry) {
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

    private double routeLengthKm(List<double[]> geometry) {
        double total = 0;
        for (int i = 1; i < geometry.size(); i++) {
            total += distance(geometry.get(i - 1), geometry.get(i));
        }
        return total;
    }

    private boolean isActive(RoadClosure c) {
        return c.getStatus() == null || c.getStatus().equalsIgnoreCase("ACTIVE");
    }

    /** Closest point on segment AB to point P (in degrees). */
    private double[] closestPointOnSegment(double[] a, double[] b, double[] p) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-12) return a;
        double t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        return new double[]{a[0] + t * dx, a[1] + t * dy};
    }

    private double distance(double[] a, double[] b) {
        return GeoUtils.distanceKm(a[0], a[1], b[0], b[1]);
    }

    private String direction(double bearing) {
        String[] dirs = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};
        int idx = (int) Math.round(((bearing % 360) + 360) % 360 / 45.0) % 8;
        return dirs[idx];
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
