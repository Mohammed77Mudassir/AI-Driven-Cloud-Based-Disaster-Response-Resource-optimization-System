package com.disaster.service;

import com.disaster.dto.RescueEquipmentDTO;
import com.disaster.enums.EquipmentStatus;
import java.util.List;

public interface EquipmentService {
    List<RescueEquipmentDTO> getAll();
    RescueEquipmentDTO getById(Long id);
    RescueEquipmentDTO create(RescueEquipmentDTO dto);
    RescueEquipmentDTO update(Long id, RescueEquipmentDTO dto);
    void delete(Long id);
    List<RescueEquipmentDTO> getByTeam(Long teamId);
    List<RescueEquipmentDTO> getByStatus(EquipmentStatus status);
    List<RescueEquipmentDTO> getByMission(Long missionId);
    RescueEquipmentDTO deploy(Long id, int quantity, Long missionId, String actor);
    RescueEquipmentDTO returnEquipment(Long id, int quantity, String actor);
    RescueEquipmentDTO startMaintenance(Long id, int quantity, String actor);
    RescueEquipmentDTO completeMaintenance(Long id, int quantity, String actor);
}
