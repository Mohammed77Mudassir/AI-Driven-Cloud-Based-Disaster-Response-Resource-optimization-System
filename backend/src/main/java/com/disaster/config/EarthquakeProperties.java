package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the USGS earthquake feed.
 *
 * <p>Binds the {@code earthquake.*} properties, which are populated from the
 * {@code USGS_EARTHQUAKE_API} environment variable and related tuning
 * variables (see application.properties). Nothing is hardcoded in the service
 * layer.</p>
 *
 * <pre>
 * earthquake.api.url          = ${USGS_EARTHQUAKE_API:https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson}
 * earthquake.refresh.interval = ${EARTHQUAKE_REFRESH_INTERVAL_MS:300000}
 * earthquake.cache.minutes    = ${EARTHQUAKE_CACHE_MINUTES:5}
 * </pre>
 */
@ConfigurationProperties(prefix = "earthquake")
public class EarthquakeProperties {

    /** USGS "all earthquakes, past day" GeoJSON feed. */
    private String apiUrl = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";

    /** How often the scheduled refresh hits the USGS API, in milliseconds. */
    private long refreshInterval = 300000;

    /** How long a successful fetch is considered fresh, in minutes. */
    private long cacheMinutes = 5;

    public boolean isConfigured() {
        return apiUrl != null && !apiUrl.isBlank();
    }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public long getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(long refreshInterval) { this.refreshInterval = refreshInterval; }
    public long getCacheMinutes() { return cacheMinutes; }
    public void setCacheMinutes(long cacheMinutes) { this.cacheMinutes = cacheMinutes; }
}
