package com.disaster.dto.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * A single candidate route returned by a routing provider (or by the Haversine
 * fallback engine). Carries distance, ETA, traffic-aware timings, geometry,
 * turn-by-turn steps and a summary so the frontend can compare and render
 * multiple alternatives.
 */
public class RouteOption {
    private int index;
    /** Human friendly label, e.g. "Fastest Route", "Shortest Route", "Alternative 1". */
    private String label;
    /** Internal preference key: "fastest", "shortest" or "alternative". */
    private String preference;
    private double distanceKm;
    private double timeMinutes;
    /** ETA including traffic delay (equals timeMinutes when no traffic data). */
    private double trafficTimeMinutes;
    private double delayMinutes;
    private boolean trafficAware;
    private int turnCount;
    /** Decoded polyline geometry as [lat, lng] pairs. */
    private List<double[]> geometry;
    /** Turn-by-turn instructions, e.g. "Turn left onto Main Street". */
    private List<String> steps;
    private String summary;
    private boolean recommended;
    private boolean blockedRoadsAvoided;
    private double averageSpeedKmph;

    public RouteOption() {
        this.geometry = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getTimeMinutes() { return timeMinutes; }
    public void setTimeMinutes(double timeMinutes) { this.timeMinutes = timeMinutes; }
    public double getTrafficTimeMinutes() { return trafficTimeMinutes; }
    public void setTrafficTimeMinutes(double trafficTimeMinutes) { this.trafficTimeMinutes = trafficTimeMinutes; }
    public double getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(double delayMinutes) { this.delayMinutes = delayMinutes; }
    public boolean isTrafficAware() { return trafficAware; }
    public void setTrafficAware(boolean trafficAware) { this.trafficAware = trafficAware; }
    public int getTurnCount() { return turnCount; }
    public void setTurnCount(int turnCount) { this.turnCount = turnCount; }
    public List<double[]> getGeometry() { return geometry; }
    public void setGeometry(List<double[]> geometry) { this.geometry = geometry; }
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public boolean isRecommended() { return recommended; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }
    public boolean isBlockedRoadsAvoided() { return blockedRoadsAvoided; }
    public void setBlockedRoadsAvoided(boolean blockedRoadsAvoided) { this.blockedRoadsAvoided = blockedRoadsAvoided; }
    public double getAverageSpeedKmph() { return averageSpeedKmph; }
    public void setAverageSpeedKmph(double averageSpeedKmph) { this.averageSpeedKmph = averageSpeedKmph; }
}
