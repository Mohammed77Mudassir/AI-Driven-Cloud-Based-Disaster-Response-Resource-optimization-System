package com.disaster.repository;

import com.disaster.entity.TeamLocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamLocationUpdateRepository extends JpaRepository<TeamLocationUpdate, Long> {
    List<TeamLocationUpdate> findTop20ByTeamIdOrderByTimestampDesc(Long teamId);
    List<TeamLocationUpdate> findAllByOrderByTimestampDesc();
}
