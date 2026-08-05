package com.disaster.dto;

import com.disaster.entity.MissionEvent;
import com.disaster.enums.MissionEventType;
import java.time.LocalDateTime;

public class MissionEventDTO {
    private Long id;
    private Long missionId;
    private MissionEventType eventType;
    private String message;
    private String performedBy;
    private LocalDateTime occurredAt;

    public MissionEventDTO() {}

    public static MissionEventDTO fromEntity(MissionEvent e) {
        MissionEventDTO dto = new MissionEventDTO();
        dto.setId(e.getId());
        if (e.getMission() != null) dto.setMissionId(e.getMission().getId());
        dto.setEventType(e.getEventType());
        dto.setMessage(e.getMessage());
        dto.setPerformedBy(e.getPerformedBy());
        dto.setOccurredAt(e.getOccurredAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public MissionEventType getEventType() { return eventType; }
    public void setEventType(MissionEventType eventType) { this.eventType = eventType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
