package com.disaster.service;

import com.disaster.dto.RescueVehicleDTO;
import com.disaster.entity.RescueMission;
import com.disaster.entity.RescueTeam;
import com.disaster.entity.RescueVehicle;
import com.disaster.enums.MissionEventType;
import com.disaster.enums.VehicleStatus;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.MissionEventRepository;
import com.disaster.repository.RescueMissionRepository;
import com.disaster.repository.RescueTeamRepository;
import com.disaster.repository.RescueVehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final RescueVehicleRepository vehicleRepository;
    private final RescueTeamRepository teamRepository;
    private final RescueMissionRepository missionRepository;
    private final MissionEventRepository missionEventRepository;

    public VehicleServiceImpl(RescueVehicleRepository vehicleRepository,
                              RescueTeamRepository teamRepository,
                              RescueMissionRepository missionRepository,
                              MissionEventRepository missionEventRepository) {
        this.vehicleRepository = vehicleRepository;
        this.teamRepository = teamRepository;
        this.missionRepository = missionRepository;
        this.missionEventRepository = missionEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueVehicleDTO> getAll() {
        return vehicleRepository.findAll().stream().map(RescueVehicleDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RescueVehicleDTO getById(Long id) {
        return RescueVehicleDTO.fromEntity(findVehicle(id));
    }

    @Override
    @Transactional
    public RescueVehicleDTO create(RescueVehicleDTO dto) {
        if (dto.getRegistrationNumber() == null || dto.getRegistrationNumber().isBlank()) {
            throw new IllegalArgumentException("Registration number is required");
        }
        RescueVehicle vehicle = new RescueVehicle();
        apply(vehicle, dto);
        vehicle.setStatus(dto.getStatus() != null ? dto.getStatus() : VehicleStatus.AVAILABLE);
        vehicle.setFuelLevel(Math.max(0, Math.min(100, dto.getFuelLevel())));
        return RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public RescueVehicleDTO update(Long id, RescueVehicleDTO dto) {
        RescueVehicle vehicle = findVehicle(id);
        if (dto.getRegistrationNumber() != null) {
            if (dto.getRegistrationNumber().isBlank()) {
                throw new IllegalArgumentException("Registration number cannot be blank");
            }
            vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        }
        apply(vehicle, dto);
        return RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
    }

    private void apply(RescueVehicle vehicle, RescueVehicleDTO dto) {
        if (dto.getTeamId() != null) {
            RescueTeam team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + dto.getTeamId()));
            vehicle.setTeam(team);
        }
        if (dto.getVehicleType() != null) vehicle.setVehicleType(dto.getVehicleType());
        if (dto.getModel() != null) vehicle.setModel(dto.getModel());
        if (dto.getCapacity() > 0) vehicle.setCapacity(dto.getCapacity());
        if (dto.getStatus() != null) vehicle.setStatus(dto.getStatus());
        vehicle.setLatitude(dto.getLatitude());
        vehicle.setLongitude(dto.getLongitude());
        if (dto.getNotes() != null) vehicle.setNotes(dto.getNotes());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        vehicleRepository.delete(findVehicle(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueVehicleDTO> getByTeam(Long teamId) {
        return vehicleRepository.findByTeamId(teamId).stream().map(RescueVehicleDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueVehicleDTO> getByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status).stream().map(RescueVehicleDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueVehicleDTO> getByMission(Long missionId) {
        return vehicleRepository.findByAssignedMissionId(missionId).stream().map(RescueVehicleDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public RescueVehicleDTO deploy(Long id, Long missionId, String actor) {
        RescueVehicle vehicle = findVehicle(id);
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalArgumentException("Vehicle is not available for deployment (current status: " + vehicle.getStatus() + ")");
        }
        RescueMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        vehicle.setStatus(VehicleStatus.DEPLOYED);
        vehicle.setAssignedMission(mission);
        vehicle.setDeployedAt(LocalDateTime.now());
        vehicle.setReturnedAt(null);
        RescueVehicleDTO saved = RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
        recordEvent(mission, "Vehicle " + vehicle.getRegistrationNumber() + " deployed to mission " + mission.getMissionCode(), actor);
        return saved;
    }

    @Override
    @Transactional
    public RescueVehicleDTO returnVehicle(Long id, String actor) {
        RescueVehicle vehicle = findVehicle(id);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setReturnedAt(LocalDateTime.now());
        RescueMission mission = vehicle.getAssignedMission();
        vehicle.setAssignedMission(null);
        RescueVehicleDTO saved = RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
        if (mission != null) {
            recordEvent(mission, "Vehicle " + vehicle.getRegistrationNumber() + " returned from mission " + mission.getMissionCode(), actor);
        }
        return saved;
    }

    @Override
    @Transactional
    public RescueVehicleDTO startMaintenance(Long id, String actor) {
        RescueVehicle vehicle = findVehicle(id);
        if (vehicle.getStatus() == VehicleStatus.DEPLOYED) {
            throw new IllegalArgumentException("Cannot send a deployed vehicle for maintenance");
        }
        vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
        return RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public RescueVehicleDTO completeMaintenance(Long id, String actor) {
        RescueVehicle vehicle = findVehicle(id);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setLastMaintainedAt(LocalDateTime.now());
        vehicle.setNextMaintenanceDue(LocalDateTime.now().plusMonths(6));
        vehicle.setFuelLevel(Math.max(vehicle.getFuelLevel(), 50));
        return RescueVehicleDTO.fromEntity(vehicleRepository.save(vehicle));
    }

    private void recordEvent(RescueMission mission, String message, String actor) {
        com.disaster.entity.MissionEvent event = new com.disaster.entity.MissionEvent();
        event.setMission(mission);
        event.setEventType(MissionEventType.RESOURCE_ALLOCATED);
        event.setMessage(message);
        event.setPerformedBy(actor != null ? actor : "system");
        event.setOccurredAt(LocalDateTime.now());
        missionEventRepository.save(event);
    }

    private RescueVehicle findVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rescue vehicle not found with id: " + id));
    }
}
