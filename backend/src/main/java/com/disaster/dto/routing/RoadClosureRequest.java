package com.disaster.dto.routing;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for reporting a road closure. Validated at the controller boundary.
 */
public class RoadClosureRequest {

    @NotBlank(message = "Closure type is required")
    private String type;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude out of range")
    @DecimalMax(value = "90.0", message = "Latitude out of range")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude out of range")
    @DecimalMax(value = "180.0", message = "Longitude out of range")
    private Double longitude;

    private String locationName;
    private String description;

    @DecimalMin(value = "0.1", message = "Avoidance radius must be at least 0.1 km")
    @DecimalMax(value = "10.0", message = "Avoidance radius must be at most 10 km")
    private Double avoidanceRadiusKm;

    public RoadClosureRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getAvoidanceRadiusKm() { return avoidanceRadiusKm == null ? 1.5 : avoidanceRadiusKm; }
    public void setAvoidanceRadiusKm(Double avoidanceRadiusKm) { this.avoidanceRadiusKm = avoidanceRadiusKm; }
}
