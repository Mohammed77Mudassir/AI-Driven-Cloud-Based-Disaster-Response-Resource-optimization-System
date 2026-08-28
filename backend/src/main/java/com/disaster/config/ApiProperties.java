package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * General holder for external hazard-data / map API credentials.
 *
 * <p>Binds the {@code app.api.*} properties, populated from environment
 * variables (see application.properties). This is the extension point for
 * future third-party APIs: add an environment variable, a property in
 * application.properties and a nested {@link Provider} here - no existing
 * service or controller has to change.</p>
 *
 * <pre>
 * app.api.openaq.api-key       = ${OPENAQ_API_KEY:}
 * app.api.usgs.api-url         = ${USGS_API_URL:https://earthquake.usgs.gov/fdsnws/event/1/query}
 * app.api.nasa-firms.api-key   = ${NASA_FIRMS_API_KEY:}
 * </pre>
 */
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    private Provider openaq = new Provider();
    private Provider usgs = new Provider();
    private Provider nasaFirms = new Provider();

    public Provider getOpenaq() {
        return openaq;
    }

    public void setOpenaq(Provider openaq) {
        this.openaq = openaq;
    }

    public Provider getUsgs() {
        return usgs;
    }

    public void setUsgs(Provider usgs) {
        this.usgs = usgs;
    }

    public Provider getNasaFirms() {
        return nasaFirms;
    }

    public void setNasaFirms(Provider nasaFirms) {
        this.nasaFirms = nasaFirms;
    }

    /** Generic credential holder: an optional API key and an endpoint URL. */
    public static class Provider {
        private String apiKey = "";
        private String apiUrl = "";

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }
}
