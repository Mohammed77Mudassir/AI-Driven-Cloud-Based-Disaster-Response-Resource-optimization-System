package com.disaster.dto;

/**
 * Public view of a single thermal anomaly (active fire) detected inside India
 * by the NASA FIRMS active-fire feed.
 *
 * <p>Brightness is the pixel temperature in Kelvin (VIIRS {@code bright_ti4} /
 * MODIS {@code brightness}); {@code frp} (fire radiative power, MW) is the
 * fire-intensity estimate; confidence is the FIRMS detection confidence (0-100,
 * values &ge; 80 are considered "high"). Acquisition time is UTC, exposed as
 * epoch millis so the UI can render it locally. The Google Maps link is built
 * at runtime from the detection coordinates - no URL is hardcoded.</p>
 */
public class FireDTO {

    public static final String SOURCE = "NASA FIRMS";

    private String fireId;
    private Double latitude;
    private Double longitude;
    private Double brightness;
    private Double confidence;
    private Long acquisitionDate;
    private String satellite;
    private String instrument;
    private Double frp;
    private String dayNight;
    private String source;
    private String mapsUrl;

    /** Google Maps deep link to the fire location. Built at runtime, never hardcoded. */
    public static String buildMapsUrl(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }

    public String getFireId() { return fireId; }
    public void setFireId(String fireId) { this.fireId = fireId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getBrightness() { return brightness; }
    public void setBrightness(Double brightness) { this.brightness = brightness; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public Long getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(Long acquisitionDate) { this.acquisitionDate = acquisitionDate; }
    public String getSatellite() { return satellite; }
    public void setSatellite(String satellite) { this.satellite = satellite; }
    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public Double getFrp() { return frp; }
    public void setFrp(Double frp) { this.frp = frp; }
    public String getDayNight() { return dayNight; }
    public void setDayNight(String dayNight) { this.dayNight = dayNight; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
}
