package com.disaster.repository;

import com.disaster.entity.StatusTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusTimelineRepository extends JpaRepository<StatusTimeline, Long> {
    List<StatusTimeline> findByDisasterIdOrderByChangedAtAsc(Long disasterId);
    void deleteByDisasterId(Long disasterId);
}
