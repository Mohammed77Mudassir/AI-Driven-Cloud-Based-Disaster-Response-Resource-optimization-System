package com.disaster.dto;

import com.disaster.entity.RescueVehicle;
import com.disaster.enums.VehicleStatus;
import com.disaster.enums.VehicleType;
import java.time.LocalDateTime;

public class RescueVehicleDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private VehicleType vehicleType;
    private String registrationNumber;
    private String model;
    private int capacity;
    private VehicleStatus status;
    private double latitude;
    private double longitude;
    private int fuelLevel;
    private LocalDateTime lastMaintainedAt;
    private LocalDateTime nextMaintenanceDue;
    private String notes;
    private Long assignedMissionId;
    private String assignedMissionTitle;
    private LocalDateTime deployedAt;
    private LocalDateTime returnedAt;

    public RescueVehicleDTO() {}

    public static RescueVehicleDTO fromEntity(RescueVehicle v) {
        RescueVehicleDTO dto = new RescueVehicleDTO();
        dto.setId(v.getId());
        if (v.getTeam() != null) {
            dto.setTeamId(v.getTeam().getId());
            dto.setTeamName(v.getTeam().getTeamName());
        }
        dto.setVehicleType(v.getVehicleType());
        dto.setRegistrationNumber(v.getRegistrationNumber());
        dto.setModel(v.getModel());
        dto.setCapacity(v.getCapacity());
        dto.setStatus(v.getStatus());
        dto.setLatitude(v.getLatitude());
        dto.setLongitude(v.getLongitude());
        dto.setFuelLevel(v.getFuelLevel());
        dto.setLastMaintainedAt(v.getLastMaintainedAt());
        dto.setNextMaintenanceDue(v.getNextMaintenanceDue());
        dto.setNotes(v.getNotes());
        dto.setDeployedAt(v.getDeployedAt());
        dto.setReturnedAt(v.getReturnedAt());
        if (v.getAssignedMission() != null) {
            dto.setAssignedMissionId(v.getAssignedMission().getId());
            dto.setAssignedMissionTitle(v.getAssignedMission().getMissionCode()
                    + " - " + v.getAssignedMission().getTitle());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
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
    public Long getAssignedMissionId() { return assignedMissionId; }
    public void setAssignedMissionId(Long assignedMissionId) { this.assignedMissionId = assignedMissionId; }
    public String getAssignedMissionTitle() { return assignedMissionTitle; }
    public void setAssignedMissionTitle(String assignedMissionTitle) { this.assignedMissionTitle = assignedMissionTitle; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}
