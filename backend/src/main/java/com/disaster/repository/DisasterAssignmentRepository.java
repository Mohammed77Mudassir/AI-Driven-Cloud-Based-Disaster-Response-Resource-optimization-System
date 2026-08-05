package com.disaster.repository;

import com.disaster.entity.DisasterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisasterAssignmentRepository extends JpaRepository<DisasterAssignment, Long> {
    List<DisasterAssignment> findByDisasterIdOrderByAssignedAtDesc(Long disasterId);
    void deleteByDisasterId(Long disasterId);
}
