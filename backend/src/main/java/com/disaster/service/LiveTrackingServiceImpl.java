package com.disaster.service;

import com.disaster.config.WebSocketConfig;
import com.disaster.dto.TeamLocationDTO;
import com.disaster.entity.RescueTeam;
import com.disaster.entity.TeamLocationUpdate;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.RescueTeamRepository;
import com.disaster.repository.TeamLocationUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LiveTrackingServiceImpl implements LiveTrackingService {

    private final TeamLocationUpdateRepository locationRepository;
    private final RescueTeamRepository teamRepository;
    private final WebSocketConfig webSocketConfig;

    public LiveTrackingServiceImpl(TeamLocationUpdateRepository locationRepository,
                                   RescueTeamRepository teamRepository,
                                   WebSocketConfig webSocketConfig) {
        this.locationRepository = locationRepository;
        this.teamRepository = teamRepository;
        this.webSocketConfig = webSocketConfig;
    }

    @Override
    @Transactional
    public TeamLocationDTO recordLocation(Long teamId, TeamLocationDTO dto) {
        RescueTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + teamId));

        TeamLocationUpdate update = new TeamLocationUpdate();
        update.setTeam(team);
        update.setLatitude(dto.getLatitude());
        update.setLongitude(dto.getLongitude());
        update.setHeading(dto.getHeading());
        update.setSpeed(dto.getSpeed());
        update.setAccuracy(dto.getAccuracy());
        update.setDeviceId(dto.getDeviceId());
        update.setTimestamp(LocalDateTime.now());
        update = locationRepository.save(update);

        team.setLatitude(dto.getLatitude());
        team.setLongitude(dto.getLongitude());
        team.setLastLocationUpdateAt(update.getTimestamp());
        teamRepository.save(team);

        TeamLocationDTO result = TeamLocationDTO.fromEntity(update);
        webSocketConfig.broadcastUpdate("TEAM_LOCATION", result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationDTO> getHistory(Long teamId) {
        return locationRepository.findTop20ByTeamIdOrderByTimestampDesc(teamId)
                .stream().map(TeamLocationDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationDTO> getLatestForTeam(Long teamId) {
        List<TeamLocationDTO> history = getHistory(teamId);
        if (history.isEmpty()) return List.of();
        return List.of(history.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamLocationDTO> getLatestForAll() {
        Map<Long, TeamLocationDTO> latestByTeam = locationRepository.findAllByOrderByTimestampDesc().stream()
                .map(TeamLocationDTO::fromEntity)
                .collect(Collectors.toMap(
                        TeamLocationDTO::getTeamId,
                        dto -> dto,
                        (first, second) -> first,
                        java.util.LinkedHashMap::new));
        return latestByTeam.values().stream().toList();
    }
}
