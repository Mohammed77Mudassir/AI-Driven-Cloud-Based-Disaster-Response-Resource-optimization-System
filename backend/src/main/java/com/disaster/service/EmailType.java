package com.disaster.service;

/**
 * Categories of email the platform can produce. Every type is rendered through
 * the shared branded HTML template via {@link EmailTemplateService}.
 */
public enum EmailType {

    DISASTER_ALERT("Disaster Alert"),
    WEATHER_ALERT("Weather Alert"),
    EARTHQUAKE_ALERT("Earthquake Alert"),
    SHELTER_INFORMATION("Shelter Information"),
    VOLUNTEER_NOTIFICATION("Volunteer Notification"),
    RESOURCE_DEPLOYMENT("Resource Deployment Notification"),
    ADMIN_REPORT("Admin Report");

    private final String label;

    EmailType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
