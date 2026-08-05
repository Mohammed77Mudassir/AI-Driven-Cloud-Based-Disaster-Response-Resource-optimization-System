package com.disaster.entity;

import com.disaster.enums.EquipmentStatus;
import com.disaster.enums.EquipmentType;
import com.disaster.enums.ResourceCondition;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_equipment")
public class RescueEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private RescueTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mission_id")
    private RescueMission assignedMission;

    private LocalDateTime deployedAt;

    private LocalDateTime returnedAt;

    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType equipmentType;

    private int totalQuantity;

    private int availableQuantity;

    private int deployedQuantity;

    private int inMaintenanceQuantity;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    @Enumerated(EnumType.STRING)
    private ResourceCondition condition;

    private LocalDateTime lastMaintainedAt;

    private LocalDateTime nextMaintenanceDue;

    private String notes;

    @Version
    private Long version = 0L;

    public RescueEquipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RescueTeam getTeam() { return team; }
    public void setTeam(RescueTeam team) { this.team = team; }
    public RescueMission getAssignedMission() { return assignedMission; }
    public void setAssignedMission(RescueMission assignedMission) { this.assignedMission = assignedMission; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
