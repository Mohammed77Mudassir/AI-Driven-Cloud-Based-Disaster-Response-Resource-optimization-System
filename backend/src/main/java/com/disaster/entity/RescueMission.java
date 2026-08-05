package com.disaster.entity;

import com.disaster.enums.MissionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_missions")
public class RescueMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String missionCode;

    private String title;

    private String missionType;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private RescueTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id")
    private Disaster disaster;

    @Enumerated(EnumType.STRING)
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

    @Version
    private Long version = 0L;

    public RescueMission() {}

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
    public RescueTeam getTeam() { return team; }
    public void setTeam(RescueTeam team) { this.team = team; }
    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
