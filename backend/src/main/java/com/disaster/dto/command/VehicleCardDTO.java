package com.disaster.dto.command;

import java.time.LocalDateTime;

/**
 * Fleet record with derived operational warnings used by the Vehicle Status
 * panel.
 */
public class VehicleCardDTO {

    private Long vehicleId;
    private String registrationNumber;
    private String vehicleType;
    private String model;
    private int capacity;
    private int fuelLevel;
    private String status;
    private String teamName;
    private String assignedMissionTitle;
    private LocalDateTime lastMaintainedAt;
    private LocalDateTime nextMaintenanceDue;
    private boolean gpsActive;
    private boolean lowFuel;
    private boolean maintenanceRequired;
    private boolean unavailable;

    public VehicleCardDTO() {}

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(int fuelLevel) { this.fuelLevel = fuelLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getAssignedMissionTitle() { return assignedMissionTitle; }
    public void setAssignedMissionTitle(String assignedMissionTitle) { this.assignedMissionTitle = assignedMissionTitle; }
    public LocalDateTime getLastMaintainedAt() { return lastMaintainedAt; }
    public void setLastMaintainedAt(LocalDateTime lastMaintainedAt) { this.lastMaintainedAt = lastMaintainedAt; }
    public LocalDateTime getNextMaintenanceDue() { return nextMaintenanceDue; }
    public void setNextMaintenanceDue(LocalDateTime nextMaintenanceDue) { this.nextMaintenanceDue = nextMaintenanceDue; }
    public boolean isGpsActive() { return gpsActive; }
    public void setGpsActive(boolean gpsActive) { this.gpsActive = gpsActive; }
    public boolean isLowFuel() { return lowFuel; }
    public void setLowFuel(boolean lowFuel) { this.lowFuel = lowFuel; }
    public boolean isMaintenanceRequired() { return maintenanceRequired; }
    public void setMaintenanceRequired(boolean maintenanceRequired) { this.maintenanceRequired = maintenanceRequired; }
    public boolean isUnavailable() { return unavailable; }
    public void setUnavailable(boolean unavailable) { this.unavailable = unavailable; }
}
