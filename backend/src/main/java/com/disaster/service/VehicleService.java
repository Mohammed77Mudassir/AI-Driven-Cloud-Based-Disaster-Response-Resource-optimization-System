package com.disaster.service;

import com.disaster.dto.RescueVehicleDTO;
import com.disaster.enums.VehicleStatus;
import java.util.List;

public interface VehicleService {
    List<RescueVehicleDTO> getAll();
    RescueVehicleDTO getById(Long id);
    RescueVehicleDTO create(RescueVehicleDTO dto);
    RescueVehicleDTO update(Long id, RescueVehicleDTO dto);
    void delete(Long id);
    List<RescueVehicleDTO> getByTeam(Long teamId);
    List<RescueVehicleDTO> getByStatus(VehicleStatus status);
    List<RescueVehicleDTO> getByMission(Long missionId);
    RescueVehicleDTO deploy(Long id, Long missionId, String actor);
    RescueVehicleDTO returnVehicle(Long id, String actor);
    RescueVehicleDTO startMaintenance(Long id, String actor);
    RescueVehicleDTO completeMaintenance(Long id, String actor);
}
