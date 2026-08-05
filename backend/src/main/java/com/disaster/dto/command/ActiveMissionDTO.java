package com.disaster.dto.command;

import java.time.LocalDateTime;

/**
 * Enriched view of an ongoing (not yet terminal) rescue mission used by the
 * Active Mission panel of the command center.
 */
public class ActiveMissionDTO {

    private Long missionId;
    private String missionCode;
    private String title;
    private String missionType;
    private String priority;
    private String status;
    private Long teamId;
    private String teamName;
    private String teamLeader;
    private String assignedVehicle;
    private Double latitude;
    private Double longitude;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime estimatedCompletionTime;
    private int progress;
    private long delayMinutes;
    private String disasterName;

    public ActiveMissionDTO() {}

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = missionType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamLeader() { return teamLeader; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public String getAssignedVehicle() { return assignedVehicle; }
    public void setAssignedVehicle(String assignedVehicle) { this.assignedVehicle = assignedVehicle; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public long getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(long delayMinutes) { this.delayMinutes = delayMinutes; }
    public String getDisasterName() { return disasterName; }
    public void setDisasterName(String disasterName) { this.disasterName = disasterName; }
}
