package com.disaster.repository;

import com.disaster.entity.RescueEquipment;
import com.disaster.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RescueEquipmentRepository extends JpaRepository<RescueEquipment, Long> {
    List<RescueEquipment> findByTeamId(Long teamId);
    List<RescueEquipment> findByStatus(EquipmentStatus status);
    List<RescueEquipment> findByTeamIdAndStatus(Long teamId, EquipmentStatus status);
    List<RescueEquipment> findByAssignedMissionId(Long missionId);
    long countByTeamIdAndStatus(Long teamId, EquipmentStatus status);
}
