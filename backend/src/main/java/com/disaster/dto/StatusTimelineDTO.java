package com.disaster.dto;

import com.disaster.entity.StatusTimeline;
import java.time.LocalDateTime;

public class StatusTimelineDTO {
    private Long id;
    private Long disasterId;
    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;
    private String changedBy;
    private String comment;

    public StatusTimelineDTO() {}
    
    public static StatusTimelineDTO fromEntity(StatusTimeline st) {
        StatusTimelineDTO dto = new StatusTimelineDTO();
        dto.setId(st.getId());
        dto.setDisasterId(st.getDisaster().getId());
        dto.setFromStatus(st.getFromStatus());
        dto.setToStatus(st.getToStatus());
        dto.setChangedAt(st.getChangedAt());
        dto.setChangedBy(st.getChangedBy() != null ? st.getChangedBy().getUsername() : "system");
        dto.setComment(st.getComment());
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
