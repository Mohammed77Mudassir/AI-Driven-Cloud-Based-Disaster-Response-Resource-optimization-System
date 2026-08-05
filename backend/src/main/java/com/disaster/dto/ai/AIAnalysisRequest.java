package com.disaster.dto.ai;

import jakarta.validation.constraints.NotBlank;

/**
 * Input contract for the AI analysis engine. Kept intentionally small and
 * ML-friendly so a future machine-learning model can consume the same shape.
 */
public class AIAnalysisRequest {

    @NotBlank
    private String disasterType;

    @NotBlank
    private String severity;

    private String location;
    private double latitude;
    private double longitude;

    /** Optional estimated population in the affected zone. Derived when omitted. */
    private Long population;

    /** Optional infrastructure resilience factor between 0 (fragile) and 1 (robust). */
    private Double infrastructureFactor;

    /** Optional active weather alert text (e.g. "Heavy rainfall warning"). */
    private String weatherAlert;

    public AIAnalysisRequest() {}

    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }
    public Double getInfrastructureFactor() { return infrastructureFactor; }
    public void setInfrastructureFactor(Double infrastructureFactor) { this.infrastructureFactor = infrastructureFactor; }
    public String getWeatherAlert() { return weatherAlert; }
    public void setWeatherAlert(String weatherAlert) { this.weatherAlert = weatherAlert; }
}
