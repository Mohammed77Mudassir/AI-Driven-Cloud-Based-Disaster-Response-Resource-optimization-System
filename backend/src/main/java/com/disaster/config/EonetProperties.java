package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the NASA EONET (Earth Observatory Natural Event Tracker)
 * event feed.
 *
 * <p>Binds the {@code eonet.*} properties, which are populated from the
 * {@code NASA_EONET_API} environment variable and related tuning variables
 * (see application.properties). Nothing is hardcoded in the service layer.</p>
 *
 * <pre>
 * eonet.api.url          = ${NASA_EONET_API:https://eonet.gsfc.nasa.gov/api/v3/events}
 * eonet.refresh.interval = ${EONET_REFRESH_INTERVAL_MS:600000}
 * eonet.cache.minutes    = ${EONET_CACHE_MINUTES:10}
 * </pre>
 */
@ConfigurationProperties(prefix = "eonet")
public class EonetProperties {

    /** NASA EONET public v3 events feed. Public; no API key required. */
    private String apiUrl = "https://eonet.gsfc.nasa.gov/api/v3/events";

    /** How often the scheduled refresh hits the NASA EONET API, in milliseconds. */
    private long refreshInterval = 600000;

    /** How long a successful fetch is considered fresh, in minutes. */
    private long cacheMinutes = 10;

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
