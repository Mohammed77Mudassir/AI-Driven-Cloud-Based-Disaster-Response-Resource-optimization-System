package com.disaster.entity;

import com.disaster.enums.ShiftStatus;
import com.disaster.enums.ShiftType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_shifts")
public class TeamShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private RescueTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private TeamMember member;

    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    private ShiftStatus shiftStatus;

    private LocalDateTime shiftStart;

    private LocalDateTime shiftEnd;

    private String notes;

    private String createdBy;

    @Version
    private Long version = 0L;

    public TeamShift() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RescueTeam getTeam() { return team; }
    public void setTeam(RescueTeam team) { this.team = team; }
    public TeamMember getMember() { return member; }
    public void setMember(TeamMember member) { this.member = member; }
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
