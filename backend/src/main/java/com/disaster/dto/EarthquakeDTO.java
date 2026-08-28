package com.disaster.dto;

/**
 * Public view of a single earthquake event detected by the USGS feed.
 *
 * <p>Risk level is derived from the magnitude using the documented rules:</p>
 * <ul>
 *   <li>Magnitude &lt; 3.5  &rarr; Low</li>
 *   <li>3.5 &le; Magnitude &lt; 5.5 &rarr; Moderate</li>
 *   <li>5.5 &le; Magnitude &lt; 6.5 &rarr; High</li>
 *   <li>Magnitude &ge; 6.5 &rarr; Critical</li>
 * </ul>
 */
public class EarthquakeDTO {

    public static final String SOURCE = "USGS";

    private String id;
    private Double magnitude;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double depth;
    private Long time;
    private String riskLevel;
    private String source;
    private String mapsUrl;

    public static String determineRiskLevel(double magnitude) {
        if (magnitude >= 6.5) {
            return "Critical";
        }
        if (magnitude >= 5.5) {
            return "High";
        }
        if (magnitude >= 3.5) {
            return "Moderate";
        }
        return "Low";
    }

    /** Google Maps deep link to the epicentre. Built at runtime, never hardcoded. */
    public static String buildMapsUrl(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getMagnitude() { return magnitude; }
    public void setMagnitude(Double magnitude) { this.magnitude = magnitude; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getDepth() { return depth; }
    public void setDepth(Double depth) { this.depth = depth; }
    public Long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
}
