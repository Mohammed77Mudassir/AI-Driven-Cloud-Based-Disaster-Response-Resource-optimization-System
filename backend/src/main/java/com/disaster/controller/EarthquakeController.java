package com.disaster.controller;

import com.disaster.dto.EarthquakeDTO;
import com.disaster.service.EarthquakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Live earthquake monitoring. Returns earthquakes detected inside India from
 * the USGS feed, newest first. Automatically degrades to the last cached data
 * (or an empty list) when the USGS API is unavailable.
 */
@RestController
@RequestMapping("/api/earthquakes")
public class EarthquakeController {

    private final EarthquakeService earthquakeService;

    public EarthquakeController(EarthquakeService earthquakeService) {
        this.earthquakeService = earthquakeService;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<EarthquakeDTO>> getEarthquakes() {
        return ResponseEntity.ok(earthquakeService.getEarthquakes());
    }
}
