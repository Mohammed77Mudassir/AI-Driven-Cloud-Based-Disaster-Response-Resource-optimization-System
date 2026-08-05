package com.disaster.dto.routing;

import java.util.List;

/**
 * Road closure / obstruction record used to make the routing engine avoid
 * impassable roads. Provides a clean, provider-agnostic interface so that
 * live road-closure feeds can be plugged in later; today closures are managed
 * in-memory through the {@code RoadClosureService}.
 */
public class RoadClosure {
    private String id;
    /** CLOSED, BLOCKED, FLOODED, LANDSLIDE or ACCIDENT. */
    private String type;
    /** ACTIVE or CLEARED. Only ACTIVE closures are avoided. */
    private String status;
    private double latitude;
    private double longitude;
    private String locationName;
    private String description;
    /** Radius (km) around the closure that routing tries to avoid. */
    private double avoidanceRadiusKm;
    /** ISO-8601 timestamp of when the closure was reported. */
    private String reportedAt;

    public RoadClosure() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getAvoidanceRadiusKm() { return avoidanceRadiusKm; }
    public void setAvoidanceRadiusKm(double avoidanceRadiusKm) { this.avoidanceRadiusKm = avoidanceRadiusKm; }
    public String getReportedAt() { return reportedAt; }
    public void setReportedAt(String reportedAt) { this.reportedAt = reportedAt; }
}
