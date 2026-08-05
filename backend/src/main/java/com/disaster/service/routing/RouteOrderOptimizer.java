package com.disaster.service.routing;

import com.disaster.dto.RouteRequest;
import com.disaster.geo.GeoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Heuristic optimizer for the "visit many places efficiently" use case.
 * Runs a nearest-neighbour construction followed by a 2-opt improvement,
 * producing a near-optimal visiting order from a start point through a set of
 * intermediate stops, ending at the final stop. Deterministic and fast enough
 * for typical rescue mission stop counts (O(n^2) + 2-opt).
 */
public final class RouteOrderOptimizer {

    private RouteOrderOptimizer() {}

    /**
     * Optimize the visiting order of {@code stops} (the intermediate
     * destinations plus the final destination) starting from {@code start}.
     */
    public static List<RouteRequest.Point> optimizeOrder(RouteRequest.Point start, List<RouteRequest.Point> stops) {
        if (stops == null || stops.isEmpty()) return new ArrayList<>();
        if (stops.size() == 1) return new ArrayList<>(stops);

        List<RouteRequest.Point> remaining = new ArrayList<>(stops);
        List<RouteRequest.Point> order = new ArrayList<>(stops.size());

        RouteRequest.Point current = start;
        while (!remaining.isEmpty()) {
            int bestIdx = 0;
            double bestDist = Double.MAX_VALUE;
            for (int i = 0; i < remaining.size(); i++) {
                double d = distance(current, remaining.get(i));
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }
            order.add(remaining.remove(bestIdx));
            current = order.get(order.size() - 1);
        }

        return twoOpt(order);
    }

    /** Total great-circle distance of a stop sequence in km. */
    public static double totalDistanceKm(List<RouteRequest.Point> points) {
        if (points == null || points.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 1; i < points.size(); i++) {
            total += distance(points.get(i - 1), points.get(i));
        }
        return total;
    }

    private static double distance(RouteRequest.Point a, RouteRequest.Point b) {
        return GeoUtils.distanceKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }

    /**
     * 2-opt improvement: repeatedly reverse any sub-tour that shortens the
     * total distance until no further improvement is found.
     */
    private static List<RouteRequest.Point> twoOpt(List<RouteRequest.Point> order) {
        List<RouteRequest.Point> route = new ArrayList<>(order);
        boolean improved = true;
        int iterations = 0;
        int maxIterations = 100 * route.size();

        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;
            for (int i = 1; i < route.size() - 1; i++) {
                for (int j = i + 1; j < route.size(); j++) {
                    double before = distance(route.get(i - 1), route.get(i))
                            + (j + 1 < route.size() ? distance(route.get(j), route.get(j + 1)) : 0.0);
                    double after = distance(route.get(i - 1), route.get(j))
                            + (j + 1 < route.size() ? distance(route.get(i), route.get(j + 1)) : 0.0);
                    if (after < before - 1e-9) {
                        reverse(route, i, j);
                        improved = true;
                    }
                }
            }
        }
        return route;
    }

    private static void reverse(List<RouteRequest.Point> list, int from, int to) {
        while (from < to) {
            RouteRequest.Point tmp = list.get(from);
            list.set(from, list.get(to));
            list.set(to, tmp);
            from++;
            to--;
        }
    }
}
