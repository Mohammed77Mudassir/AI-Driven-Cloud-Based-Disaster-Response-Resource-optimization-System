package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the OpenAQ air-quality feed.
 *
 * <p>The API key is <em>not</em> declared here: it is reused from the shared
 * {@link ApiProperties#getOpenaq()} holder ({@code app.api.openaq.api-key},
 * populated from {@code OPENAQ_API_KEY}) so there is no duplicate OpenAQ
 * configuration. This class only owns the air-quality module tuning (endpoint,
 * refresh cadence and cache freshness). Nothing is hardcoded in the service
 * layer.</p>
 *
 * <pre>
 * air-quality.api.url          = ${OPENAQ_API_URL:https://api.openaq.org/v2/latest}
 * air-quality.refresh.interval = ${OPENAQ_REFRESH_INTERVAL_MS:600000}
 * air-quality.cache.minutes    = ${OPENAQ_CACHE_MINUTES:10}
 * </pre>
 */
@ConfigurationProperties(prefix = "air-quality")
public class AirQualityProperties {

    /** OpenAQ v2 "latest measurements" endpoint (India is added as a query filter). */
    private String apiUrl = "https://api.openaq.org/v2/latest";

    /** How often the scheduled refresh hits the OpenAQ API, in milliseconds. */
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
