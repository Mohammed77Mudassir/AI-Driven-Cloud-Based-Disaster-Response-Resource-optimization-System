package com.disaster.dto;

/**
 * Response for offline distance / ETA queries used by the route and
 * monitoring panels.
 */
public class GeoQueryResponse {
    private double fromLat;
    private double fromLng;
    private double toLat;
    private double toLng;
    private double distanceKm;
    private double etaMinutes;
    private double speedKmph;
    private double initialBearing;
    private String message;

    public GeoQueryResponse() {}

    public double getFromLat() { return fromLat; }
    public void setFromLat(double fromLat) { this.fromLat = fromLat; }
    public double getFromLng() { return fromLng; }
    public void setFromLng(double fromLng) { this.fromLng = fromLng; }
    public double getToLat() { return toLat; }
    public void setToLat(double toLat) { this.toLat = toLat; }
    public double getToLng() { return toLng; }
    public void setToLng(double toLng) { this.toLng = toLng; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(double etaMinutes) { this.etaMinutes = etaMinutes; }
    public double getSpeedKmph() { return speedKmph; }
    public void setSpeedKmph(double speedKmph) { this.speedKmph = speedKmph; }
    public double getInitialBearing() { return initialBearing; }
    public void setInitialBearing(double initialBearing) { this.initialBearing = initialBearing; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
