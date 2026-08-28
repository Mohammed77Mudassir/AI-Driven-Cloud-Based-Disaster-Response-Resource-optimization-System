package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration for the NASA FIRMS (Fire Information for Resource Management
 * System) active-fire feed.
 *
 * <p>The API key is <em>not</em> declared here: it is reused from the shared
 * {@link ApiProperties#getNasaFirms()} holder ({@code app.api.nasa-firms.api-key},
 * populated from {@code NASA_FIRMS_API_KEY}) so there is no duplicate FIRMS
 * configuration. This class only owns the fire-module tuning (endpoint,
 * refresh cadence, cache freshness, which FIRMS datasets to poll and the query
 * window). Nothing is hardcoded in the service layer.</p>
 *
 * <pre>
 * fire.api.url          = ${NASA_FIRMS_API_URL:https://firms.modaps.eosdis.nasa.gov/api/area/csv}
 * fire.refresh.interval = ${FIRE_REFRESH_INTERVAL_MS:600000}
 * fire.cache.minutes    = ${FIRE_CACHE_MINUTES:10}
 * fire.sources          = ${NASA_FIRMS_SOURCES:VIIRS_SNPP_NRT,MODIS_NRT}
 * fire.day              = ${NASA_FIRMS_DAY:1}
 * fire.day-range        = ${NASA_FIRMS_DAY_RANGE:1}
 * </pre>
 */
@ConfigurationProperties(prefix = "fire")
public class FireProperties {

    /** NASA FIRMS area CSV endpoint. The full request URL is built from this
     *  base + API key + dataset + India area + query window. */
    private String apiUrl = "https://firms.modaps.eosdis.nasa.gov/api/area/csv";

    /** How often the scheduled refresh hits the NASA FIRMS API, in milliseconds. */
    private long refreshInterval = 600000;

    /** How long a successful fetch is considered fresh, in minutes. */
    private long cacheMinutes = 10;

    /** FIRMS datasets to poll; results are merged and de-duplicated across sources. */
    private List<String> sources = List.of("VIIRS_SNPP_NRT", "MODIS_NRT");

    /** How many days back the area query window starts (FIRMS area API contract). */
    private int day = 1;

    /** Width of the area query window in days (FIRMS area API contract). */
    private int dayRange = 1;

    public boolean isConfigured() {
        return apiUrl != null && !apiUrl.isBlank();
    }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public long getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(long refreshInterval) { this.refreshInterval = refreshInterval; }
    public long getCacheMinutes() { return cacheMinutes; }
    public void setCacheMinutes(long cacheMinutes) { this.cacheMinutes = cacheMinutes; }
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public int getDayRange() { return dayRange; }
    public void setDayRange(int dayRange) { this.dayRange = dayRange; }
}
