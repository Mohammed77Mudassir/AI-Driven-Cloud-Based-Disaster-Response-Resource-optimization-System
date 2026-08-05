package com.disaster.dto.routing;

/**
 * Traffic information for a calculated route.
 * When the routing provider supports traffic, {@code available} is true and
 * the current ETA vs normal ETA difference is reported as delay. When the
 * provider does not support traffic, {@code available} is false and the
 * frontend clearly indicates that standard (non-traffic) routing is used.
 */
public class TrafficInfo {
    private boolean available;
    private double normalTimeMinutes;
    private double currentTimeMinutes;
    private double delayMinutes;
    /** "LOW", "MODERATE", "HEAVY" or "UNKNOWN". */
    private String level;
    private String message;

    public TrafficInfo() {}

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public double getNormalTimeMinutes() { return normalTimeMinutes; }
    public void setNormalTimeMinutes(double normalTimeMinutes) { this.normalTimeMinutes = normalTimeMinutes; }
    public double getCurrentTimeMinutes() { return currentTimeMinutes; }
    public void setCurrentTimeMinutes(double currentTimeMinutes) { this.currentTimeMinutes = currentTimeMinutes; }
    public double getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(double delayMinutes) { this.delayMinutes = delayMinutes; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
