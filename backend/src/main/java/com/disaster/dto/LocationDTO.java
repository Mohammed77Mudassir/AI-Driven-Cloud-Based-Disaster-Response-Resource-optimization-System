package com.disaster.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single entity position emitted by the Real-Time Monitoring module.
 * The enriched fields feed animated markers, clustering and heat maps
 * on the frontend.
 */
public class LocationDTO {
    private String entityType;
    private Long entityId;
    private String name;
    private double latitude;
    private double longitude;
    private String status;
    private String markerColor;
    private Double heading;
    private Double speed;
    private Integer battery;
    private String severity;
    private Integer capacity;
    private Integer occupancy;
    private String updatedAt;
    private Map<String, Object> extra = new LinkedHashMap<>();

    public LocationDTO() {}

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMarkerColor() { return markerColor; }
    public void setMarkerColor(String markerColor) { this.markerColor = markerColor; }
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    public Integer getBattery() { return battery; }
    public void setBattery(Integer battery) { this.battery = battery; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getOccupancy() { return occupancy; }
    public void setOccupancy(Integer occupancy) { this.occupancy = occupancy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }
    public LocationDTO addExtra(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }
}
