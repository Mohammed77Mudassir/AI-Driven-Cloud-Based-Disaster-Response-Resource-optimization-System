package com.disaster.repository;

import com.disaster.entity.RescueVehicle;
import com.disaster.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RescueVehicleRepository extends JpaRepository<RescueVehicle, Long> {
    List<RescueVehicle> findByTeamId(Long teamId);
    List<RescueVehicle> findByStatus(VehicleStatus status);
    List<RescueVehicle> findByTeamIdAndStatus(Long teamId, VehicleStatus status);
    List<RescueVehicle> findByAssignedMissionId(Long missionId);
    long countByTeamIdAndStatus(Long teamId, VehicleStatus status);
}
