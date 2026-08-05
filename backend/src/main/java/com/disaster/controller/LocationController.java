package com.disaster.controller;

import com.disaster.config.WebSocketConfig;
import com.disaster.dto.LocationDTO;
import com.disaster.service.LocationTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationTrackingService locationTrackingService;
    private final WebSocketConfig webSocketConfig;

    public LocationController(LocationTrackingService locationTrackingService, WebSocketConfig webSocketConfig) {
        this.locationTrackingService = locationTrackingService;
        this.webSocketConfig = webSocketConfig;
    }

    @GetMapping
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationTrackingService.getAllLocations());
    }

    @GetMapping("/live")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<LocationDTO>> getLiveLocations() {
        List<LocationDTO> live = locationTrackingService.simulateMovement();
        if (!live.isEmpty()) {
            webSocketConfig.broadcastUpdate("DRONE_LOCATION", live);
        }
        webSocketConfig.broadcastUpdate("LOCATION_SNAPSHOT", locationTrackingService.getLocationsSnapshot());
        return ResponseEntity.ok(live);
    }
}
