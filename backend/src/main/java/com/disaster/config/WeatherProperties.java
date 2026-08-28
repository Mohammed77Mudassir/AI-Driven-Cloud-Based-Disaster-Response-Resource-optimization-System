package com.disaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the weather data provider.
 *
 * <p>Binds the {@code weather.*} properties, which are populated from the
 * {@code OPENWEATHER_API_KEY} / {@code OPENWEATHER_API_URL} environment
 * variables (see application.properties). No API key is ever hardcoded.</p>
 *
 * <pre>
 * weather.api-key = ${OPENWEATHER_API_KEY:}
 * weather.api-url = ${OPENWEATHER_API_URL:https://api.openweathermap.org/data/2.5/weather}
 * </pre>
 */
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {

    /** OpenWeatherMap API key. Blank means "use the mock weather provider". */
    private String apiKey = "";

    /** OpenWeatherMap "current weather" endpoint. */
    private String apiUrl = "https://api.openweathermap.org/data/2.5/weather";

    /** Temperature units returned by the API: metric, imperial or standard. */
    private String units = "metric";

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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
