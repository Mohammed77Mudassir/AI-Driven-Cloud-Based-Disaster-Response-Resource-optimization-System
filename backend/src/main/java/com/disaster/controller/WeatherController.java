package com.disaster.controller;

import com.disaster.dto.WeatherDTO;
import com.disaster.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;
    
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'WEATHER_VIEW')")
    public ResponseEntity<WeatherDTO> getWeather(
            @RequestParam String location,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(weatherService.getWeather(location, latitude, longitude));
    }
}
