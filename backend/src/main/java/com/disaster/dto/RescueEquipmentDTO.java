package com.disaster.dto;

import com.disaster.entity.RescueEquipment;
import com.disaster.enums.EquipmentStatus;
import com.disaster.enums.EquipmentType;
import com.disaster.enums.ResourceCondition;
import java.time.LocalDateTime;

public class RescueEquipmentDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private String name;
    private EquipmentType equipmentType;
    private int totalQuantity;
    private int availableQuantity;
    private int deployedQuantity;
    private int inMaintenanceQuantity;
    private EquipmentStatus status;
    private ResourceCondition condition;
    private LocalDateTime lastMaintainedAt;
    private LocalDateTime nextMaintenanceDue;
    private String notes;
    private Long assignedMissionId;
    private String assignedMissionTitle;
    private LocalDateTime deployedAt;
    private LocalDateTime returnedAt;

    public RescueEquipmentDTO() {}

    public static RescueEquipmentDTO fromEntity(RescueEquipment e) {
        RescueEquipmentDTO dto = new RescueEquipmentDTO();
        dto.setId(e.getId());
        if (e.getTeam() != null) {
            dto.setTeamId(e.getTeam().getId());
            dto.setTeamName(e.getTeam().getTeamName());
        }
        dto.setName(e.getName());
        dto.setEquipmentType(e.getEquipmentType());
        dto.setTotalQuantity(e.getTotalQuantity());
        dto.setAvailableQuantity(e.getAvailableQuantity());
        dto.setDeployedQuantity(e.getDeployedQuantity());
        dto.setInMaintenanceQuantity(e.getInMaintenanceQuantity());
        dto.setStatus(e.getStatus());
        dto.setCondition(e.getCondition());
        dto.setLastMaintainedAt(e.getLastMaintainedAt());
        dto.setNextMaintenanceDue(e.getNextMaintenanceDue());
        dto.setNotes(e.getNotes());
        dto.setDeployedAt(e.getDeployedAt());
        dto.setReturnedAt(e.getReturnedAt());
        if (e.getAssignedMission() != null) {
            dto.setAssignedMissionId(e.getAssignedMission().getId());
            dto.setAssignedMissionTitle(e.getAssignedMission().getMissionCode()
                    + " - " + e.getAssignedMission().getTitle());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public int getDeployedQuantity() { return deployedQuantity; }
    public void setDeployedQuantity(int deployedQuantity) { this.deployedQuantity = deployedQuantity; }
    public int getInMaintenanceQuantity() { return inMaintenanceQuantity; }
    public void setInMaintenanceQuantity(int inMaintenanceQuantity) { this.inMaintenanceQuantity = inMaintenanceQuantity; }
    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
    public ResourceCondition getCondition() { return condition; }
    public void setCondition(ResourceCondition condition) { this.condition = condition; }
    public LocalDateTime getLastMaintainedAt() { return lastMaintainedAt; }
    public void setLastMaintainedAt(LocalDateTime lastMaintainedAt) { this.lastMaintainedAt = lastMaintainedAt; }
    public LocalDateTime getNextMaintenanceDue() { return nextMaintenanceDue; }
    public void setNextMaintenanceDue(LocalDateTime nextMaintenanceDue) { this.nextMaintenanceDue = nextMaintenanceDue; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getAssignedMissionId() { return assignedMissionId; }
    public void setAssignedMissionId(Long assignedMissionId) { this.assignedMissionId = assignedMissionId; }
    public String getAssignedMissionTitle() { return assignedMissionTitle; }
    public void setAssignedMissionTitle(String assignedMissionTitle) { this.assignedMissionTitle = assignedMissionTitle; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}
