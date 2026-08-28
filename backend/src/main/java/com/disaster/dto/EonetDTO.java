package com.disaster.dto;

/**
 * Public view of a single NASA EONET natural disaster event detected inside
 * India's geographical boundaries.
 *
 * <p>Only <em>active</em> (open) events are ever produced; closed events are
 * discarded during mapping. The Google Maps link is built at runtime from the
 * event coordinates so no URL is hardcoded.</p>
 */
public class EonetDTO {

    public static final String SOURCE = "NASA EONET";

    private String eventId;
    private String title;
    private String category;
    private String status;
    private Double latitude;
    private Double longitude;
    private Long eventDate;
    private String source;
    private String mapsUrl;

    /** Google Maps deep link to the event location. Built at runtime. */
    public static String buildMapsUrl(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Long getEventDate() { return eventDate; }
    public void setEventDate(Long eventDate) { this.eventDate = eventDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMapsUrl() { return mapsUrl; }
    public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
}
