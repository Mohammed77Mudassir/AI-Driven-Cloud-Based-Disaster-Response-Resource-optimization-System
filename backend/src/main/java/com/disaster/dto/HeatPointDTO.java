package com.disaster.dto;

/**
 * A single weighted point for the real-time heat map overlay.
 * Intensity is a 0..1 value used by the frontend to scale the gradient.
 */
public class HeatPointDTO {
    private double latitude;
    private double longitude;
    private double intensity;
    private String entityType;

    public HeatPointDTO() {}

    public HeatPointDTO(double latitude, double longitude, double intensity, String entityType) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.intensity = intensity;
        this.entityType = entityType;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) { this.intensity = intensity; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
}
