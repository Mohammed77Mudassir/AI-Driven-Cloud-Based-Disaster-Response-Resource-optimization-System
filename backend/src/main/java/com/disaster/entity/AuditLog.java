package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {}
    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private AuditLog a = new AuditLog();
        public Builder action(String v) { a.action = v; return this; }
        public Builder entityType(String v) { a.entityType = v; return this; }
        public Builder entityId(Long v) { a.entityId = v; return this; }
        public Builder performedBy(String v) { a.performedBy = v; return this; }
        public Builder details(String v) { a.details = v; return this; }
        public Builder timestamp(LocalDateTime v) { a.timestamp = v; return this; }
        public AuditLog build() { return a; }
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
