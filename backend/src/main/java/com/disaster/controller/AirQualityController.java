package com.disaster.controller;

import com.disaster.dto.AirQualityDTO;
import com.disaster.service.AirQualityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Live OpenAQ air-quality monitoring. Returns the latest pollutant readings
 * (PM2.5, PM10, NO2, O3, CO plus a derived AQI/category) for stations inside
 * India, most hazardous first. Automatically degrades to the last cached data
 * (or an empty list - HTTP 200 - when OpenAQ is temporarily unavailable or no
 * {@code OPENAQ_API_KEY} is configured).
 */
@RestController
@RequestMapping("/api/air-quality")
public class AirQualityController {

    private final AirQualityService airQualityService;

    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<AirQualityDTO>> getAirQuality() {
        return ResponseEntity.ok(airQualityService.getAirQuality());
    }
}
