package com.disaster.repository;

import com.disaster.entity.RescueTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RescueTeamRepository extends JpaRepository<RescueTeam, Long> {
    List<RescueTeam> findByStatus(String status);
    List<RescueTeam> findByAssignedDisasterId(Long disasterId);
    List<RescueTeam> findByStatusIn(List<String> statuses);
}
