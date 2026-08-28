package com.disaster.controller;

import com.disaster.dto.WeatherDTO;
import com.disaster.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Current-weather endpoint.
 *
 * <pre>
 *   GET /api/weather?location=Bengaluru
 *   GET /api/weather?latitude=12.9716&longitude=77.5946
 * </pre>
 *
 * <p>Coordinates win when both are supplied; otherwise a small built-in map
 * of common Indian cities is used to resolve coordinates from the location
 * name (there is no geocoding dependency). Unknown locations fall back to a
 * neutral coordinate pair so the endpoint never fails on name alone. The
 * response shape is identical whether the data comes from OpenWeather or the
 * mock provider.</p>
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final String DEFAULT_LOCATION = "Bengaluru";
    private static final Map<String, double[]> CITY_COORDS = Map.ofEntries(
            Map.entry("bengaluru", new double[]{12.9716, 77.5946}),
            Map.entry("bangalore", new double[]{12.9716, 77.5946}),
            Map.entry("delhi", new double[]{28.6139, 77.2090}),
            Map.entry("new delhi", new double[]{28.6139, 77.2090}),
            Map.entry("mumbai", new double[]{19.0760, 72.8777}),
            Map.entry("chennai", new double[]{13.0827, 80.2707}),
            Map.entry("kolkata", new double[]{22.5726, 88.3639}),
            Map.entry("hyderabad", new double[]{17.3850, 78.4867}),
            Map.entry("pune", new double[]{18.5204, 73.8567}),
            Map.entry("ahmedabad", new double[]{23.0225, 72.5714}),
            Map.entry("jaipur", new double[]{26.9124, 75.7873}),
            Map.entry("kochi", new double[]{9.9312, 76.2673}),
            Map.entry("thiruvananthapuram", new double[]{8.5241, 76.9366}),
            Map.entry("lucknow", new double[]{26.8467, 80.9462}),
            Map.entry("guwahati", new double[]{26.1445, 91.7362}),
            Map.entry("srinagar", new double[]{34.0837, 74.7973}));

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'WEATHER_VIEW')")
    public ResponseEntity<?> getWeather(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        if ((latitude == null) != (longitude == null)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "latitude and longitude must be provided together."));
        }
        if (latitude != null
                && (latitude < -90.0 || latitude > 90.0
                || longitude < -180.0 || longitude > 180.0)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid coordinates: latitude must be within [-90, 90] and longitude within [-180, 180]."));
        }

        String resolvedLocation = (location == null || location.isBlank()) ? DEFAULT_LOCATION : location.trim();
        double lat;
        double lon;
        if (latitude != null && longitude != null) {
            lat = latitude;
            lon = longitude;
        } else {
            double[] coords = CITY_COORDS.get(resolvedLocation.toLowerCase());
            lat = coords != null ? coords[0] : 0.0;
            lon = coords != null ? coords[1] : 0.0;
        }
        return ResponseEntity.ok(weatherService.getWeather(resolvedLocation, lat, lon));
    }
}
