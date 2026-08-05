package com.disaster.repository;

import com.disaster.entity.MissionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MissionEventRepository extends JpaRepository<MissionEvent, Long> {
    List<MissionEvent> findByMissionIdOrderByOccurredAtDesc(Long missionId);
    void deleteByMissionId(Long missionId);
}
