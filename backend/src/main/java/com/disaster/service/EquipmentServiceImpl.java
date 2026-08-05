package com.disaster.service;

import com.disaster.dto.RescueEquipmentDTO;
import com.disaster.entity.MissionEvent;
import com.disaster.entity.RescueEquipment;
import com.disaster.entity.RescueMission;
import com.disaster.entity.RescueTeam;
import com.disaster.enums.EquipmentStatus;
import com.disaster.enums.MissionEventType;
import com.disaster.enums.ResourceCondition;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.MissionEventRepository;
import com.disaster.repository.RescueEquipmentRepository;
import com.disaster.repository.RescueMissionRepository;
import com.disaster.repository.RescueTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final RescueEquipmentRepository equipmentRepository;
    private final RescueTeamRepository teamRepository;
    private final RescueMissionRepository missionRepository;
    private final MissionEventRepository missionEventRepository;

    public EquipmentServiceImpl(RescueEquipmentRepository equipmentRepository,
                                RescueTeamRepository teamRepository,
                                RescueMissionRepository missionRepository,
                                MissionEventRepository missionEventRepository) {
        this.equipmentRepository = equipmentRepository;
        this.teamRepository = teamRepository;
        this.missionRepository = missionRepository;
        this.missionEventRepository = missionEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueEquipmentDTO> getAll() {
        return equipmentRepository.findAll().stream().map(RescueEquipmentDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RescueEquipmentDTO getById(Long id) {
        return RescueEquipmentDTO.fromEntity(findEquipment(id));
    }

    @Override
    @Transactional
    public RescueEquipmentDTO create(RescueEquipmentDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Equipment name is required");
        }
        if (dto.getTotalQuantity() <= 0) {
            throw new IllegalArgumentException("Total quantity must be greater than zero");
        }
        RescueEquipment equipment = new RescueEquipment();
        apply(equipment, dto);
        equipment.setAvailableQuantity(dto.getAvailableQuantity() > 0 ? dto.getAvailableQuantity() : dto.getTotalQuantity());
        equipment.setDeployedQuantity(dto.getDeployedQuantity());
        equipment.setInMaintenanceQuantity(dto.getInMaintenanceQuantity());
        equipment.setStatus(dto.getStatus() != null ? dto.getStatus() : EquipmentStatus.AVAILABLE);
        equipment.setCondition(dto.getCondition() != null ? dto.getCondition() : ResourceCondition.GOOD);
        validateBalance(equipment);
        return RescueEquipmentDTO.fromEntity(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public RescueEquipmentDTO update(Long id, RescueEquipmentDTO dto) {
        RescueEquipment equipment = findEquipment(id);
        if (dto.getName() != null) equipment.setName(dto.getName());
        if (dto.getEquipmentType() != null) equipment.setEquipmentType(dto.getEquipmentType());
        if (dto.getTeamId() != null) {
            RescueTeam team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + dto.getTeamId()));
            equipment.setTeam(team);
        }
        if (dto.getCondition() != null) equipment.setCondition(dto.getCondition());
        if (dto.getNotes() != null) equipment.setNotes(dto.getNotes());
        return RescueEquipmentDTO.fromEntity(equipmentRepository.save(equipment));
    }

    private void apply(RescueEquipment equipment, RescueEquipmentDTO dto) {
        equipment.setName(dto.getName());
        equipment.setEquipmentType(dto.getEquipmentType());
        if (dto.getTeamId() != null) {
            RescueTeam team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + dto.getTeamId()));
            equipment.setTeam(team);
        }
        equipment.setTotalQuantity(dto.getTotalQuantity());
        equipment.setNotes(dto.getNotes());
    }

    private void validateBalance(RescueEquipment e) {
        if (e.getAvailableQuantity() + e.getDeployedQuantity() + e.getInMaintenanceQuantity() > e.getTotalQuantity()) {
            throw new IllegalArgumentException("Available + deployed + maintenance quantity exceeds total quantity");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        equipmentRepository.delete(findEquipment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueEquipmentDTO> getByTeam(Long teamId) {
        return equipmentRepository.findByTeamId(teamId).stream().map(RescueEquipmentDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueEquipmentDTO> getByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status).stream().map(RescueEquipmentDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueEquipmentDTO> getByMission(Long missionId) {
        return equipmentRepository.findByAssignedMissionId(missionId).stream().map(RescueEquipmentDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public RescueEquipmentDTO deploy(Long id, int quantity, Long missionId, String actor) {
        RescueEquipment e = findEquipment(id);
        if (quantity <= 0 || quantity > e.getAvailableQuantity()) {
            throw new IllegalArgumentException("Deployment quantity must be between 1 and " + e.getAvailableQuantity());
        }
        RescueMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        e.setAvailableQuantity(e.getAvailableQuantity() - quantity);
        e.setDeployedQuantity(e.getDeployedQuantity() + quantity);
        e.setAssignedMission(mission);
        e.setDeployedAt(LocalDateTime.now());
        e.setReturnedAt(null);
        recomputeStatus(e);
        RescueEquipmentDTO saved = RescueEquipmentDTO.fromEntity(equipmentRepository.save(e));
        recordEvent(mission, "Deployed " + quantity + " x " + e.getName() + " to mission " + mission.getMissionCode(), actor);
        return saved;
    }

    @Override
    @Transactional
    public RescueEquipmentDTO returnEquipment(Long id, int quantity, String actor) {
        RescueEquipment e = findEquipment(id);
        if (quantity <= 0 || quantity > e.getDeployedQuantity()) {
            throw new IllegalArgumentException("Return quantity must be between 1 and " + e.getDeployedQuantity());
        }
        e.setDeployedQuantity(e.getDeployedQuantity() - quantity);
        e.setAvailableQuantity(e.getAvailableQuantity() + quantity);
        e.setReturnedAt(LocalDateTime.now());
        if (e.getDeployedQuantity() == 0) {
            e.setAssignedMission(null);
        }
        recomputeStatus(e);
        RescueEquipmentDTO saved = RescueEquipmentDTO.fromEntity(equipmentRepository.save(e));
        if (e.getAssignedMission() != null) {
            recordEvent(e.getAssignedMission(), "Returned " + quantity + " x " + e.getName(), actor);
        }
        return saved;
    }

    @Override
    @Transactional
    public RescueEquipmentDTO startMaintenance(Long id, int quantity, String actor) {
        RescueEquipment e = findEquipment(id);
        if (quantity <= 0 || quantity > e.getAvailableQuantity()) {
            throw new IllegalArgumentException("Maintenance quantity must be between 1 and " + e.getAvailableQuantity());
        }
        e.setAvailableQuantity(e.getAvailableQuantity() - quantity);
        e.setInMaintenanceQuantity(e.getInMaintenanceQuantity() + quantity);
        recomputeStatus(e);
        return RescueEquipmentDTO.fromEntity(equipmentRepository.save(e));
    }

    @Override
    @Transactional
    public RescueEquipmentDTO completeMaintenance(Long id, int quantity, String actor) {
        RescueEquipment e = findEquipment(id);
        if (quantity <= 0 || quantity > e.getInMaintenanceQuantity()) {
            throw new IllegalArgumentException("Completion quantity must be between 1 and " + e.getInMaintenanceQuantity());
        }
        e.setInMaintenanceQuantity(e.getInMaintenanceQuantity() - quantity);
        e.setAvailableQuantity(e.getAvailableQuantity() + quantity);
        e.setLastMaintainedAt(LocalDateTime.now());
        e.setNextMaintenanceDue(LocalDateTime.now().plusMonths(6));
        recomputeStatus(e);
        return RescueEquipmentDTO.fromEntity(equipmentRepository.save(e));
    }

    private void recordEvent(RescueMission mission, String message, String actor) {
        MissionEvent event = new MissionEvent();
        event.setMission(mission);
        event.setEventType(MissionEventType.RESOURCE_ALLOCATED);
        event.setMessage(message);
        event.setPerformedBy(actor != null ? actor : "system");
        event.setOccurredAt(LocalDateTime.now());
        missionEventRepository.save(event);
    }

    private void recomputeStatus(RescueEquipment e) {
        if (e.getAvailableQuantity() == 0 && e.getDeployedQuantity() > 0) {
            e.setStatus(EquipmentStatus.DEPLOYED);
        } else if (e.getAvailableQuantity() == 0 && e.getInMaintenanceQuantity() > 0) {
            e.setStatus(EquipmentStatus.IN_MAINTENANCE);
        } else if (e.getAvailableQuantity() == 0) {
            e.setStatus(EquipmentStatus.DEPLETED);
        } else {
            e.setStatus(EquipmentStatus.AVAILABLE);
        }
    }

    private RescueEquipment findEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rescue equipment not found with id: " + id));
    }
}
