package com.disaster.service;

import com.disaster.dto.TeamLocationDTO;
import java.util.List;

public interface LiveTrackingService {
    TeamLocationDTO recordLocation(Long teamId, TeamLocationDTO dto);
    List<TeamLocationDTO> getHistory(Long teamId);
    List<TeamLocationDTO> getLatestForTeam(Long teamId);
    List<TeamLocationDTO> getLatestForAll();
}
