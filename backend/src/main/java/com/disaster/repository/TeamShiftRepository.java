package com.disaster.repository;

import com.disaster.entity.TeamShift;
import com.disaster.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TeamShiftRepository extends JpaRepository<TeamShift, Long> {
    List<TeamShift> findByTeamIdOrderByShiftStartDesc(Long teamId);
    List<TeamShift> findByMemberIdOrderByShiftStartDesc(Long memberId);
    List<TeamShift> findByShiftStartLessThanEqualAndShiftEndGreaterThanEqualOrderByShiftStartAsc(
            LocalDateTime end, LocalDateTime start);
    List<TeamShift> findByShiftStartGreaterThanEqualAndShiftEndLessThanEqualOrderByShiftStartAsc(
            LocalDateTime from, LocalDateTime to);
    List<TeamShift> findByShiftStatus(ShiftStatus status);
}
