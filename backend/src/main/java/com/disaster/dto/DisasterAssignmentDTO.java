package com.disaster.dto;

import com.disaster.entity.DisasterAssignment;
import java.time.LocalDateTime;

public class DisasterAssignmentDTO {
    private Long id;
    private Long disasterId;
    private Long teamId;
    private String teamName;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime releasedAt;
    private String action;

    public DisasterAssignmentDTO() {}

    public static DisasterAssignmentDTO fromEntity(DisasterAssignment a) {
        DisasterAssignmentDTO dto = new DisasterAssignmentDTO();
        dto.setId(a.getId());
        dto.setDisasterId(a.getDisaster().getId());
        dto.setTeamId(a.getTeam() != null ? a.getTeam().getId() : null);
        dto.setTeamName(a.getTeamName());
        dto.setAssignedBy(a.getAssignedBy());
        dto.setAssignedAt(a.getAssignedAt());
        dto.setReleasedAt(a.getReleasedAt());
        dto.setAction(a.getAction());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
