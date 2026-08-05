package com.disaster.dto;

import com.disaster.entity.ResourceMovement;
import com.disaster.enums.ResourceMovementType;
import java.time.LocalDateTime;

public class ResourceMovementDTO {
    private Long id;
    private Long resourceId;
    private String resourceType;
    private Long missionId;
    private String missionTitle;
    private ResourceMovementType movementType;
    private int quantity;
    private String actor;
    private String notes;
    private int availableAfter;
    private int deployedAfter;
    private int inMaintenanceAfter;
    private LocalDateTime occurredAt;

    public ResourceMovementDTO() {}

    public static ResourceMovementDTO fromEntity(ResourceMovement m) {
        ResourceMovementDTO dto = new ResourceMovementDTO();
        dto.setId(m.getId());
        if (m.getResource() != null) {
            dto.setResourceId(m.getResource().getId());
            if (m.getResource().getResourceType() != null) {
                dto.setResourceType(m.getResource().getResourceType().name());
            }
        }
        if (m.getMission() != null) {
            dto.setMissionId(m.getMission().getId());
            dto.setMissionTitle(m.getMission().getMissionCode() + " - " + m.getMission().getTitle());
        }
        dto.setMovementType(m.getMovementType());
        dto.setQuantity(m.getQuantity());
        dto.setActor(m.getActor());
        dto.setNotes(m.getNotes());
        dto.setAvailableAfter(m.getAvailableAfter());
        dto.setDeployedAfter(m.getDeployedAfter());
        dto.setInMaintenanceAfter(m.getInMaintenanceAfter());
        dto.setOccurredAt(m.getOccurredAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public String getMissionTitle() { return missionTitle; }
    public void setMissionTitle(String missionTitle) { this.missionTitle = missionTitle; }
    public ResourceMovementType getMovementType() { return movementType; }
    public void setMovementType(ResourceMovementType movementType) { this.movementType = movementType; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getAvailableAfter() { return availableAfter; }
    public void setAvailableAfter(int availableAfter) { this.availableAfter = availableAfter; }
    public int getDeployedAfter() { return deployedAfter; }
    public void setDeployedAfter(int deployedAfter) { this.deployedAfter = deployedAfter; }
    public int getInMaintenanceAfter() { return inMaintenanceAfter; }
    public void setInMaintenanceAfter(int inMaintenanceAfter) { this.inMaintenanceAfter = inMaintenanceAfter; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
