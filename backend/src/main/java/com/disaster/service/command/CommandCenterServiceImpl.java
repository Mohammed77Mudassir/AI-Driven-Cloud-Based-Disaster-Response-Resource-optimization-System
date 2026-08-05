package com.disaster.service.command;

import com.disaster.dto.command.ActiveMissionDTO;
import com.disaster.dto.command.CommandCenterDTO;
import com.disaster.dto.command.EquipmentReadinessDTO;
import com.disaster.dto.command.KpiDTO;
import com.disaster.dto.command.MilestoneDTO;
import com.disaster.dto.command.MissionTimelineDTO;
import com.disaster.dto.command.MonthlyTrendDTO;
import com.disaster.dto.command.OperationalAlertDTO;
import com.disaster.dto.command.ResponseTimeAnalyticsDTO;
import com.disaster.dto.command.TeamCardDTO;
import com.disaster.dto.command.TeamWorkloadDTO;
import com.disaster.dto.command.VehicleCardDTO;
import com.disaster.entity.MissionEvent;
import com.disaster.entity.RescueEquipment;
import com.disaster.entity.RescueMission;
import com.disaster.entity.RescueTeam;
import com.disaster.entity.RescueVehicle;
import com.disaster.entity.TeamLocationUpdate;
import com.disaster.entity.TeamShift;
import com.disaster.enums.MissionEventType;
import com.disaster.enums.MissionStatus;
import com.disaster.enums.ShiftStatus;
import com.disaster.enums.TeamStatus;
import com.disaster.enums.VehicleStatus;
import com.disaster.repository.MissionEventRepository;
import com.disaster.repository.RescueEquipmentRepository;
import com.disaster.repository.RescueMissionRepository;
import com.disaster.repository.RescueTeamRepository;
import com.disaster.repository.RescueVehicleRepository;
import com.disaster.repository.TeamLocationUpdateRepository;
import com.disaster.repository.TeamShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandCenterServiceImpl implements CommandCenterService {

    private static final List<MissionStatus> ACTIVE_STATUSES =
            List.of(MissionStatus.PENDING, MissionStatus.ASSIGNED, MissionStatus.IN_PROGRESS);

    private static final int LOW_FUEL_THRESHOLD = 25;
    private static final int EQUIPMENT_SHORTAGE_THRESHOLD = 50;
    private static final int INACTIVE_TEAM_HOURS = 24;
    private static final int MAINTENANCE_MAX_AGE_DAYS = 90;

    private final RescueTeamRepository teamRepository;
    private final RescueMissionRepository missionRepository;
    private final RescueVehicleRepository vehicleRepository;
    private final RescueEquipmentRepository equipmentRepository;
    private final TeamShiftRepository shiftRepository;
    private final MissionEventRepository eventRepository;
    private final TeamLocationUpdateRepository locationRepository;

    public CommandCenterServiceImpl(RescueTeamRepository teamRepository,
                                    RescueMissionRepository missionRepository,
                                    RescueVehicleRepository vehicleRepository,
                                    RescueEquipmentRepository equipmentRepository,
                                    TeamShiftRepository shiftRepository,
                                    MissionEventRepository eventRepository,
                                    TeamLocationUpdateRepository locationRepository) {
        this.teamRepository = teamRepository;
        this.missionRepository = missionRepository;
        this.vehicleRepository = vehicleRepository;
        this.equipmentRepository = equipmentRepository;
        this.shiftRepository = shiftRepository;
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandCenterDTO getDashboard() {
        LocalDateTime now = LocalDateTime.now();

        List<RescueTeam> teams = teamRepository.findAll();
        List<RescueMission> missions = missionRepository.findAllByOrderByIdDesc();
        List<RescueVehicle> vehicles = vehicleRepository.findAll();
        List<RescueEquipment> equipment = equipmentRepository.findAll();
        List<TeamShift> shifts = shiftRepository.findAll();

        Map<Long, List<MissionEvent>> eventsByMission = eventRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getMission() != null ? e.getMission().getId() : -1L));

        Map<Long, TeamLocationUpdate> latestLocations = latestLocationByTeam();

        List<RescueMission> activeMissions = missions.stream()
                .filter(m -> ACTIVE_STATUSES.contains(m.getStatus()))
                .toList();

        CommandCenterDTO dto = new CommandCenterDTO();
        dto.setLastUpdated(now);
        dto.setKpis(buildKpis(teams, missions, vehicles, equipment));
        dto.setActiveMissions(buildActiveMissions(activeMissions, vehicles, teams, latestLocations));
        dto.setTeams(buildTeamCards(teams, missions, latestLocations));
        dto.setResponseAnalytics(buildResponseAnalytics(missions));
        dto.setEquipmentReadiness(buildEquipmentReadiness(teams, equipment));
        dto.setVehicles(buildVehicleCards(vehicles));
        dto.setWorkloads(buildWorkloads(teams, missions, vehicles, equipment, shifts));
        dto.setMissionTimelines(buildMissionTimelines(activeMissions, eventsByMission));
        dto.setAlerts(buildAlerts(teams, activeMissions, vehicles, equipment, latestLocations));
        return dto;
    }

    // ------------------------------------------------------------------
    // KPIs
    // ------------------------------------------------------------------

    private List<KpiDTO> buildKpis(List<RescueTeam> teams, List<RescueMission> missions,
                                   List<RescueVehicle> vehicles, List<RescueEquipment> equipment) {
        List<KpiDTO> kpis = new ArrayList<>();

        long totalTeams = teams.size();
        long onMission = teams.stream().filter(t -> TeamStatus.ON_MISSION.name().equals(t.getStatus())).count();
        long availableTeams = teams.stream().filter(t -> TeamStatus.AVAILABLE.name().equals(t.getStatus())).count();
        long resting = teams.stream().filter(t -> TeamStatus.STANDING_BY.name().equals(t.getStatus())
                || TeamStatus.RETURNED.name().equals(t.getStatus())
                || TeamStatus.OFF_DUTY.name().equals(t.getStatus())).count();
        long activeMissions = missions.stream().filter(m -> ACTIVE_STATUSES.contains(m.getStatus())).count();
        long criticalMissions = missions.stream()
                .filter(m -> ACTIVE_STATUSES.contains(m.getStatus()) && "CRITICAL".equalsIgnoreCase(m.getPriority()))
                .count();
        double avgResponse = averageResponseMinutes(missions);
        double avgDuration = averageMissionDurationMinutes(missions);

        double totalVehicle = vehicles.size();
        double availableVehicle = vehicles.stream()
                .filter(v -> VehicleStatus.AVAILABLE == v.getStatus()).count();
        double vehicleAvailability = totalVehicle > 0 ? round1(availableVehicle * 100.0 / totalVehicle) : 100.0;

        int totalEq = equipment.stream().mapToInt(RescueEquipment::getTotalQuantity).sum();
        int availableEq = equipment.stream().mapToInt(RescueEquipment::getAvailableQuantity).sum();
        double eqReadiness = totalEq > 0 ? round1(availableEq * 100.0 / totalEq) : 100.0;

        kpis.add(new KpiDTO("TOTAL_TEAMS", "Total Rescue Teams", totalTeams, "", null));
        kpis.add(new KpiDTO("TEAMS_ON_MISSION", "Teams on Mission", onMission, "", null));
        kpis.add(new KpiDTO("AVAILABLE_TEAMS", "Available Teams", availableTeams, "", null));
        kpis.add(new KpiDTO("TEAMS_RESTING", "Teams Resting", resting, "", null));
        kpis.add(new KpiDTO("ACTIVE_MISSIONS", "Active Rescue Missions", activeMissions, "", null));
        kpis.add(new KpiDTO("CRITICAL_MISSIONS", "Critical Missions", criticalMissions, "", null));
        kpis.add(new KpiDTO("AVG_RESPONSE", "Avg Response Time", avgResponse, "min", null));
        kpis.add(new KpiDTO("AVG_DURATION", "Avg Mission Duration", avgDuration, "min", null));
        kpis.add(new KpiDTO("VEHICLE_AVAILABILITY", "Vehicle Availability", vehicleAvailability, "%", null));
        kpis.add(new KpiDTO("EQUIPMENT_READINESS", "Equipment Readiness", eqReadiness, "%", null));
        return kpis;
    }

    // ------------------------------------------------------------------
    // Active missions
    // ------------------------------------------------------------------

    private List<ActiveMissionDTO> buildActiveMissions(List<RescueMission> activeMissions,
                                                       List<RescueVehicle> vehicles,
                                                       List<RescueTeam> teams,
                                                       Map<Long, TeamLocationUpdate> latestLocations) {
        Map<Long, RescueTeam> teamById = teams.stream().collect(Collectors.toMap(RescueTeam::getId, t -> t));
        return activeMissions.stream().map(m -> {
            ActiveMissionDTO dto = new ActiveMissionDTO();
            dto.setMissionId(m.getId());
            dto.setMissionCode(m.getMissionCode());
            dto.setTitle(m.getTitle());
            dto.setMissionType(m.getMissionType());
            dto.setPriority(m.getPriority());
            dto.setStatus(m.getStatus() != null ? m.getStatus().name() : null);
            dto.setStartTime(m.getStartTime());
            dto.setEstimatedCompletionTime(m.getEndTime());
            if (m.getDisaster() != null) {
                dto.setDisasterName(m.getDisaster().getDisasterType() + " at " + m.getDisaster().getLocation());
            }
            RescueTeam team = m.getTeam() != null ? teamById.get(m.getTeam().getId()) : null;
            if (team != null) {
                dto.setTeamId(team.getId());
                dto.setTeamName(team.getTeamName());
                dto.setTeamLeader(team.getTeamLeader());
                TeamLocationUpdate loc = latestLocations.get(team.getId());
                if (loc != null) {
                    dto.setLatitude(loc.getLatitude());
                    dto.setLongitude(loc.getLongitude());
                    dto.setLocation(team.getLocation());
                } else {
                    dto.setLatitude(team.getLatitude());
                    dto.setLongitude(team.getLongitude());
                    dto.setLocation(team.getLocation());
                }
            }
            vehicles.stream()
                    .filter(v -> m.getId().equals(v.getAssignedMission() != null ? v.getAssignedMission().getId() : null))
                    .findFirst()
                    .ifPresent(v -> dto.setAssignedVehicle(
                            (v.getVehicleType() != null ? v.getVehicleType().name() : "VEHICLE")
                                    + " (" + v.getRegistrationNumber() + ")"));
            dto.setProgress(computeProgress(m));
            if (m.getStatus() == MissionStatus.IN_PROGRESS && m.getEndTime() != null && LocalDateTime.now().isAfter(m.getEndTime())) {
                dto.setDelayMinutes(Math.max(0, Duration.between(m.getEndTime(), LocalDateTime.now()).toMinutes()));
            }
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------------
    // Team availability cards
    // ------------------------------------------------------------------

    private List<TeamCardDTO> buildTeamCards(List<RescueTeam> teams, List<RescueMission> missions,
                                             Map<Long, TeamLocationUpdate> latestLocations) {
        Map<Long, RescueMission> activeByTeam = missions.stream()
                .filter(m -> ACTIVE_STATUSES.contains(m.getStatus()) && m.getTeam() != null)
                .collect(Collectors.toMap(
                        m -> m.getTeam().getId(), m -> m, (a, b) -> a, LinkedHashMap::new));

        return teams.stream().map(t -> {
            TeamCardDTO dto = new TeamCardDTO();
            dto.setTeamId(t.getId());
            dto.setTeamName(t.getTeamName());
            dto.setTeamLeader(t.getTeamLeader());
            dto.setTeamSize(t.getMemberCount());
            dto.setMaxCapacity(t.getMaxCapacity());
            dto.setSpecialization(t.getSpecialty());
            dto.setStatus(t.getStatus());
            dto.setLocation(t.getLocation());
            TeamLocationUpdate loc = latestLocations.get(t.getId());
            if (loc != null) {
                dto.setLatitude(loc.getLatitude());
                dto.setLongitude(loc.getLongitude());
                dto.setLastLocationUpdateAt(loc.getTimestamp());
            } else {
                dto.setLatitude(t.getLatitude());
                dto.setLongitude(t.getLongitude());
                dto.setLastLocationUpdateAt(t.getLastLocationUpdateAt());
            }
            RescueMission mission = activeByTeam.get(t.getId());
            if (mission != null) {
                dto.setAssignedMissionCode(mission.getMissionCode());
                dto.setAssignedMissionTitle(mission.getTitle());
            }
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------------
    // Response time analytics
    // ------------------------------------------------------------------

    private ResponseTimeAnalyticsDTO buildResponseAnalytics(List<RescueMission> missions) {
        ResponseTimeAnalyticsDTO dto = new ResponseTimeAnalyticsDTO();

        List<Double> responseTimes = missions.stream()
                .filter(m -> m.getStartTime() != null && m.getCreatedAt() != null)
                .map(m -> (double) Math.max(0, Duration.between(m.getCreatedAt(), m.getStartTime()).toMinutes()))
                .toList();

        dto.setAverageResponseMinutes(responseTimes.isEmpty() ? 0.0 : round1(responseTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        dto.setFastestResponseMinutes(responseTimes.isEmpty() ? 0.0 : responseTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0));
        dto.setSlowestResponseMinutes(responseTimes.isEmpty() ? 0.0 : responseTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        dto.setAverageMissionDurationMinutes(round1(averageMissionDurationMinutes(missions)));

        long completed = missions.stream().filter(m -> m.getStatus() == MissionStatus.COMPLETED).count();
        long cancelled = missions.stream().filter(m -> m.getStatus() == MissionStatus.CANCELLED).count();
        dto.setCompletionRate((completed + cancelled) > 0 ? round1(completed * 100.0 / (completed + cancelled)) : 0.0);
        dto.setMonthlyTrend(buildMonthlyTrend(missions));
        return dto;
    }

    private List<MonthlyTrendDTO> buildMonthlyTrend(List<RescueMission> missions) {
        YearMonth current = YearMonth.now();
        List<MonthlyTrendDTO> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            List<RescueMission> monthMissions = missions.stream()
                    .filter(m -> m.getCreatedAt() != null && YearMonth.from(m.getCreatedAt()).equals(ym))
                    .toList();
            List<Double> responseTimes = monthMissions.stream()
                    .filter(m -> m.getStartTime() != null)
                    .map(m -> (double) Math.max(0, Duration.between(m.getCreatedAt(), m.getStartTime()).toMinutes()))
                    .toList();
            double avg = responseTimes.isEmpty() ? 0.0
                    : round1(responseTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            long completed = monthMissions.stream().filter(m -> m.getStatus() == MissionStatus.COMPLETED).count();
            trend.add(new MonthlyTrendDTO(ym.getMonth().name().substring(0, 3) + " '" + (ym.getYear() % 100), avg, completed));
        }
        return trend;
    }

    private double averageResponseMinutes(List<RescueMission> missions) {
        List<Double> responseTimes = missions.stream()
                .filter(m -> m.getStartTime() != null && m.getCreatedAt() != null)
                .map(m -> (double) Math.max(0, Duration.between(m.getCreatedAt(), m.getStartTime()).toMinutes()))
                .toList();
        return responseTimes.isEmpty() ? 0.0 : round1(responseTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    private double averageMissionDurationMinutes(List<RescueMission> missions) {
        List<Double> durations = missions.stream()
                .filter(m -> m.getStatus() == MissionStatus.COMPLETED
                        && m.getCompletedAt() != null && m.getStartTime() != null)
                .map(m -> (double) Math.max(0, Duration.between(m.getStartTime(), m.getCompletedAt()).toMinutes()))
                .toList();
        return durations.isEmpty() ? 0.0 : round1(durations.stream().mapToDouble(Double::doubleValue).average().orElse(0));
    }

    // ------------------------------------------------------------------
    // Equipment readiness
    // ------------------------------------------------------------------

    private List<EquipmentReadinessDTO> buildEquipmentReadiness(List<RescueTeam> teams, List<RescueEquipment> equipment) {
        Map<Long, List<RescueEquipment>> byTeam = equipment.stream()
                .filter(e -> e.getTeam() != null)
                .collect(Collectors.groupingBy(e -> e.getTeam().getId()));

        return teams.stream().map(t -> {
            List<RescueEquipment> list = byTeam.getOrDefault(t.getId(), List.of());
            EquipmentReadinessDTO dto = new EquipmentReadinessDTO();
            dto.setTeamId(t.getId());
            dto.setTeamName(t.getTeamName());
            int assigned = list.stream().mapToInt(RescueEquipment::getTotalQuantity).sum();
            int available = list.stream().mapToInt(RescueEquipment::getAvailableQuantity).sum();
            int deployed = list.stream().mapToInt(RescueEquipment::getDeployedQuantity).sum();
            int maintenance = list.stream().mapToInt(RescueEquipment::getInMaintenanceQuantity).sum();
            dto.setAssigned(assigned);
            dto.setAvailable(available);
            dto.setDeployed(deployed);
            dto.setMaintenance(maintenance);
            dto.setMissing(Math.max(0, assigned - available - deployed - maintenance));
            dto.setReadinessPercent(assigned > 0 ? round1(available * 100.0 / assigned) : 100.0);
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------------
    // Vehicle status
    // ------------------------------------------------------------------

    private List<VehicleCardDTO> buildVehicleCards(List<RescueVehicle> vehicles) {
        LocalDateTime now = LocalDateTime.now();
        return vehicles.stream().map(v -> {
            VehicleCardDTO dto = new VehicleCardDTO();
            dto.setVehicleId(v.getId());
            dto.setRegistrationNumber(v.getRegistrationNumber());
            dto.setVehicleType(v.getVehicleType() != null ? v.getVehicleType().name() : null);
            dto.setModel(v.getModel());
            dto.setCapacity(v.getCapacity());
            dto.setFuelLevel(v.getFuelLevel());
            dto.setStatus(v.getStatus() != null ? v.getStatus().name() : null);
            dto.setLastMaintainedAt(v.getLastMaintainedAt());
            dto.setNextMaintenanceDue(v.getNextMaintenanceDue());
            if (v.getTeam() != null) dto.setTeamName(v.getTeam().getTeamName());
            if (v.getAssignedMission() != null) {
                dto.setAssignedMissionTitle(v.getAssignedMission().getMissionCode() + " - " + v.getAssignedMission().getTitle());
            }
            dto.setGpsActive(v.getLatitude() != 0.0 || v.getLongitude() != 0.0);
            dto.setLowFuel(v.getFuelLevel() < LOW_FUEL_THRESHOLD);
            boolean due = v.getNextMaintenanceDue() != null && now.isAfter(v.getNextMaintenanceDue());
            boolean stale = v.getLastMaintainedAt() != null
                    && Duration.between(v.getLastMaintainedAt(), now).toDays() > MAINTENANCE_MAX_AGE_DAYS;
            dto.setMaintenanceRequired(VehicleStatus.IN_MAINTENANCE == v.getStatus() || due || stale);
            dto.setUnavailable(VehicleStatus.IN_MAINTENANCE == v.getStatus() || VehicleStatus.OUT_OF_SERVICE == v.getStatus());
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------------
    // Team workload
    // ------------------------------------------------------------------

    private List<TeamWorkloadDTO> buildWorkloads(List<RescueTeam> teams, List<RescueMission> missions,
                                                 List<RescueVehicle> vehicles, List<RescueEquipment> equipment,
                                                 List<TeamShift> shifts) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        Map<Long, Long> activeByTeam = missions.stream()
                .filter(m -> ACTIVE_STATUSES.contains(m.getStatus()) && m.getTeam() != null)
                .collect(Collectors.groupingBy(m -> m.getTeam().getId(), Collectors.counting()));

        Map<Long, List<TeamShift>> shiftsByTeam = shifts.stream()
                .filter(s -> s.getTeam() != null)
                .collect(Collectors.groupingBy(s -> s.getTeam().getId()));

        Map<Long, List<RescueVehicle>> vehiclesByTeam = vehicles.stream()
                .filter(v -> v.getTeam() != null)
                .collect(Collectors.groupingBy(v -> v.getTeam().getId()));

        Map<Long, List<RescueEquipment>> equipmentByTeam = equipment.stream()
                .filter(e -> e.getTeam() != null)
                .collect(Collectors.groupingBy(e -> e.getTeam().getId()));

        return teams.stream().map(t -> {
            TeamWorkloadDTO dto = new TeamWorkloadDTO();
            dto.setTeamId(t.getId());
            dto.setTeamName(t.getTeamName());
            dto.setTeamSize(t.getMemberCount());
            dto.setActiveMissions(activeByTeam.getOrDefault(t.getId(), 0L).intValue());

            double shiftHours = shiftsByTeam.getOrDefault(t.getId(), List.of()).stream()
                    .filter(s -> s.getShiftStart() != null && s.getShiftEnd() != null
                            && !s.getShiftStart().isBefore(weekAgo) && !s.getShiftStart().isAfter(now)
                            && s.getShiftStatus() != ShiftStatus.CANCELLED)
                    .mapToDouble(s -> Math.max(0, Duration.between(s.getShiftStart(), s.getShiftEnd()).toMinutes()) / 60.0)
                    .sum();
            dto.setShiftHours(round1(shiftHours));

            List<RescueEquipment> eqList = equipmentByTeam.getOrDefault(t.getId(), List.of());
            int totalEq = eqList.stream().mapToInt(RescueEquipment::getTotalQuantity).sum();
            int usedEq = eqList.stream().mapToInt(e -> e.getDeployedQuantity() + e.getInMaintenanceQuantity()).sum();
            double eqRatio = totalEq > 0 ? (double) usedEq / totalEq : 0.0;

            List<RescueVehicle> vehList = vehiclesByTeam.getOrDefault(t.getId(), List.of());
            long deployedVeh = vehList.stream().filter(v -> VehicleStatus.DEPLOYED == v.getStatus()).count();
            double vehRatio = vehList.isEmpty() ? 0.0 : (double) deployedVeh / vehList.size();

            double resourceUsage = round1((eqRatio * 0.6 + vehRatio * 0.4) * 100.0);
            dto.setResourceUsagePercent(resourceUsage);

            double score = Math.min(100, Math.round(
                    Math.min(dto.getActiveMissions(), 3) / 3.0 * 45
                            + Math.min(shiftHours, 12) / 12.0 * 30
                            + resourceUsage / 100.0 * 25));
            dto.setScore(score);
            dto.setLevel(score >= 75 ? "CRITICAL" : score >= 55 ? "HIGH" : score >= 30 ? "MEDIUM" : "LOW");
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------------
    // Mission timelines
    // ------------------------------------------------------------------

    private List<MissionTimelineDTO> buildMissionTimelines(List<RescueMission> activeMissions,
                                                           Map<Long, List<MissionEvent>> eventsByMission) {
        return activeMissions.stream().map(m -> {
            MissionTimelineDTO dto = new MissionTimelineDTO();
            dto.setMissionId(m.getId());
            dto.setMissionCode(m.getMissionCode());
            dto.setTitle(m.getTitle());
            dto.setStatus(m.getStatus() != null ? m.getStatus().name() : null);

            List<MissionEvent> events = eventsByMission.getOrDefault(m.getId(), List.of());
            dto.getMilestones().add(milestone("Mission Created", "CREATED", events, m));
            dto.getMilestones().add(milestone("Team Assigned", "ASSIGNED", events, m));
            dto.getMilestones().add(milestone("Vehicle Assigned", "DEPLOYED", events, m));
            dto.getMilestones().add(milestone("Equipment Loaded", "RESOURCE_ALLOCATED", events, m));
            dto.getMilestones().add(milestone("Rescue Started", "IN_PROGRESS", events, m));
            dto.getMilestones().add(milestone("Mission Completed", "COMPLETED", events, m));
            return dto;
        }).toList();
    }

    private MilestoneDTO milestone(String label, String eventType, List<MissionEvent> events, RescueMission mission) {
        MissionEvent found = events.stream()
                .filter(e -> matches(e, eventType))
                .max(Comparator.comparing(MissionEvent::getOccurredAt))
                .orElse(null);
        boolean doneByStatus = switch (eventType) {
            case "ASSIGNED" -> mission.getStatus() == MissionStatus.ASSIGNED
                    || mission.getStatus() == MissionStatus.IN_PROGRESS
                    || mission.getStatus() == MissionStatus.COMPLETED;
            case "DEPLOYED" -> mission.getStatus() == MissionStatus.IN_PROGRESS
                    || mission.getStatus() == MissionStatus.COMPLETED;
            case "IN_PROGRESS" -> mission.getStatus() == MissionStatus.IN_PROGRESS
                    || mission.getStatus() == MissionStatus.COMPLETED;
            case "COMPLETED" -> mission.getStatus() == MissionStatus.COMPLETED;
            case "CREATED" -> true;
            default -> false;
        };
        boolean done = found != null || doneByStatus;
        return new MilestoneDTO(label, eventType,
                found != null ? found.getOccurredAt() : null, done);
    }

    private boolean matches(MissionEvent event, String eventType) {
        if ("DEPLOYED".equals(eventType)) {
            return event.getEventType() == MissionEventType.DEPLOYED
                    || (event.getEventType() == MissionEventType.RESOURCE_ALLOCATED
                    && event.getMessage() != null && event.getMessage().toLowerCase().contains("vehicle"));
        }
        if ("RESOURCE_ALLOCATED".equals(eventType)) {
            return event.getEventType() == MissionEventType.RESOURCE_ALLOCATED
                    && (event.getMessage() == null || !event.getMessage().toLowerCase().contains("vehicle"));
        }
        if ("IN_PROGRESS".equals(eventType)) {
            return event.getEventType() == MissionEventType.STATUS_CHANGED
                    && event.getMessage() != null && event.getMessage().toLowerCase().contains("in progress");
        }
        if ("COMPLETED".equals(eventType)) {
            return event.getEventType() == MissionEventType.COMPLETED
                    || (event.getEventType() == MissionEventType.STATUS_CHANGED
                    && event.getMessage() != null && event.getMessage().toLowerCase().contains("completed"));
        }
        if ("ASSIGNED".equals(eventType)) {
            return event.getEventType() == MissionEventType.ASSIGNED
                    || (event.getEventType() == MissionEventType.STATUS_CHANGED
                    && event.getMessage() != null && event.getMessage().toLowerCase().contains("assigned to team"));
        }
        return event.getEventType() != null && event.getEventType().name().equals(eventType);
    }

    private int computeProgress(RescueMission mission) {
        int progress = 10;
        if (mission.getStatus() == MissionStatus.COMPLETED) return 100;
        if (mission.getStatus() == MissionStatus.CANCELLED) return 0;
        if (mission.getStatus() == MissionStatus.IN_PROGRESS) progress = Math.max(progress, 75);
        if (mission.getStatus() == MissionStatus.ASSIGNED) progress = Math.max(progress, 25);
        return progress;
    }

    // ------------------------------------------------------------------
    // Operational alerts
    // ------------------------------------------------------------------

    private List<OperationalAlertDTO> buildAlerts(List<RescueTeam> teams, List<RescueMission> activeMissions,
                                                  List<RescueVehicle> vehicles, List<RescueEquipment> equipment,
                                                  Map<Long, TeamLocationUpdate> latestLocations) {
        List<OperationalAlertDTO> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Delayed missions
        activeMissions.stream()
                .filter(m -> m.getEndTime() != null && now.isAfter(m.getEndTime()))
                .forEach(m -> alerts.add(new OperationalAlertDTO("CRITICAL", "MISSION_DELAYED",
                        "Mission " + m.getMissionCode() + " (" + m.getTitle() + ") is overdue",
                        "MISSION", m.getId(), m.getTitle(), now)));

        // Low fuel vehicles
        vehicles.stream().filter(v -> v.getFuelLevel() < LOW_FUEL_THRESHOLD)
                .forEach(v -> alerts.add(new OperationalAlertDTO("WARNING", "VEHICLE_LOW_FUEL",
                        "Vehicle " + v.getRegistrationNumber() + " has " + v.getFuelLevel() + "% fuel left",
                        "VEHICLE", v.getId(), v.getRegistrationNumber(), now)));

        // Maintenance due
        vehicles.stream().filter(v -> v.getNextMaintenanceDue() != null && now.isAfter(v.getNextMaintenanceDue()))
                .forEach(v -> alerts.add(new OperationalAlertDTO("WARNING", "VEHICLE_MAINTENANCE",
                        "Vehicle " + v.getRegistrationNumber() + " is due for maintenance",
                        "VEHICLE", v.getId(), v.getRegistrationNumber(), now)));

        // Equipment shortage
        Map<Long, List<RescueEquipment>> eqByTeam = equipment.stream()
                .filter(e -> e.getTeam() != null).collect(Collectors.groupingBy(e -> e.getTeam().getId()));
        teams.forEach(t -> {
            int total = eqByTeam.getOrDefault(t.getId(), List.of()).stream().mapToInt(RescueEquipment::getTotalQuantity).sum();
            int available = eqByTeam.getOrDefault(t.getId(), List.of()).stream().mapToInt(RescueEquipment::getAvailableQuantity).sum();
            if (total > 0 && (available * 100.0 / total) < EQUIPMENT_SHORTAGE_THRESHOLD) {
                alerts.add(new OperationalAlertDTO("WARNING", "EQUIPMENT_SHORTAGE",
                        "Team " + t.getTeamName() + " equipment readiness below " + EQUIPMENT_SHORTAGE_THRESHOLD + "%",
                        "TEAM", t.getId(), t.getTeamName(), now));
            }
        });

        // Inactive / overdue teams
        teams.forEach(t -> {
            boolean deployed = TeamStatus.ON_MISSION.name().equals(t.getStatus())
                    || TeamStatus.DEPLOYED.name().equals(t.getStatus());
            LocalDateTime last = latestLocations.containsKey(t.getId())
                    ? latestLocations.get(t.getId()).getTimestamp() : t.getLastLocationUpdateAt();
            if (deployed && (last == null || Duration.between(last, now).toHours() > INACTIVE_TEAM_HOURS)) {
                alerts.add(new OperationalAlertDTO("WARNING", "TEAM_INACTIVE",
                        "Team " + t.getTeamName() + " has no live location for over " + INACTIVE_TEAM_HOURS + " hours",
                        "TEAM", t.getId(), t.getTeamName(), now));
            }
            if (TeamStatus.DEPLOYED.name().equals(t.getStatus())) {
                alerts.add(new OperationalAlertDTO("INFO", "TEAM_DEPLOYED",
                        "Team " + t.getTeamName() + " is on emergency deployment",
                        "TEAM", t.getId(), t.getTeamName(), now));
            }
        });

        java.util.function.ToIntFunction<OperationalAlertDTO> severityRank =
                a -> a.getSeverity().equals("CRITICAL") ? 0 : a.getSeverity().equals("WARNING") ? 1 : 2;
        return alerts.stream()
                .sorted(Comparator.comparingInt(severityRank)
                        .thenComparing(Comparator.comparing(OperationalAlertDTO::getTimestamp).reversed()))
                .limit(15)
                .toList();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Map<Long, TeamLocationUpdate> latestLocationByTeam() {
        Map<Long, TeamLocationUpdate> latest = new LinkedHashMap<>();
        for (TeamLocationUpdate update : locationRepository.findAllByOrderByTimestampDesc()) {
            if (update.getTeam() != null) {
                latest.putIfAbsent(update.getTeam().getId(), update);
            }
        }
        return latest;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
