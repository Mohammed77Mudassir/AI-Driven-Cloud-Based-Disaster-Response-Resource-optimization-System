package com.disaster.entity;

import com.disaster.enums.DroneStatus;
import com.disaster.enums.MissionStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "drones")
public class Drone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String droneId;
    @Enumerated(EnumType.STRING)
    private DroneStatus status;
    private int battery;
    private boolean cameraStatus;
    private double latitude;
    private double longitude;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_disaster_id")
    private Disaster assignedDisaster;
    @Enumerated(EnumType.STRING)
    private MissionStatus missionStatus;

    public Drone() {}
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
    public Disaster getAssignedDisaster() { return assignedDisaster; }
    public void setAssignedDisaster(Disaster assignedDisaster) { this.assignedDisaster = assignedDisaster; }
    public MissionStatus getMissionStatus() { return missionStatus; }
    public void setMissionStatus(MissionStatus missionStatus) { this.missionStatus = missionStatus; }
}
