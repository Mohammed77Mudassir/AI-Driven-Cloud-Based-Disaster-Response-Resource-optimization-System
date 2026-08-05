package com.disaster.service;

import com.disaster.dto.RescueTeamDTO;
import com.disaster.dto.TeamAvailabilityDTO;
import com.disaster.dto.TeamMemberDTO;
import java.util.List;

public interface RescueTeamService {
    List<RescueTeamDTO> getAllTeams();
    RescueTeamDTO getTeamById(Long id);
    RescueTeamDTO createTeam(RescueTeamDTO dto);
    RescueTeamDTO updateTeam(Long id, RescueTeamDTO dto);
    void deleteTeam(Long id);
    RescueTeamDTO assignToDisaster(Long teamId, Long disasterId);
    RescueTeamDTO updateTeamStatus(Long teamId, String status);
    List<RescueTeamDTO> getTeamsByStatus(String status);
    List<RescueTeamDTO> getTeamsByDisaster(Long disasterId);
    List<TeamMemberDTO> getTeamMembers(Long teamId);
    TeamMemberDTO addTeamMember(Long teamId, TeamMemberDTO dto);
    TeamMemberDTO updateTeamMember(Long memberId, TeamMemberDTO dto);
    void removeTeamMember(Long memberId);
    RescueTeamDTO assignLeader(Long teamId, Long memberId);
    TeamAvailabilityDTO getTeamAvailability(Long teamId);
    List<TeamAvailabilityDTO> getAvailabilityOverview();
}
