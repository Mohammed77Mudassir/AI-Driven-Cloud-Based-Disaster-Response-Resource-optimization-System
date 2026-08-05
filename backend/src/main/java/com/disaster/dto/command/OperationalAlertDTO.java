package com.disaster.dto.command;

import java.time.LocalDateTime;

/**
 * An operational alert surfaced by the command center (delayed missions, low
 * fuel, equipment shortage, inactive teams, maintenance due, deployments).
 */
public class OperationalAlertDTO {

    private String severity;
    private String category;
    private String message;
    private String entityType;
    private Long entityId;
    private String entityName;
    private LocalDateTime timestamp;

    public OperationalAlertDTO() {}

    public OperationalAlertDTO(String severity, String category, String message,
                               String entityType, Long entityId, String entityName,
                               LocalDateTime timestamp) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.timestamp = timestamp;
    }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
