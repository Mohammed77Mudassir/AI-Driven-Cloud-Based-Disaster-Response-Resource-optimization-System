package com.disaster.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RouteRequest {
    @Valid
    @NotNull(message = "Start point is required")
    private Point start;
    @Valid
    @NotNull(message = "End point is required")
    private Point end;
    @Valid
    private List<Point> destinations;
    /** Routing preference: "fastest" or "shortest". Kept for backward compatibility. */
    private String mode;
    /** Number of alternative routes to generate (0-4). Defaults to 2. */
    private Integer alternativesCount;
    /** When true, prioritises main roads, minimises time and uses emergency speeds. */
    private boolean emergencyMode;
    /** When true, the engine routes around active road closures. Defaults to true. */
    private Boolean avoidRoadClosures;
    /** Preferred route label: "fastest", "shortest", "alternative" or a route index. */
    private String preferredRoute;
    /** Transport profile for the routing provider, e.g. "driving-car". */
    private String profile;
    /** When true (default), intermediate destinations are re-ordered to minimise travel. */
    private Boolean optimizeOrder;

    public RouteRequest() {}

    public Point getStart() { return start; }
    public void setStart(Point start) { this.start = start; }
    public Point getEnd() { return end; }
    public void setEnd(Point end) { this.end = end; }
    public List<Point> getDestinations() { return destinations; }
    public void setDestinations(List<Point> destinations) { this.destinations = destinations; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Integer getAlternativesCount() { return alternativesCount == null ? 2 : alternativesCount; }
    public void setAlternativesCount(Integer alternativesCount) { this.alternativesCount = alternativesCount; }
    public boolean isEmergencyMode() { return emergencyMode; }
    public void setEmergencyMode(boolean emergencyMode) { this.emergencyMode = emergencyMode; }
    public boolean isAvoidRoadClosures() { return avoidRoadClosures == null || avoidRoadClosures; }
    public void setAvoidRoadClosures(Boolean avoidRoadClosures) { this.avoidRoadClosures = avoidRoadClosures; }
    public String getPreferredRoute() { return preferredRoute; }
    public void setPreferredRoute(String preferredRoute) { this.preferredRoute = preferredRoute; }
    public String getProfile() { return profile == null || profile.isBlank() ? "driving-car" : profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public boolean isOptimizeOrder() { return optimizeOrder == null || optimizeOrder; }
    public void setOptimizeOrder(Boolean optimizeOrder) { this.optimizeOrder = optimizeOrder; }

    public double getStartLatitude() { return start == null ? 0 : start.getLatitude(); }
    public double getStartLongitude() { return start == null ? 0 : start.getLongitude(); }
    public double getEndLatitude() { return end == null ? 0 : end.getLatitude(); }
    public double getEndLongitude() { return end == null ? 0 : end.getLongitude(); }

    public static class Point {
        private double latitude;
        private double longitude;

        public Point() {}

        public Point(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}
