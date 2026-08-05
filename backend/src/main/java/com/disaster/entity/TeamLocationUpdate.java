package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_location_updates")
public class TeamLocationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private RescueTeam team;

    private double latitude;

    private double longitude;

    private double heading;

    private double speed;

    private double accuracy;

    private String deviceId;

    private LocalDateTime timestamp;

    public TeamLocationUpdate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RescueTeam getTeam() { return team; }
    public void setTeam(RescueTeam team) { this.team = team; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getHeading() { return heading; }
    public void setHeading(double heading) { this.heading = heading; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
