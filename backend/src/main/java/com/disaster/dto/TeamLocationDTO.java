package com.disaster.dto;

import com.disaster.entity.TeamLocationUpdate;
import java.time.LocalDateTime;

public class TeamLocationDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private double latitude;
    private double longitude;
    private double heading;
    private double speed;
    private double accuracy;
    private String deviceId;
    private LocalDateTime timestamp;

    public TeamLocationDTO() {}

    public static TeamLocationDTO fromEntity(TeamLocationUpdate u) {
        TeamLocationDTO dto = new TeamLocationDTO();
        dto.setId(u.getId());
        if (u.getTeam() != null) {
            dto.setTeamId(u.getTeam().getId());
            dto.setTeamName(u.getTeam().getTeamName());
        }
        dto.setLatitude(u.getLatitude());
        dto.setLongitude(u.getLongitude());
        dto.setHeading(u.getHeading());
        dto.setSpeed(u.getSpeed());
        dto.setAccuracy(u.getAccuracy());
        dto.setDeviceId(u.getDeviceId());
        dto.setTimestamp(u.getTimestamp());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
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
