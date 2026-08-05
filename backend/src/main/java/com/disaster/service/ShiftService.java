package com.disaster.service;

import com.disaster.dto.DutyRosterSummaryDTO;
import com.disaster.dto.TeamShiftDTO;
import com.disaster.enums.ShiftStatus;
import java.time.LocalDate;
import java.util.List;

public interface ShiftService {
    List<TeamShiftDTO> getAll();
    TeamShiftDTO getById(Long id);
    TeamShiftDTO create(TeamShiftDTO dto, String actor);
    TeamShiftDTO update(Long id, TeamShiftDTO dto);
    void delete(Long id);
    List<TeamShiftDTO> getByTeam(Long teamId);
    List<TeamShiftDTO> getByMember(Long memberId);
    List<TeamShiftDTO> getRosterForDate(LocalDate date);
    List<TeamShiftDTO> getRosterForRange(LocalDate from, LocalDate to);
    TeamShiftDTO updateStatus(Long id, ShiftStatus status);
    DutyRosterSummaryDTO getDutyRoster(LocalDate date);
}
