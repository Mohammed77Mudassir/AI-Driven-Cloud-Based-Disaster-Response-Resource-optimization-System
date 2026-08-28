package com.disaster.service;

import com.disaster.config.WeatherProperties;
import com.disaster.dto.WeatherDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * OpenWeatherMap-backed {@link WeatherService}.
 *
 * <p>The API key and endpoint are read exclusively from
 * {@link WeatherProperties} (bound from the {@code OPENWEATHER_API_KEY} /
 * {@code OPENWEATHER_API_URL} environment variables) - nothing is
 * hardcoded. When no key is configured the provider transparently delegates
 * to {@link MockWeatherProvider}, preserving the exact demo behaviour. Any
 * external failure (timeout, HTTP error, malformed body) also falls back to
 * the mock provider, so the weather page never crashes because OpenWeather
 * is unavailable.</p>
 */
@Service
@Primary
public class OpenWeatherProvider implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherProvider.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final WeatherProperties properties;
    private final RestTemplate restTemplate;
    private final MockWeatherProvider mockWeatherProvider;
    private final WeatherRiskService weatherRiskService;

    public OpenWeatherProvider(WeatherProperties properties,
                               RestTemplate externalApiRestTemplate,
                               MockWeatherProvider mockWeatherProvider,
                               WeatherRiskService weatherRiskService) {
        this.properties = properties;
        this.restTemplate = externalApiRestTemplate;
        this.mockWeatherProvider = mockWeatherProvider;
        this.weatherRiskService = weatherRiskService;
    }

    @PostConstruct
    void reportMode() {
        if (properties.isConfigured()) {
            log.info("OpenWeather provider ACTIVE (units: {}).", properties.getUnits());
        } else {
            log.info("OPENWEATHER_API_KEY not configured; using the mock weather provider.");
        }
    }

    @Override
    public WeatherDTO getWeather(String location, double latitude, double longitude) {
        if (!properties.isConfigured()) {
            log.info("[Weather] OPENWEATHER_API_KEY not configured; using the mock weather provider.");
            return mockWeatherProvider.getWeather(location, latitude, longitude);
        }

        log.info("[Weather] Using OpenWeather provider");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getApiUrl())
                .queryParam("units", properties.getUnits())
                .queryParam("appid", properties.getApiKey());

        // Prefer explicit coordinates; fall back to a city-name query.
        if (latitude != 0.0 || longitude != 0.0) {
            builder.queryParam("lat", latitude).queryParam("lon", longitude);
        } else if (location != null && !location.isBlank()) {
            builder.queryParam("q", location);
        }

        String url = builder.build().toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);
            if (body == null) {
                throw new IllegalStateException("OpenWeatherMap returned an empty response");
            }
            return map(body, location, latitude, longitude);
        } catch (RuntimeException ex) {
            log.warn("[Weather] OpenWeather unavailable, using mock fallback ({}).", ex.getMessage());
            return mockWeatherProvider.getWeather(location, latitude, longitude);
        }
    }

    @SuppressWarnings("unchecked")
    private WeatherDTO map(Map<String, Object> body, String location, double latitude, double longitude) {
        WeatherDTO dto = new WeatherDTO();
        dto.setLocation(location);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);

        Map<String, Object> main = (Map<String, Object>) body.get("main");
        if (main != null) {
            dto.setTemperature(num(main.get("temp")));
            dto.setFeelsLike(num(main.get("feels_like")));
            dto.setHumidity((int) num(main.get("humidity")));
            dto.setPressure((int) num(main.get("pressure")));
        }

        Map<String, Object> wind = (Map<String, Object>) body.get("wind");
        if (wind != null) {
            dto.setWindSpeed(num(wind.get("speed")));
            dto.setWindDirection(num(wind.get("deg")));
        }

        Map<String, Object> clouds = (Map<String, Object>) body.get("clouds");
        if (clouds != null) {
            dto.setCloudiness((int) num(clouds.get("all")));
        }

        Object visibility = body.get("visibility");
        dto.setVisibility(visibility instanceof Number n ? n.intValue() : 10000);

        List<Map<String, Object>> weather = (List<Map<String, Object>>) body.get("weather");
        String condition = "Unknown";
        String description = "";
        if (weather != null && !weather.isEmpty()) {
            Object mainCondition = weather.get(0).get("main");
            if (mainCondition != null) {
                condition = String.valueOf(mainCondition);
            }
            Object desc = weather.get(0).get("description");
            if (desc != null) {
                description = String.valueOf(desc);
            }
        }
        dto.setWeatherCondition(condition);
        dto.setWeatherDescription(description);
        dto.setRainProbability(estimateRainProbability(condition, body));

        Map<String, Object> sys = (Map<String, Object>) body.get("sys");
        if (sys != null) {
            if (sys.get("sunrise") instanceof Number sr) {
                dto.setSunrise(formatTime(sr.longValue()));
            }
            if (sys.get("sunset") instanceof Number ss) {
                dto.setSunset(formatTime(ss.longValue()));
            }
        }

        Object dt = body.get("dt");
        dto.setTimestamp(dt instanceof Number n ? n.longValue() * 1000L : System.currentTimeMillis());
        dto.setLastUpdated(Instant.now().toString());

        dto.setProvider("openweather");
        dto.setMockData(false);
        dto.setFallback(false);

        weatherRiskService.assess(dto);
        return dto;
    }

    /** Heuristic rain probability based on the current conditions and cloud cover. */
    @SuppressWarnings("unchecked")
    private double estimateRainProbability(String condition, Map<String, Object> body) {
        if (condition.contains("Rain") || condition.contains("Drizzle") || condition.contains("Thunderstorm")) {
            return 85.0;
        }
        if (condition.contains("Snow")) {
            return 70.0;
        }
        Map<String, Object> clouds = (Map<String, Object>) body.get("clouds");
        if (clouds != null) {
            double coverage = num(clouds.get("all"));
            if (coverage > 70) return 45.0;
            if (coverage > 40) return 20.0;
        }
        return condition.contains("Clear") ? 5.0 : 10.0;
    }

    private String formatTime(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .format(TIME_FORMATTER);
    }

    private double num(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }
}
