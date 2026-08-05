package com.disaster.service.routing;

import com.disaster.dto.RouteRequest;
import com.disaster.dto.routing.RoadClosure;

import java.util.List;

/**
 * Immutable context handed to every {@link RouteProvider}. Carries the
 * validated request, the already-optimized visiting order and the active road
 * closures so each provider can focus on producing candidate routes.
 */
public class RouteContext {
    private final RouteRequest request;
    /** Ordered stops: start, optimized waypoints/destinations, end. */
    private final List<RouteRequest.Point> orderedStops;
    private final List<RoadClosure> closures;

    public RouteContext(RouteRequest request, List<RouteRequest.Point> orderedStops, List<RoadClosure> closures) {
        this.request = request;
        this.orderedStops = orderedStops;
        this.closures = closures == null ? List.of() : closures;
    }

    public RouteRequest getRequest() { return request; }
    public List<RouteRequest.Point> getOrderedStops() { return orderedStops; }
    public List<RoadClosure> getClosures() { return closures; }
}
