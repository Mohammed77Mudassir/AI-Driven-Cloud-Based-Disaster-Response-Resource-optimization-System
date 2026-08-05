package com.disaster.repository;

import com.disaster.entity.RescueMission;
import com.disaster.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RescueMissionRepository extends JpaRepository<RescueMission, Long> {
    List<RescueMission> findByTeamIdOrderByIdDesc(Long teamId);
    List<RescueMission> findByDisasterIdOrderByIdDesc(Long disasterId);
    List<RescueMission> findByStatusOrderByIdDesc(MissionStatus status);
    List<RescueMission> findByTeamIdAndStatusOrderByIdDesc(Long teamId, MissionStatus status);
    List<RescueMission> findByTeamIdAndStatusInOrderByIdDesc(Long teamId, List<MissionStatus> statuses);
    List<RescueMission> findAllByOrderByIdDesc();
    long countByStatus(MissionStatus status);
}
