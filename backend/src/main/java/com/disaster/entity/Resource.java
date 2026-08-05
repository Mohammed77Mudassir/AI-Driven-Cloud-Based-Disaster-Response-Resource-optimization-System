package com.disaster.entity;

import com.disaster.enums.ResourceCondition;
import com.disaster.enums.ResourceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    private int quantity;
    private int totalQuantity;
    private int deployedQuantity;
    private int inMaintenanceQuantity;
    @Enumerated(EnumType.STRING)
    private ResourceCondition condition;
    private LocalDateTime lastMaintainedAt;
    private LocalDateTime maintenanceDueAt;
    private LocalDateTime deployedAt;
    private LocalDateTime returnedAt;
    private boolean available;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_disaster_id")
    private Disaster assignedDisaster;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mission_id")
    private RescueMission assignedMission;
    private String location;
    private double latitude;
    private double longitude;

    @Version
    private Long version = 0L;

    public Resource() {}
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
    public Disaster getAssignedDisaster() { return assignedDisaster; }
    public void setAssignedDisaster(Disaster assignedDisaster) { this.assignedDisaster = assignedDisaster; }
    public RescueMission getAssignedMission() { return assignedMission; }
    public void setAssignedMission(RescueMission assignedMission) { this.assignedMission = assignedMission; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
