package com.disaster.service;

import com.disaster.config.WebSocketConfig;
import com.disaster.dto.LocationDTO;
import com.disaster.enums.DroneStatus;
import com.disaster.enums.MissionStatus;
import com.disaster.entity.Drone;
import com.disaster.repository.DroneRepository;
import com.disaster.security.RateLimitingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Drives the live real-time simulation: moves in-mission drones, pushes
 * location snapshots and dashboard analytics over WebSocket, and cleans up
 * stale connections.
 */
@Service
public class LiveSimulationScheduler {

    private static final Logger log = LoggerFactory.getLogger(LiveSimulationScheduler.class);
    private static final long STALE_SESSION_TIMEOUT_MS = 90_000;

    private final WebSocketConfig webSocketConfig;
    private final DroneRepository droneRepository;
    private final AnalyticsService analyticsService;
    private final LocationTrackingService locationTrackingService;
    private final RateLimitingFilter rateLimitingFilter;

    public LiveSimulationScheduler(WebSocketConfig webSocketConfig,
                                   DroneRepository droneRepository,
                                   AnalyticsService analyticsService,
                                   LocationTrackingService locationTrackingService,
                                   RateLimitingFilter rateLimitingFilter) {
        this.webSocketConfig = webSocketConfig;
        this.droneRepository = droneRepository;
        this.analyticsService = analyticsService;
        this.locationTrackingService = locationTrackingService;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Scheduled(fixedDelay = 3000)
    public void simulateDroneLocations() {
        try {
            List<LocationDTO> live = locationTrackingService.simulateMovement();
            if (!live.isEmpty()) {
                webSocketConfig.broadcastUpdate("DRONE_LOCATION", live);
            }
            List<LocationDTO> snapshot = locationTrackingService.getLocationsSnapshot();
            webSocketConfig.broadcastUpdate("LOCATION_SNAPSHOT", snapshot);
        } catch (Exception e) {
            log.warn("Location simulation failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void broadcastDashboard() {
        try {
            webSocketConfig.broadcastUpdate("DASHBOARD",
                    Map.of("analytics", analyticsService.getAnalytics()));
        } catch (Exception e) {
            log.warn("Dashboard broadcast failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void sweepStaleSessions() {
        webSocketConfig.closeStaleSessions(STALE_SESSION_TIMEOUT_MS);
        rateLimitingFilter.evictStaleBuckets();
    }

    // Keep battery / status bookkeeping in one place for tests that read drones.
    List<Drone> inMissionDrones() {
        return droneRepository.findByStatus(DroneStatus.IN_MISSION)
                .stream().filter(d -> d.getMissionStatus() == MissionStatus.IN_PROGRESS).toList();
    }
}
