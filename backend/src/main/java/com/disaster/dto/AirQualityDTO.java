package com.disaster.dto;

/**
 * Public view of a single air-quality station inside India with its latest
 * pollutant readings from the OpenAQ feed.
 *
 * <p>Concentrations are the raw measured values (PM2.5/PM10 in &mu;g/m&sup3;,
 * NO2/O3 in &mu;g/m&sup3;, CO in mg/m&sup3;). {@code aqi} and {@code category}
 * are derived from the PM2.5 reading using the CPCB/NAQI breakpoints (see
 * {@link #aqi(Double)} and {@link #aqiCategory(Double)}) - no machine-learning
 * model is involved. The measurement timestamp is UTC, exposed as epoch millis
 * so the UI can render it locally. The Google Maps link is built at runtime
 * from the station coordinates - no URL is hardcoded.</p>
 */
public class AirQualityDTO {

    public static final String SOURCE = "OpenAQ";

    private String stationName;
    private Double latitude;
    private Double longitude;
    private Double pm25;
    private Double pm10;
    private Double no2;
    private Double o3;
    private Double co;
    private Integer aqi;
    private String category;
    private Long timestamp;
    private String source;
    private String mapsUrl;

    /** Google Maps deep link to the station location. Built at runtime, never hardcoded. */
    public static String buildMapsUrl(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }

    /**
     * CPCB/NAQI numeric AQI (0-500) derived from the 24-hour PM2.5 reading
     * using the standard piecewise-linear breakpoints. Returns null when no
     * PM2.5 reading is available. Purely rule-based; no ML involved.
     */
    public static Integer aqi(Double pm25) {
        if (pm25 == null) {
            return null;
        }
        double c = pm25;
        if (c < 31) return piecewise(c, 0, 30, 0, 50);
        if (c < 61) return piecewise(c, 31, 60, 51, 100);
        if (c < 91) return piecewise(c, 61, 90, 101, 200);
        if (c < 121) return piecewise(c, 91, 120, 201, 300);
        if (c < 251) return piecewise(c, 121, 250, 301, 400);
        return 500;
    }

    /**
     * CPCB/NAQI air-quality category label derived from the 24-hour PM2.5
     * reading. Returns null when no PM2.5 reading is available.
     */
    public static String aqiCategory(Double pm25) {
        if (pm25 == null) {
            return null;
        }
        double c = pm25;
        if (c < 31) return "Good";
        if (c < 61) return "Satisfactory";
        if (c < 91) return "Moderately Polluted";
        if (c < 121) return "Poor";
        if (c < 251) return "Very Poor";
        return "Severe";
    }

    private static int piecewise(double c, double cLow, double cHigh, double iLow, double iHigh) {
        double aqi = ((iHigh - iLow) / (cHigh - cLow)) * (c - cLow) + iLow;
        return (int) Math.round(aqi);
    }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getPm25() { return pm25; }
    public void setPm25(Double pm25) { this.pm25 = pm25; }
    public Double getPm10() { return pm10; }
    public void setPm10(Double pm10) { this.pm10 = pm10; }
    public Double getNo2() { return no2; }
    public void setNo2(Double no2) { this.no2 = no2; }
    public Double getO3() { return o3; }
    public void setO3(Double o3) { this.o3 = o3; }
    public Double getCo() { return co; }
    public void setCo(Double co) { this.co = co; }
    public Integer getAqi() { return aqi; }
    public void setAqi(Integer aqi) { this.aqi = aqi; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
}
