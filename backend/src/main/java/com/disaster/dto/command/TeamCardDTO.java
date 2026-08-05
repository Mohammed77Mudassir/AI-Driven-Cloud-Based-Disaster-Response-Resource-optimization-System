package com.disaster.dto.command;

import java.time.LocalDateTime;

/**
 * Compact team record rendered as a color-coded card in the Team Availability
 * section of the command center.
 */
public class TeamCardDTO {

    private Long teamId;
    private String teamName;
    private String teamLeader;
    private int teamSize;
    private int maxCapacity;
    private String specialization;
    private String status;
    private int availableMembers;
    private String assignedMissionCode;
    private String assignedMissionTitle;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastLocationUpdateAt;

    public TeamCardDTO() {}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamLeader() { return teamLeader; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAvailableMembers() { return availableMembers; }
    public void setAvailableMembers(int availableMembers) { this.availableMembers = availableMembers; }
    public String getAssignedMissionCode() { return assignedMissionCode; }
    public void setAssignedMissionCode(String assignedMissionCode) { this.assignedMissionCode = assignedMissionCode; }
    public String getAssignedMissionTitle() { return assignedMissionTitle; }
    public void setAssignedMissionTitle(String assignedMissionTitle) { this.assignedMissionTitle = assignedMissionTitle; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getLastLocationUpdateAt() { return lastLocationUpdateAt; }
    public void setLastLocationUpdateAt(LocalDateTime lastLocationUpdateAt) { this.lastLocationUpdateAt = lastLocationUpdateAt; }
}
