package com.disaster.dto.command;

import java.time.LocalDateTime;

/**
 * A single milestone in a mission's visual timeline.
 */
public class MilestoneDTO {

    private String label;
    private String eventType;
    private LocalDateTime timestamp;
    private boolean done;

    public MilestoneDTO() {}

    public MilestoneDTO(String label, String eventType, LocalDateTime timestamp, boolean done) {
        this.label = label;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.done = done;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
