package com.disaster.ai.model;

/**
 * Normalized, framework-agnostic input for the prediction model. This decouples
 * the model layer from the HTTP/DTO layer so the same shape can be fed to an
 * ML model or an external scoring service later.
 */
public class AnalysisInput {

    private final String disasterType;
    private final String severity;
    private final String location;
    private final double latitude;
    private final double longitude;
    private final Long population;
    private final double infrastructureFactor;
    private final String weatherAlert;

    public AnalysisInput(String disasterType, String severity, String location,
                         double latitude, double longitude, Long population,
                         double infrastructureFactor, String weatherAlert) {
        this.disasterType = disasterType;
        this.severity = severity;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.population = population;
        this.infrastructureFactor = infrastructureFactor;
        this.weatherAlert = weatherAlert;
    }

    public String getDisasterType() { return disasterType; }
    public String getSeverity() { return severity; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Long getPopulation() { return population; }
    public double getInfrastructureFactor() { return infrastructureFactor; }
    public String getWeatherAlert() { return weatherAlert; }

    public boolean hasCoordinates() {
        return latitude != 0.0 || longitude != 0.0;
    }

    public boolean hasLocation() {
        return location != null && !location.isBlank();
    }

    public boolean hasPopulation() {
        return population != null && population > 0;
    }
}
