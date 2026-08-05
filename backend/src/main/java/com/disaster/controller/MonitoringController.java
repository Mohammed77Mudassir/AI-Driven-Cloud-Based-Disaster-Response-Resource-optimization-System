package com.disaster.controller;

import com.disaster.dto.GeoQueryResponse;
import com.disaster.dto.HeatPointDTO;
import com.disaster.dto.LocationDTO;
import com.disaster.dto.MonitoringOverviewDTO;
import com.disaster.dto.RouteResponse;
import com.disaster.geo.GeoUtils;
import com.disaster.service.LocationTrackingService;
import com.disaster.service.WebSocketSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Real-Time Monitoring endpoints: aggregated overview, offline distance and
 * ETA calculation, and live websocket session status.
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final LocationTrackingService locationTrackingService;
    private final WebSocketSessionService webSocketSessionService;

    public MonitoringController(LocationTrackingService locationTrackingService,
                                WebSocketSessionService webSocketSessionService) {
        this.locationTrackingService = locationTrackingService;
        this.webSocketSessionService = webSocketSessionService;
    }

    @GetMapping("/overview")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<MonitoringOverviewDTO> getOverview() {
        return ResponseEntity.ok(locationTrackingService.getOverview());
    }

    @GetMapping("/locations")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationTrackingService.getAllLocations());
    }

    @GetMapping("/distance")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<GeoQueryResponse> distance(
            @RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng) {
        double distanceKm = GeoUtils.distanceKm(fromLat, fromLng, toLat, toLng);
        GeoQueryResponse response = new GeoQueryResponse();
        response.setFromLat(fromLat);
        response.setFromLng(fromLng);
        response.setToLat(toLat);
        response.setToLng(toLng);
        response.setDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
        response.setEtaMinutes(Math.round(GeoUtils.etaMinutes(distanceKm, 40.0) * 100.0) / 100.0);
        response.setSpeedKmph(40.0);
        response.setInitialBearing(Math.round(GeoUtils.bearing(fromLat, fromLng, toLat, toLng) * 10.0) / 10.0);
        response.setMessage("Distance calculated offline using the Haversine formula");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eta")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<GeoQueryResponse> eta(
            @RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng,
            @RequestParam(defaultValue = "40") double speedKmph) {
        double distanceKm = GeoUtils.distanceKm(fromLat, fromLng, toLat, toLng);
        GeoQueryResponse response = new GeoQueryResponse();
        response.setFromLat(fromLat);
        response.setFromLng(fromLng);
        response.setToLat(toLat);
        response.setToLng(toLng);
        response.setDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
        response.setEtaMinutes(Math.round(GeoUtils.etaMinutes(distanceKm, speedKmph) * 100.0) / 100.0);
        response.setSpeedKmph(speedKmph);
        response.setInitialBearing(Math.round(GeoUtils.bearing(fromLat, fromLng, toLat, toLng) * 10.0) / 10.0);
        response.setMessage("ETA estimated offline at " + speedKmph + " km/h");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/heatmap")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<List<HeatPointDTO>> heatmap() {
        return ResponseEntity.ok(locationTrackingService.getHeatmapPoints());
    }

    /**
     * Offline route between two coordinates: interpolated polyline points,
     * total distance and ETA. Used for route visualization on the live map.
     */
    @GetMapping("/route")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<RouteResponse> route(
            @RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng,
            @RequestParam(defaultValue = "30") int steps,
            @RequestParam(defaultValue = "40") double speedKmph) {
        double distanceKm = GeoUtils.distanceKm(fromLat, fromLng, toLat, toLng);
        double etaMinutes = GeoUtils.etaMinutes(distanceKm, speedKmph);

        int segments = Math.max(2, Math.min(200, steps));
        List<double[]> route = new ArrayList<>();
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double[] point = GeoUtils.interpolate(fromLat, fromLng, toLat, toLng, t);
            route.add(new double[]{Math.round(point[0] * 1_000_000.0) / 1_000_000.0,
                    Math.round(point[1] * 1_000_000.0) / 1_000_000.0});
        }

        RouteResponse response = new RouteResponse();
        response.setTotalDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
        response.setTotalTimeMinutes(Math.round(etaMinutes * 100.0) / 100.0);
        response.setRoute(route);
        response.setWaypoints(List.of("Start", "Destination"));
        response.setMode("direct");
        response.setMessage("Great-circle route with " + segments + " interpolated segments at " + speedKmph + " km/h");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ws-status")
    @PreAuthorize("@rbacService.hasPermission(authentication.principal, 'MAP_VIEW')")
    public ResponseEntity<?> wsStatus() {
        return ResponseEntity.ok(java.util.Map.of(
                "activeConnections", webSocketSessionService.getActiveSessionCount(),
                "heartbeatIntervalSeconds", 30,
                "staleTimeoutSeconds", 90));
    }
}
