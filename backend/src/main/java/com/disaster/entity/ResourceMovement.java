package com.disaster.entity;

import com.disaster.enums.ResourceMovementType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_movements")
public class ResourceMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private RescueMission mission;

    @Enumerated(EnumType.STRING)
    private ResourceMovementType movementType;

    private int quantity;

    private String actor;

    private String notes;

    private int availableAfter;

    private int deployedAfter;

    private int inMaintenanceAfter;

    private LocalDateTime occurredAt;

    public ResourceMovement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public RescueMission getMission() { return mission; }
    public void setMission(RescueMission mission) { this.mission = mission; }
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
