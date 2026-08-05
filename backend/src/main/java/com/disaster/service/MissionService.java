package com.disaster.service;

import com.disaster.dto.MissionEventDTO;
import com.disaster.dto.RescueMissionDTO;
import com.disaster.enums.MissionStatus;
import java.util.List;

public interface MissionService {
    List<RescueMissionDTO> getAll();
    RescueMissionDTO getById(Long id);
    RescueMissionDTO create(RescueMissionDTO dto, String actor);
    RescueMissionDTO update(Long id, RescueMissionDTO dto);
    void delete(Long id);
    List<RescueMissionDTO> getByTeam(Long teamId);
    List<RescueMissionDTO> getByTeamAndStatus(Long teamId, MissionStatus status);
    List<RescueMissionDTO> getByDisaster(Long disasterId);
    List<RescueMissionDTO> getByStatus(MissionStatus status);
    RescueMissionDTO updateStatus(Long id, MissionStatus status, String reason, String actor);
    List<MissionEventDTO> getEvents(Long missionId);
    List<MissionStatus> getAllowedTransitions(MissionStatus status);
}
