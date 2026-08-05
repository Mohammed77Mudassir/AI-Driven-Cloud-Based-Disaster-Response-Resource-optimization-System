package com.disaster.dto;

import com.disaster.entity.AuditLog;
import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}
    
    public static AuditLogDTO fromEntity(AuditLog a) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(a.getId());
        dto.setAction(a.getAction());
        dto.setEntityType(a.getEntityType());
        dto.setEntityId(a.getEntityId());
        dto.setPerformedBy(a.getPerformedBy());
        dto.setDetails(a.getDetails());
        dto.setTimestamp(a.getTimestamp());
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
