package com.disaster.service;

import com.disaster.dto.DutyRosterSummaryDTO;
import com.disaster.dto.TeamShiftDTO;
import com.disaster.entity.RescueTeam;
import com.disaster.entity.TeamMember;
import com.disaster.entity.TeamShift;
import com.disaster.enums.ShiftStatus;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.RescueTeamRepository;
import com.disaster.repository.TeamMemberRepository;
import com.disaster.repository.TeamShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ShiftServiceImpl implements ShiftService {

    private final TeamShiftRepository shiftRepository;
    private final RescueTeamRepository teamRepository;
    private final TeamMemberRepository memberRepository;

    public ShiftServiceImpl(TeamShiftRepository shiftRepository,
                            RescueTeamRepository teamRepository,
                            TeamMemberRepository memberRepository) {
        this.shiftRepository = shiftRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamShiftDTO> getAll() {
        return shiftRepository.findAll().stream().map(TeamShiftDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamShiftDTO getById(Long id) {
        return TeamShiftDTO.fromEntity(findShift(id));
    }

    @Override
    @Transactional
    public TeamShiftDTO create(TeamShiftDTO dto, String actor) {
        if (dto.getTeamId() == null) {
            throw new IllegalArgumentException("A team is required for a shift");
        }
        if (dto.getShiftStart() == null || dto.getShiftEnd() == null) {
            throw new IllegalArgumentException("Shift start and end times are required");
        }
        if (!dto.getShiftEnd().isAfter(dto.getShiftStart())) {
            throw new IllegalArgumentException("Shift end must be after shift start");
        }
        RescueTeam team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + dto.getTeamId()));
        TeamShift shift = new TeamShift();
        shift.setTeam(team);
        if (dto.getMemberId() != null) {
            TeamMember member = memberRepository.findById(dto.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + dto.getMemberId()));
            shift.setMember(member);
        }
        shift.setShiftType(dto.getShiftType());
        shift.setShiftStatus(dto.getShiftStatus() != null ? dto.getShiftStatus() : ShiftStatus.SCHEDULED);
        shift.setShiftStart(dto.getShiftStart());
        shift.setShiftEnd(dto.getShiftEnd());
        shift.setNotes(dto.getNotes());
        shift.setCreatedBy(actor);
        return TeamShiftDTO.fromEntity(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public TeamShiftDTO update(Long id, TeamShiftDTO dto) {
        TeamShift shift = findShift(id);
        if (dto.getShiftType() != null) shift.setShiftType(dto.getShiftType());
        if (dto.getShiftStatus() != null) shift.setShiftStatus(dto.getShiftStatus());
        if (dto.getShiftStart() != null) shift.setShiftStart(dto.getShiftStart());
        if (dto.getShiftEnd() != null) shift.setShiftEnd(dto.getShiftEnd());
        if (dto.getNotes() != null) shift.setNotes(dto.getNotes());
        if (shift.getShiftStart() != null && shift.getShiftEnd() != null
                && !shift.getShiftEnd().isAfter(shift.getShiftStart())) {
            throw new IllegalArgumentException("Shift end must be after shift start");
        }
        return TeamShiftDTO.fromEntity(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        shiftRepository.delete(findShift(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamShiftDTO> getByTeam(Long teamId) {
        return shiftRepository.findByTeamIdOrderByShiftStartDesc(teamId).stream().map(TeamShiftDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamShiftDTO> getByMember(Long memberId) {
        return shiftRepository.findByMemberIdOrderByShiftStartDesc(memberId).stream().map(TeamShiftDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamShiftDTO> getRosterForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return shiftRepository.findByShiftStartLessThanEqualAndShiftEndGreaterThanEqualOrderByShiftStartAsc(end, start)
                .stream().map(TeamShiftDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamShiftDTO> getRosterForRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' dates are required for a roster range");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' date must be on or after the 'from' date");
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return shiftRepository.findByShiftStartLessThanEqualAndShiftEndGreaterThanEqualOrderByShiftStartAsc(end, start)
                .stream().map(TeamShiftDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public TeamShiftDTO updateStatus(Long id, ShiftStatus status) {
        TeamShift shift = findShift(id);
        if (shift.getShiftStatus() == ShiftStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot change a completed shift");
        }
        shift.setShiftStatus(status);
        TeamMember member = shift.getMember();
        if (member != null) {
            if (status == ShiftStatus.ACTIVE) {
                member.setAvailable(false);
            } else if (status == ShiftStatus.COMPLETED || status == ShiftStatus.CANCELLED) {
                member.setAvailable(true);
            }
            memberRepository.save(member);
        }
        return TeamShiftDTO.fromEntity(shiftRepository.save(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public DutyRosterSummaryDTO getDutyRoster(LocalDate date) {
        LocalDate rosterDate = date != null ? date : LocalDate.now();
        List<TeamShiftDTO> shifts = getRosterForDate(rosterDate);
        long distinctMembers = shifts.stream()
                .map(TeamShiftDTO::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long onDuty = shifts.stream()
                .filter(s -> s.getShiftStatus() == ShiftStatus.ACTIVE)
                .count();
        return DutyRosterSummaryDTO.of(rosterDate, (int) distinctMembers, (int) onDuty, shifts);
    }

    private TeamShift findShift(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
    }
}
