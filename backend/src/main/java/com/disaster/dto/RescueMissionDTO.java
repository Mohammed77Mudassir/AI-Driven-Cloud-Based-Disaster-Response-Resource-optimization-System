package com.disaster.dto;

import com.disaster.entity.RescueMission;
import com.disaster.enums.MissionStatus;
import java.time.LocalDateTime;

public class RescueMissionDTO {
    private Long id;
    private String missionCode;
    private String title;
    private String missionType;
    private String description;
    private Long teamId;
    private String teamName;
    private Long disasterId;
    private String disasterName;
    private MissionStatus status;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String assignedBy;
    private String instructions;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelledReason;

    public RescueMissionDTO() {}

    public static RescueMissionDTO fromEntity(RescueMission m) {
        RescueMissionDTO dto = new RescueMissionDTO();
        dto.setId(m.getId());
        dto.setMissionCode(m.getMissionCode());
        dto.setTitle(m.getTitle());
        dto.setMissionType(m.getMissionType());
        dto.setDescription(m.getDescription());
        if (m.getTeam() != null) {
            dto.setTeamId(m.getTeam().getId());
            dto.setTeamName(m.getTeam().getTeamName());
        }
        if (m.getDisaster() != null) {
            dto.setDisasterId(m.getDisaster().getId());
            dto.setDisasterName(m.getDisaster().getDisasterType() + " at " + m.getDisaster().getLocation());
        }
        dto.setStatus(m.getStatus());
        dto.setPriority(m.getPriority());
        dto.setStartTime(m.getStartTime());
        dto.setEndTime(m.getEndTime());
        dto.setAssignedBy(m.getAssignedBy());
        dto.setInstructions(m.getInstructions());
        dto.setCreatedBy(m.getCreatedBy());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setCompletedAt(m.getCompletedAt());
        dto.setCancelledAt(m.getCancelledAt());
        dto.setCancelledReason(m.getCancelledReason());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = missionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDisasterName() { return disasterName; }
    public void setDisasterName(String disasterName) { this.disasterName = disasterName; }
    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelledReason() { return cancelledReason; }
    public void setCancelledReason(String cancelledReason) { this.cancelledReason = cancelledReason; }
}
