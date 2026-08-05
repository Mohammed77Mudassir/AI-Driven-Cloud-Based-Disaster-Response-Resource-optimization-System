package com.disaster.dto;

import com.disaster.dto.routing.RoadClosure;
import com.disaster.dto.routing.RouteOption;
import com.disaster.dto.routing.TrafficInfo;

import java.util.List;

/**
 * Result of a route calculation. Keeps the original fields
 * ({@code totalDistance}, {@code estimatedTime}, {@code route}, ...) for
 * backward compatibility and adds the enterprise navigation fields:
 * alternatives, traffic, provider metadata and route status.
 */
public class RouteResponse {
    private double totalDistanceKm;
    private double totalTimeMinutes;
    private List<double[]> route;
    private List<String> waypoints;
    private String mode;
    private String message;

    /** Provider that produced the route: "openrouteservice", "haversine", ... */
    private String provider;
    /** True when the primary provider was unavailable and Haversine fallback was used. */
    private boolean fallback;
    /** Whether the provider returned traffic-aware ETAs. */
    private boolean trafficAware;
    private TrafficInfo traffic;
    /** The route the user preferred (or the recommended one). */
    private RouteOption selectedRoute;
    /** All candidate routes, including the selected one. */
    private List<RouteOption> alternatives;
    private int totalTurns;
    private double averageSpeedKmph;
    /** Route status: "OK", "FALLBACK", "BLOCKED_AVOIDED", "TRAFFIC". */
    private String routeStatus;
    private String lastUpdated;
    /** Active road closures that were avoided while building this route. */
    private List<RoadClosure> avoidedClosures;

    public RouteResponse() {}

    public double getTotalDistance() { return totalDistanceKm; }
    public double getEstimatedTime() { return totalTimeMinutes; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public double getTotalTimeMinutes() { return totalTimeMinutes; }
    public void setTotalTimeMinutes(double totalTimeMinutes) { this.totalTimeMinutes = totalTimeMinutes; }
    public List<double[]> getRoute() { return route; }
    public void setRoute(List<double[]> route) { this.route = route; }
    public List<String> getWaypoints() { return waypoints; }
    public void setWaypoints(List<String> waypoints) { this.waypoints = waypoints; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public boolean isFallback() { return fallback; }
    public void setFallback(boolean fallback) { this.fallback = fallback; }
    public boolean isTrafficAware() { return trafficAware; }
    public void setTrafficAware(boolean trafficAware) { this.trafficAware = trafficAware; }
    public TrafficInfo getTraffic() { return traffic; }
    public void setTraffic(TrafficInfo traffic) { this.traffic = traffic; }
    public RouteOption getSelectedRoute() { return selectedRoute; }
    public void setSelectedRoute(RouteOption selectedRoute) { this.selectedRoute = selectedRoute; }
    public List<RouteOption> getAlternatives() { return alternatives; }
    public void setAlternatives(List<RouteOption> alternatives) { this.alternatives = alternatives; }
    public int getTotalTurns() { return totalTurns; }
    public void setTotalTurns(int totalTurns) { this.totalTurns = totalTurns; }
    public double getAverageSpeedKmph() { return averageSpeedKmph; }
    public void setAverageSpeedKmph(double averageSpeedKmph) { this.averageSpeedKmph = averageSpeedKmph; }
    public String getRouteStatus() { return routeStatus; }
    public void setRouteStatus(String routeStatus) { this.routeStatus = routeStatus; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<RoadClosure> getAvoidedClosures() { return avoidedClosures; }
    public void setAvoidedClosures(List<RoadClosure> avoidedClosures) { this.avoidedClosures = avoidedClosures; }
}
