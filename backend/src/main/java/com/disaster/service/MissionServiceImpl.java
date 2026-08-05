package com.disaster.service;

import com.disaster.config.WebSocketConfig;
import com.disaster.dto.MissionEventDTO;
import com.disaster.dto.RescueMissionDTO;
import com.disaster.entity.Disaster;
import com.disaster.entity.MissionEvent;
import com.disaster.entity.RescueMission;
import com.disaster.entity.RescueTeam;
import com.disaster.enums.MissionEventType;
import com.disaster.enums.MissionStatus;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.DisasterRepository;
import com.disaster.repository.MissionEventRepository;
import com.disaster.repository.RescueMissionRepository;
import com.disaster.repository.RescueTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MissionServiceImpl implements MissionService {

    private static final Map<MissionStatus, List<MissionStatus>> ALLOWED_TRANSITIONS = Map.of(
            MissionStatus.PENDING, List.of(MissionStatus.ASSIGNED, MissionStatus.CANCELLED),
            MissionStatus.ASSIGNED, List.of(MissionStatus.IN_PROGRESS, MissionStatus.CANCELLED),
            MissionStatus.IN_PROGRESS, List.of(MissionStatus.COMPLETED, MissionStatus.CANCELLED),
            MissionStatus.COMPLETED, List.of(),
            MissionStatus.CANCELLED, List.of()
    );

    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final RescueMissionRepository missionRepository;
    private final MissionEventRepository eventRepository;
    private final RescueTeamRepository teamRepository;
    private final DisasterRepository disasterRepository;
    private final WebSocketConfig webSocketConfig;

    public MissionServiceImpl(RescueMissionRepository missionRepository,
                              MissionEventRepository eventRepository,
                              RescueTeamRepository teamRepository,
                              DisasterRepository disasterRepository,
                              WebSocketConfig webSocketConfig) {
        this.missionRepository = missionRepository;
        this.eventRepository = eventRepository;
        this.teamRepository = teamRepository;
        this.disasterRepository = disasterRepository;
        this.webSocketConfig = webSocketConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueMissionDTO> getAll() {
        return missionRepository.findAllByOrderByIdDesc().stream().map(RescueMissionDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RescueMissionDTO getById(Long id) {
        return RescueMissionDTO.fromEntity(findMission(id));
    }

    @Override
    @Transactional
    public RescueMissionDTO create(RescueMissionDTO dto, String actor) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Mission title is required");
        }
        if (dto.getTeamId() == null) {
            throw new IllegalArgumentException("A rescue team must be assigned to the mission");
        }
        if (dto.getDisasterId() == null) {
            throw new IllegalArgumentException("A disaster must be assigned to the mission");
        }
        RescueTeam team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + dto.getTeamId()));
        Disaster disaster = disasterRepository.findById(dto.getDisasterId())
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found with id: " + dto.getDisasterId()));

        RescueMission mission = new RescueMission();
        mission.setMissionCode(generateMissionCode());
        mission.setTitle(dto.getTitle());
        mission.setMissionType(dto.getMissionType());
        mission.setDescription(dto.getDescription());
        mission.setTeam(team);
        mission.setDisaster(disaster);
        mission.setStatus(MissionStatus.PENDING);
        mission.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        mission.setInstructions(dto.getInstructions());
        mission.setAssignedBy(actor);
        mission.setCreatedBy(actor);
        mission.setCreatedAt(LocalDateTime.now());
        mission.setStartTime(dto.getStartTime());
        mission.setEndTime(dto.getEndTime());
        mission = missionRepository.save(mission);

        recordEvent(mission, MissionEventType.CREATED, "Mission " + mission.getMissionCode() + " created and assigned to team " + team.getTeamName(), actor);
        webSocketConfig.broadcastUpdate("MISSION_CREATED", RescueMissionDTO.fromEntity(mission));
        return RescueMissionDTO.fromEntity(mission);
    }

    @Override
    @Transactional
    public RescueMissionDTO update(Long id, RescueMissionDTO dto) {
        RescueMission mission = findMission(id);
        if (dto.getTitle() != null) mission.setTitle(dto.getTitle());
        if (dto.getMissionType() != null) mission.setMissionType(dto.getMissionType());
        if (dto.getDescription() != null) mission.setDescription(dto.getDescription());
        if (dto.getInstructions() != null) mission.setInstructions(dto.getInstructions());
        if (dto.getPriority() != null) mission.setPriority(dto.getPriority());
        if (dto.getStartTime() != null) mission.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) mission.setEndTime(dto.getEndTime());
        return RescueMissionDTO.fromEntity(missionRepository.save(mission));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        eventRepository.deleteByMissionId(id);
        missionRepository.delete(findMission(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueMissionDTO> getByTeam(Long teamId) {
        return missionRepository.findByTeamIdOrderByIdDesc(teamId).stream().map(RescueMissionDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueMissionDTO> getByTeamAndStatus(Long teamId, MissionStatus status) {
        return missionRepository.findByTeamIdAndStatusOrderByIdDesc(teamId, status)
                .stream().map(RescueMissionDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueMissionDTO> getByDisaster(Long disasterId) {
        return missionRepository.findByDisasterIdOrderByIdDesc(disasterId).stream().map(RescueMissionDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueMissionDTO> getByStatus(MissionStatus status) {
        return missionRepository.findByStatusOrderByIdDesc(status).stream().map(RescueMissionDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public RescueMissionDTO updateStatus(Long id, MissionStatus status, String reason, String actor) {
        RescueMission mission = findMission(id);
        if (mission.getStatus() == status) {
            throw new IllegalArgumentException("Mission is already in status " + status);
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(mission.getStatus(), List.of()).contains(status)) {
            throw new IllegalArgumentException("Invalid mission status transition: " + mission.getStatus() + " -> " + status);
        }

        RescueTeam team = mission.getTeam();
        MissionStatus previous = mission.getStatus();
        mission.setStatus(status);
        String eventMessage = "Mission status changed from " + previous + " to " + status;

        switch (status) {
            case ASSIGNED -> {
                eventMessage = "Mission acknowledged and assigned to team " + (team != null ? team.getTeamName() : "");
                if (team != null) team.setStatus("STANDING_BY");
            }
            case IN_PROGRESS -> {
                mission.setStartTime(LocalDateTime.now());
                eventMessage = "Mission is now in progress";
                if (team != null) {
                    team.setStatus("ON_MISSION");
                    team.setDeployedAt(LocalDateTime.now());
                    team.setReturnedAt(null);
                    if (mission.getDisaster() != null) {
                        team.setAssignedDisaster(mission.getDisaster());
                    }
                }
            }
            case COMPLETED -> {
                mission.setCompletedAt(LocalDateTime.now());
                eventMessage = "Mission completed successfully";
                if (team != null) {
                    team.setStatus("AVAILABLE");
                    team.setReturnedAt(LocalDateTime.now());
                    team.setAssignedDisaster(null);
                }
            }
            case CANCELLED -> {
                mission.setCancelledAt(LocalDateTime.now());
                mission.setCancelledReason(reason);
                eventMessage = "Mission cancelled" + (reason != null && !reason.isBlank() ? " - " + reason : "");
                if (team != null) {
                    team.setStatus("AVAILABLE");
                    team.setAssignedDisaster(null);
                }
            }
            default -> { }
        }
        if (team != null) teamRepository.save(team);
        mission = missionRepository.save(mission);
        recordEvent(mission, MissionEventType.STATUS_CHANGED, eventMessage, actor);
        webSocketConfig.broadcastUpdate("MISSION_STATUS", RescueMissionDTO.fromEntity(mission));
        return RescueMissionDTO.fromEntity(mission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionEventDTO> getEvents(Long missionId) {
        return eventRepository.findByMissionIdOrderByOccurredAtDesc(missionId)
                .stream().map(MissionEventDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionStatus> getAllowedTransitions(MissionStatus status) {
        return ALLOWED_TRANSITIONS.getOrDefault(status, List.of());
    }

    @Transactional
    public void recordEvent(RescueMission mission, MissionEventType type, String message, String actor) {
        MissionEvent event = new MissionEvent();
        event.setMission(mission);
        event.setEventType(type);
        event.setMessage(message);
        event.setPerformedBy(actor != null ? actor : "system");
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    private RescueMission findMission(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + id));
    }

    private String generateMissionCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            suffix.append(ALPHANUM.charAt(ThreadLocalRandom.current().nextInt(ALPHANUM.length())));
        }
        return "MSN-" + datePart + "-" + suffix;
    }
}
