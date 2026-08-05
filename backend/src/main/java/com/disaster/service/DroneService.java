package com.disaster.service;

import com.disaster.dto.DroneDTO;
import com.disaster.entity.Drone;
import com.disaster.entity.Disaster;
import com.disaster.enums.DroneStatus;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.DroneRepository;
import com.disaster.repository.DisasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DroneService {
    private final DroneRepository droneRepository;
    private final DisasterRepository disasterRepository;
    
    public DroneService(DroneRepository droneRepository, DisasterRepository disasterRepository) {
        this.droneRepository = droneRepository;
        this.disasterRepository = disasterRepository;
    }
    
    @Transactional(readOnly = true)
    public List<DroneDTO> getAll() {
        return droneRepository.findAll().stream().map(DroneDTO::fromEntity).toList();
    }
    
    @Transactional(readOnly = true)
    public DroneDTO getById(Long id) {
        return DroneDTO.fromEntity(droneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Drone not found")));
    }
    
    public DroneDTO create(DroneDTO dto) {
        Drone d = new Drone();
        d.setDroneId(dto.getDroneId());
        d.setStatus(dto.getStatus() != null ? dto.getStatus() : DroneStatus.AVAILABLE);
        d.setBattery(dto.getBattery());
        d.setCameraStatus(dto.isCameraStatus());
        d.setLatitude(dto.getLatitude());
        d.setLongitude(dto.getLongitude());
        d.setMissionStatus(dto.getMissionStatus());
        return DroneDTO.fromEntity(droneRepository.save(d));
    }
    
    public DroneDTO update(Long id, DroneDTO dto) {
        Drone d = droneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
        d.setDroneId(dto.getDroneId());
        d.setStatus(dto.getStatus());
        d.setBattery(dto.getBattery());
        d.setCameraStatus(dto.isCameraStatus());
        d.setLatitude(dto.getLatitude());
        d.setLongitude(dto.getLongitude());
        d.setMissionStatus(dto.getMissionStatus());
        if (dto.getAssignedDisasterId() != null) {
            Disaster disaster = disasterRepository.findById(dto.getAssignedDisasterId()).orElse(null);
            d.setAssignedDisaster(disaster);
        }
        return DroneDTO.fromEntity(droneRepository.save(d));
    }
    
    public void delete(Long id) {
        droneRepository.deleteById(id);
    }
    
    public DroneDTO updateLocation(Long id, double latitude, double longitude) {
        Drone d = droneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Drone not found"));
        d.setLatitude(latitude);
        d.setLongitude(longitude);
        return DroneDTO.fromEntity(droneRepository.save(d));
    }
}
