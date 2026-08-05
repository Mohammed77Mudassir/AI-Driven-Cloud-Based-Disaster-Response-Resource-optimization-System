package com.disaster.entity;

import com.disaster.enums.MissionEventType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_events")
public class MissionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private RescueMission mission;

    @Enumerated(EnumType.STRING)
    private MissionEventType eventType;

    private String message;

    private String performedBy;

    private LocalDateTime occurredAt;

    public MissionEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RescueMission getMission() { return mission; }
    public void setMission(RescueMission mission) { this.mission = mission; }
    public MissionEventType getEventType() { return eventType; }
    public void setEventType(MissionEventType eventType) { this.eventType = eventType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
