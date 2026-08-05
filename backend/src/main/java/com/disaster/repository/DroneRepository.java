package com.disaster.repository;

import com.disaster.entity.Drone;
import com.disaster.enums.DroneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DroneRepository extends JpaRepository<Drone, Long> {
    List<Drone> findByStatus(DroneStatus status);
    List<Drone> findByAssignedDisasterId(Long disasterId);
}
