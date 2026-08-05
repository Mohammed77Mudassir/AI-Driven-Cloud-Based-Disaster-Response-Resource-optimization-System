package com.disaster.controller;

import com.disaster.dto.*;
import com.disaster.entity.*;
import com.disaster.enums.MissionStatus;
import com.disaster.repository.*;
import com.disaster.service.SystemSettingService;
import com.disaster.service.UserService;
import com.disaster.service.WebSocketSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final List<DisasterStatus> ACTIVE_DISASTER_STATUSES = List.of(
            DisasterStatus.ASSIGNED, DisasterStatus.RESOURCES_DISPATCHED, DisasterStatus.IN_PROGRESS);

    private final UserService userService;
    private final SystemSettingService systemSettingService;
    private final WebSocketSessionService webSocketSessionService;

    private final UserRepository userRepository;
    private final DisasterRepository disasterRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final HospitalRepository hospitalRepository;
    private final ShelterRepository shelterRepository;
    private final VolunteerRepository volunteerRepository;
    private final DroneRepository droneRepository;
    private final ResourceRepository resourceRepository;
    private final RescueMissionRepository rescueMissionRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(UserService userService, SystemSettingService systemSettingService,
                           WebSocketSessionService webSocketSessionService,
                           UserRepository userRepository, DisasterRepository disasterRepository,
                           RescueTeamRepository rescueTeamRepository, HospitalRepository hospitalRepository,
                           ShelterRepository shelterRepository, VolunteerRepository volunteerRepository,
                           DroneRepository droneRepository, ResourceRepository resourceRepository,
                           RescueMissionRepository rescueMissionRepository,
                           NotificationRepository notificationRepository, AuditLogRepository auditLogRepository) {
        this.userService = userService;
        this.systemSettingService = systemSettingService;
        this.webSocketSessionService = webSocketSessionService;
        this.userRepository = userRepository;
        this.disasterRepository = disasterRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.hospitalRepository = hospitalRepository;
        this.shelterRepository = shelterRepository;
        this.volunteerRepository = volunteerRepository;
        this.droneRepository = droneRepository;
        this.resourceRepository = resourceRepository;
        this.rescueMissionRepository = rescueMissionRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ------------------------------------------------------------------
    // Emergency Operations Center dashboard
    // ------------------------------------------------------------------

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        List<User> users = userRepository.findAll();
        List<Disaster> allDisasters = disasterRepository.findAllByOrderByDateDesc();
        List<Resource> resources = resourceRepository.findAll();

        AdminDashboardDTO dto = new AdminDashboardDTO();

        // Users
        dto.setTotalUsers(users.size());
        dto.setActiveUsers(users.stream().filter(User::isActive).count());
        dto.setInactiveUsers(users.size() - dto.getActiveUsers());
        dto.setUsersByRole(users.stream()
                .collect(Collectors.groupingBy(u -> u.getRole() != null ? u.getRole().name() : "UNKNOWN",
                        Collectors.counting())));

        // Disasters
        dto.setTotalDisasters(allDisasters.size());
        dto.setPendingDisasters(allDisasters.stream()
                .filter(d -> d.getStatus() == DisasterStatus.PENDING).count());
        dto.setActiveMissions(allDisasters.stream()
                .filter(d -> d.getStatus() != null && ACTIVE_DISASTER_STATUSES.contains(d.getStatus())).count());
        dto.setResolvedDisasters(allDisasters.stream()
                .filter(d -> d.getStatus() == DisasterStatus.RESOLVED).count());

        dto.setDisastersByStatus(allDisasters.stream()
                .collect(Collectors.groupingBy(d -> d.getStatus() != null ? d.getStatus().name() : "UNKNOWN",
                        Collectors.counting())));
        dto.setDisastersBySeverity(allDisasters.stream()
                .collect(Collectors.groupingBy(d -> d.getSeverity() != null ? d.getSeverity() : "Unknown",
                        Collectors.counting())));
        dto.setDisastersByType(allDisasters.stream()
                .collect(Collectors.groupingBy(Disaster::getDisasterType, Collectors.counting())));
        dto.setDisastersByMonth(allDisasters.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDate() != null ? d.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")) : "Unknown",
                        TreeMap::new, Collectors.counting())));

        // Resource inventory
        long deployed = resources.stream().mapToLong(Resource::getDeployedQuantity).sum();
        long maintenance = resources.stream().mapToLong(Resource::getInMaintenanceQuantity).sum();
        long totalQty = resources.stream().mapToLong(Resource::getTotalQuantity).sum();
        dto.setResourcesAvailable(Math.max(0, totalQty - deployed - maintenance));
        dto.setResourcesDeployed(deployed);
        dto.setResourcesInMaintenance(maintenance);
        dto.setResourceUtilizationPercent(totalQty > 0 ? Math.round((deployed + maintenance) * 100.0 / totalQty) : 0);

        // Entity totals
        dto.setTotalRescueTeams(rescueTeamRepository.count());
        dto.setTotalHospitals(hospitalRepository.count());
        dto.setTotalShelters(shelterRepository.count());
        dto.setTotalVolunteers(volunteerRepository.count());
        dto.setTotalResources(resources.size());
        dto.setTotalDrones(droneRepository.count());

        // Live connectivity + missions
        dto.setConnectedUsers(webSocketSessionService.getActiveSessionCount());
        dto.setWebsocketHealthy(webSocketSessionService.hasActiveSessions() || webSocketSessionService.getActiveSessionCount() == 0);
        dto.setActiveMissions(dto.getActiveMissions() + rescueMissionRepository.countByStatus(MissionStatus.ASSIGNED)
                + rescueMissionRepository.countByStatus(MissionStatus.IN_PROGRESS));

        // System
        dto.setSystemUptime(systemSettingService.getSystemUptime());
        dto.setSystemHealthStatus(systemSettingService.getSystemHealth().get("status").toString());

        // Feeds
        dto.setRecentActivities(auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .limit(12).map(AuditLogDTO::fromEntity).toList());
        dto.setRecentNotifications(notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(12).map(NotificationDTO::fromEntity).toList());

        return ResponseEntity.ok(dto);
    }

    // ------------------------------------------------------------------
    // Emergency Operations Center endpoints
    // ------------------------------------------------------------------

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications() {
        List<Notification> latest = notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(10).toList();
        long unread = notificationRepository.findAll().stream()
                .filter(n -> !n.isRead()).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unread", unread);
        result.put("notifications", latest.stream().map(NotificationDTO::fromEntity).toList());
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Settings
    // ------------------------------------------------------------------

    @GetMapping("/settings")
    public ResponseEntity<List<SystemSettingDTO>> getSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PutMapping("/settings/{key}")
    public ResponseEntity<SystemSettingDTO> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(systemSettingService.updateSetting(key, body.get("value")));
    }
}
