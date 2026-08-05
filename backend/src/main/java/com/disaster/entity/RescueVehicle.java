package com.disaster.entity;

import com.disaster.enums.VehicleStatus;
import com.disaster.enums.VehicleType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_vehicles")
public class RescueVehicle {

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

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private String registrationNumber;

    private String model;

    private int capacity;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    private double latitude;

    private double longitude;

    private int fuelLevel;

    private LocalDateTime lastMaintainedAt;

    private LocalDateTime nextMaintenanceDue;

    private String notes;

    @Version
    private Long version = 0L;

    public RescueVehicle() {}

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
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(int fuelLevel) { this.fuelLevel = fuelLevel; }
    public LocalDateTime getLastMaintainedAt() { return lastMaintainedAt; }
    public void setLastMaintainedAt(LocalDateTime lastMaintainedAt) { this.lastMaintainedAt = lastMaintainedAt; }
    public LocalDateTime getNextMaintenanceDue() { return nextMaintenanceDue; }
    public void setNextMaintenanceDue(LocalDateTime nextMaintenanceDue) { this.nextMaintenanceDue = nextMaintenanceDue; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
