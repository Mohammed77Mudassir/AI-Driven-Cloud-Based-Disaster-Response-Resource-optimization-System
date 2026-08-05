package com.disaster.dto;

import com.disaster.entity.TeamShift;
import com.disaster.enums.ShiftStatus;
import com.disaster.enums.ShiftType;
import java.time.LocalDateTime;

public class TeamShiftDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long memberId;
    private String memberName;
    private ShiftType shiftType;
    private ShiftStatus shiftStatus;
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private String notes;
    private String createdBy;

    public TeamShiftDTO() {}

    public static TeamShiftDTO fromEntity(TeamShift s) {
        TeamShiftDTO dto = new TeamShiftDTO();
        dto.setId(s.getId());
        if (s.getTeam() != null) {
            dto.setTeamId(s.getTeam().getId());
            dto.setTeamName(s.getTeam().getTeamName());
        }
        if (s.getMember() != null) {
            dto.setMemberId(s.getMember().getId());
            dto.setMemberName(s.getMember().getName());
        }
        dto.setShiftType(s.getShiftType());
        dto.setShiftStatus(s.getShiftStatus());
        dto.setShiftStart(s.getShiftStart());
        dto.setShiftEnd(s.getShiftEnd());
        dto.setNotes(s.getNotes());
        dto.setCreatedBy(s.getCreatedBy());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }
    public ShiftStatus getShiftStatus() { return shiftStatus; }
    public void setShiftStatus(ShiftStatus shiftStatus) { this.shiftStatus = shiftStatus; }
    public LocalDateTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalDateTime shiftStart) { this.shiftStart = shiftStart; }
    public LocalDateTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalDateTime shiftEnd) { this.shiftEnd = shiftEnd; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
