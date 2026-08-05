package com.disaster.service;

import com.disaster.dto.ResourceDTO;
import com.disaster.dto.ResourceMovementDTO;
import com.disaster.entity.Resource;
import com.disaster.entity.ResourceMovement;
import com.disaster.entity.Disaster;
import com.disaster.entity.MissionEvent;
import com.disaster.entity.RescueMission;
import com.disaster.enums.MissionEventType;
import com.disaster.enums.ResourceCondition;
import com.disaster.enums.ResourceMovementType;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.ResourceRepository;
import com.disaster.repository.ResourceMovementRepository;
import com.disaster.repository.DisasterRepository;
import com.disaster.repository.MissionEventRepository;
import com.disaster.repository.RescueMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final ResourceMovementRepository movementRepository;
    private final DisasterRepository disasterRepository;
    private final RescueMissionRepository missionRepository;
    private final MissionEventRepository missionEventRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           ResourceMovementRepository movementRepository,
                           DisasterRepository disasterRepository,
                           RescueMissionRepository missionRepository,
                           MissionEventRepository missionEventRepository) {
        this.resourceRepository = resourceRepository;
        this.movementRepository = movementRepository;
        this.disasterRepository = disasterRepository;
        this.missionRepository = missionRepository;
        this.missionEventRepository = missionEventRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceDTO> getAll() {
        return resourceRepository.findAll().stream().map(ResourceDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ResourceDTO getById(Long id) {
        return ResourceDTO.fromEntity(findResource(id));
    }

    public ResourceDTO create(ResourceDTO dto) {
        Resource r = new Resource();
        r.setResourceType(dto.getResourceType());
        r.setQuantity(dto.getQuantity());
        r.setTotalQuantity(dto.getTotalQuantity() > 0 ? dto.getTotalQuantity() : dto.getQuantity());
        r.setDeployedQuantity(dto.getDeployedQuantity());
        r.setInMaintenanceQuantity(dto.getInMaintenanceQuantity());
        r.setCondition(dto.getCondition() != null ? dto.getCondition() : ResourceCondition.GOOD);
        r.setAvailable(dto.getQuantity() > 0);
        r.setLocation(dto.getLocation());
        r.setLatitude(dto.getLatitude());
        r.setLongitude(dto.getLongitude());
        LocalDateTime now = LocalDateTime.now();
        if (dto.getQuantity() > 0 && dto.getMaintenanceDueAt() == null) {
            r.setMaintenanceDueAt(now.plusMonths(6));
            r.setLastMaintainedAt(now);
        } else {
            r.setMaintenanceDueAt(dto.getMaintenanceDueAt());
            r.setLastMaintainedAt(dto.getLastMaintainedAt());
        }
        return ResourceDTO.fromEntity(resourceRepository.save(r));
    }

    public ResourceDTO update(Long id, ResourceDTO dto) {
        Resource r = findResource(id);
        if (dto.getResourceType() != null) r.setResourceType(dto.getResourceType());
        if (dto.getQuantity() > 0) r.setQuantity(dto.getQuantity());
        if (dto.getTotalQuantity() > 0) r.setTotalQuantity(dto.getTotalQuantity());
        if (dto.getCondition() != null) r.setCondition(dto.getCondition());
        r.setAvailable(dto.getQuantity() > 0);
        if (dto.getLocation() != null) r.setLocation(dto.getLocation());
        r.setLatitude(dto.getLatitude());
        r.setLongitude(dto.getLongitude());
        if (dto.getLastMaintainedAt() != null) r.setLastMaintainedAt(dto.getLastMaintainedAt());
        if (dto.getMaintenanceDueAt() != null) r.setMaintenanceDueAt(dto.getMaintenanceDueAt());
        if (dto.getAssignedDisasterId() != null) {
            Disaster d = disasterRepository.findById(dto.getAssignedDisasterId()).orElse(null);
            r.setAssignedDisaster(d);
        } else {
            r.setAssignedDisaster(null);
        }
        return ResourceDTO.fromEntity(resourceRepository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        findResource(id);
        movementRepository.deleteByResourceId(id);
        resourceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ResourceDTO> getAvailable() {
        return resourceRepository.findByAvailableTrue().stream().map(ResourceDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceDTO> getByDisaster(Long disasterId) {
        return resourceRepository.findByAssignedDisasterId(disasterId).stream().map(ResourceDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceDTO> getByMission(Long missionId) {
        return resourceRepository.findByAssignedMissionId(missionId).stream().map(ResourceDTO::fromEntity).toList();
    }

    @Transactional
    public ResourceDTO deploy(Long id, int quantity, Long missionId, String actor) {
        Resource r = findResource(id);
        if (quantity <= 0 || quantity > r.getQuantity()) {
            throw new IllegalArgumentException("Deployment quantity must be between 1 and available quantity " + r.getQuantity());
        }
        RescueMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));

        r.setQuantity(r.getQuantity() - quantity);
        r.setDeployedQuantity(r.getDeployedQuantity() + quantity);
        r.setAvailable(r.getQuantity() > 0);
        r.setDeployedAt(LocalDateTime.now());
        r.setReturnedAt(null);
        r.setAssignedMission(mission);
        if (mission.getDisaster() != null) {
            r.setAssignedDisaster(mission.getDisaster());
        }
        ResourceDTO saved = ResourceDTO.fromEntity(resourceRepository.save(r));

        recordMovement(r, mission, ResourceMovementType.DEPLOYED, quantity,
                "Deployed " + quantity + " x " + r.getResourceType() + " to mission " + mission.getMissionCode(), actor);
        recordMissionEvent(mission, "Deployed " + quantity + " x " + r.getResourceType() + " to mission " + mission.getMissionCode(), actor);
        return saved;
    }

    @Transactional
    public ResourceDTO returnResource(Long id, int quantity, String actor) {
        Resource r = findResource(id);
        if (quantity <= 0 || quantity > r.getDeployedQuantity()) {
            throw new IllegalArgumentException("Return quantity must be between 1 and deployed quantity " + r.getDeployedQuantity());
        }
        RescueMission mission = r.getAssignedMission();
        r.setQuantity(r.getQuantity() + quantity);
        r.setDeployedQuantity(r.getDeployedQuantity() - quantity);
        r.setAvailable(true);
        r.setReturnedAt(LocalDateTime.now());
        recordMovement(r, mission, ResourceMovementType.RETURNED, quantity,
                "Returned " + quantity + " x " + r.getResourceType(), actor);
        if (mission != null) {
            recordMissionEvent(mission, "Returned " + quantity + " x " + r.getResourceType(), actor);
        }
        if (r.getDeployedQuantity() == 0) {
            r.setAssignedMission(null);
            r.setAssignedDisaster(null);
        }
        return ResourceDTO.fromEntity(resourceRepository.save(r));
    }

    @Transactional
    public ResourceDTO startMaintenance(Long id, int quantity, String actor) {
        Resource r = findResource(id);
        if (quantity <= 0 || quantity > r.getQuantity()) {
            throw new IllegalArgumentException("Maintenance quantity must be between 1 and available quantity " + r.getQuantity());
        }
        r.setQuantity(r.getQuantity() - quantity);
        r.setInMaintenanceQuantity(r.getInMaintenanceQuantity() + quantity);
        r.setAvailable(r.getQuantity() > 0);
        ResourceDTO saved = ResourceDTO.fromEntity(resourceRepository.save(r));
        recordMovement(r, null, ResourceMovementType.MAINTENANCE_STARTED, quantity,
                "Sent " + quantity + " x " + r.getResourceType() + " for maintenance", actor);
        return saved;
    }

    @Transactional
    public ResourceDTO completeMaintenance(Long id, int quantity, String actor) {
        Resource r = findResource(id);
        if (quantity <= 0 || quantity > r.getInMaintenanceQuantity()) {
            throw new IllegalArgumentException("Completion quantity must be between 1 and maintenance quantity " + r.getInMaintenanceQuantity());
        }
        r.setQuantity(r.getQuantity() + quantity);
        r.setInMaintenanceQuantity(r.getInMaintenanceQuantity() - quantity);
        r.setAvailable(true);
        r.setLastMaintainedAt(LocalDateTime.now());
        r.setMaintenanceDueAt(LocalDateTime.now().plusMonths(6));
        r.setCondition(ResourceCondition.GOOD);
        ResourceDTO saved = ResourceDTO.fromEntity(resourceRepository.save(r));
        recordMovement(r, null, ResourceMovementType.MAINTENANCE_COMPLETED, quantity,
                "Completed maintenance on " + quantity + " x " + r.getResourceType(), actor);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ResourceMovementDTO> getMovements() {
        return movementRepository.findAllByOrderByOccurredAtDesc()
                .stream().map(ResourceMovementDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceMovementDTO> getMovementsByResource(Long resourceId) {
        findResource(resourceId);
        return movementRepository.findByResourceIdOrderByOccurredAtDesc(resourceId)
                .stream().map(ResourceMovementDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceMovementDTO> getMovementsByMission(Long missionId) {
        return movementRepository.findByMissionIdOrderByOccurredAtDesc(missionId)
                .stream().map(ResourceMovementDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceMovementDTO> getMovementsByType(ResourceMovementType type) {
        return movementRepository.findByMovementTypeOrderByOccurredAtDesc(type)
                .stream().map(ResourceMovementDTO::fromEntity).toList();
    }

    private void recordMovement(Resource r, RescueMission mission, ResourceMovementType type,
                                int quantity, String notes, String actor) {
        ResourceMovement m = new ResourceMovement();
        m.setResource(r);
        m.setMission(mission);
        m.setMovementType(type);
        m.setQuantity(quantity);
        m.setActor(actor != null ? actor : "system");
        m.setNotes(notes);
        m.setAvailableAfter(r.getQuantity());
        m.setDeployedAfter(r.getDeployedQuantity());
        m.setInMaintenanceAfter(r.getInMaintenanceQuantity());
        m.setOccurredAt(LocalDateTime.now());
        movementRepository.save(m);
    }

    private void recordMissionEvent(RescueMission mission, String message, String actor) {
        MissionEvent event = new MissionEvent();
        event.setMission(mission);
        event.setEventType(MissionEventType.RESOURCE_ALLOCATED);
        event.setMessage(message);
        event.setPerformedBy(actor != null ? actor : "system");
        event.setOccurredAt(LocalDateTime.now());
        missionEventRepository.save(event);
    }

    private Resource findResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }
}
