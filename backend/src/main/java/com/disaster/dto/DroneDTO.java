package com.disaster.dto;

import com.disaster.entity.Drone;
import com.disaster.enums.DroneStatus;
import com.disaster.enums.MissionStatus;

public class DroneDTO {
    private Long id;
    private String droneId;
    private DroneStatus status;
    private int battery;
    private boolean cameraStatus;
    private double latitude;
    private double longitude;
    private Long assignedDisasterId;
    private String assignedDisasterName;
    private MissionStatus missionStatus;

    public DroneDTO() {}
    
    public static DroneDTO fromEntity(Drone d) {
        DroneDTO dto = new DroneDTO();
        dto.setId(d.getId());
        dto.setDroneId(d.getDroneId());
        dto.setStatus(d.getStatus());
        dto.setBattery(d.getBattery());
        dto.setCameraStatus(d.isCameraStatus());
        dto.setLatitude(d.getLatitude());
        dto.setLongitude(d.getLongitude());
        dto.setMissionStatus(d.getMissionStatus());
        if (d.getAssignedDisaster() != null) {
            dto.setAssignedDisasterId(d.getAssignedDisaster().getId());
            dto.setAssignedDisasterName(d.getAssignedDisaster().getDisasterType());
        }
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDroneId() { return droneId; }
    public void setDroneId(String droneId) { this.droneId = droneId; }
    public DroneStatus getStatus() { return status; }
    public void setStatus(DroneStatus status) { this.status = status; }
    public int getBattery() { return battery; }
    public void setBattery(int battery) { this.battery = battery; }
    public boolean isCameraStatus() { return cameraStatus; }
    public void setCameraStatus(boolean cameraStatus) { this.cameraStatus = cameraStatus; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Long getAssignedDisasterId() { return assignedDisasterId; }
    public void setAssignedDisasterId(Long assignedDisasterId) { this.assignedDisasterId = assignedDisasterId; }
    public String getAssignedDisasterName() { return assignedDisasterName; }
    public void setAssignedDisasterName(String assignedDisasterName) { this.assignedDisasterName = assignedDisasterName; }
    public MissionStatus getMissionStatus() { return missionStatus; }
    public void setMissionStatus(MissionStatus missionStatus) { this.missionStatus = missionStatus; }
}
