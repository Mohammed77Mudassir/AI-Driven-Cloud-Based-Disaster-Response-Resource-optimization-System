package com.disaster.dto;

import com.disaster.entity.Resource;
import com.disaster.enums.ResourceCondition;
import com.disaster.enums.ResourceType;
import java.time.LocalDateTime;

public class ResourceDTO {
    private Long id;
    private ResourceType resourceType;
    private int quantity;
    private int totalQuantity;
    private int deployedQuantity;
    private int inMaintenanceQuantity;
    private ResourceCondition condition;
    private LocalDateTime lastMaintainedAt;
    private LocalDateTime maintenanceDueAt;
    private LocalDateTime deployedAt;
    private LocalDateTime returnedAt;
    private boolean available;
    private Long assignedDisasterId;
    private String assignedDisasterName;
    private Long assignedMissionId;
    private String assignedMissionTitle;
    private String location;
    private double latitude;
    private double longitude;

    public ResourceDTO() {}

    public static ResourceDTO fromEntity(Resource r) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(r.getId());
        dto.setResourceType(r.getResourceType());
        dto.setQuantity(r.getQuantity());
        dto.setTotalQuantity(r.getTotalQuantity());
        dto.setDeployedQuantity(r.getDeployedQuantity());
        dto.setInMaintenanceQuantity(r.getInMaintenanceQuantity());
        dto.setCondition(r.getCondition());
        dto.setLastMaintainedAt(r.getLastMaintainedAt());
        dto.setMaintenanceDueAt(r.getMaintenanceDueAt());
        dto.setDeployedAt(r.getDeployedAt());
        dto.setReturnedAt(r.getReturnedAt());
        dto.setAvailable(r.isAvailable());
        dto.setLocation(r.getLocation());
        dto.setLatitude(r.getLatitude());
        dto.setLongitude(r.getLongitude());
        if (r.getAssignedDisaster() != null) {
            dto.setAssignedDisasterId(r.getAssignedDisaster().getId());
            dto.setAssignedDisasterName(r.getAssignedDisaster().getDisasterType()
                    + " at " + r.getAssignedDisaster().getLocation());
        }
        if (r.getAssignedMission() != null) {
            dto.setAssignedMissionId(r.getAssignedMission().getId());
            dto.setAssignedMissionTitle(r.getAssignedMission().getMissionCode()
                    + " - " + r.getAssignedMission().getTitle());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getDeployedQuantity() { return deployedQuantity; }
    public void setDeployedQuantity(int deployedQuantity) { this.deployedQuantity = deployedQuantity; }
    public int getInMaintenanceQuantity() { return inMaintenanceQuantity; }
    public void setInMaintenanceQuantity(int inMaintenanceQuantity) { this.inMaintenanceQuantity = inMaintenanceQuantity; }
    public ResourceCondition getCondition() { return condition; }
    public void setCondition(ResourceCondition condition) { this.condition = condition; }
    public LocalDateTime getLastMaintainedAt() { return lastMaintainedAt; }
    public void setLastMaintainedAt(LocalDateTime lastMaintainedAt) { this.lastMaintainedAt = lastMaintainedAt; }
    public LocalDateTime getMaintenanceDueAt() { return maintenanceDueAt; }
    public void setMaintenanceDueAt(LocalDateTime maintenanceDueAt) { this.maintenanceDueAt = maintenanceDueAt; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Long getAssignedDisasterId() { return assignedDisasterId; }
    public void setAssignedDisasterId(Long assignedDisasterId) { this.assignedDisasterId = assignedDisasterId; }
    public String getAssignedDisasterName() { return assignedDisasterName; }
    public void setAssignedDisasterName(String assignedDisasterName) { this.assignedDisasterName = assignedDisasterName; }
    public Long getAssignedMissionId() { return assignedMissionId; }
    public void setAssignedMissionId(Long assignedMissionId) { this.assignedMissionId = assignedMissionId; }
    public String getAssignedMissionTitle() { return assignedMissionTitle; }
    public void setAssignedMissionTitle(String assignedMissionTitle) { this.assignedMissionTitle = assignedMissionTitle; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
