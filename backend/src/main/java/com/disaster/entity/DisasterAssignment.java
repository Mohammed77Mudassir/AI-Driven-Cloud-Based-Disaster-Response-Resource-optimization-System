package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable audit-style record of a rescue team being assigned to (or released
 * from) a disaster. Provides the assignment history required for the disaster
 * detail view.
 */
@Entity
@Table(name = "disaster_assignments")
public class DisasterAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id")
    private Disaster disaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private RescueTeam team;

    @Column(length = 255)
    private String teamName;

    @Column(length = 255)
    private String assignedBy;

    private LocalDateTime assignedAt;

    private LocalDateTime releasedAt;

    @Column(length = 20)
    private String action = "ASSIGNED";

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }

    public DisasterAssignment() {}

    public DisasterAssignment(Disaster disaster, RescueTeam team, String assignedBy, String action) {
        this.disaster = disaster;
        this.team = team;
        this.teamName = team.getTeamName();
        this.assignedBy = assignedBy;
        this.action = action;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }
    public RescueTeam getTeam() { return team; }
    public void setTeam(RescueTeam team) { this.team = team; }
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
