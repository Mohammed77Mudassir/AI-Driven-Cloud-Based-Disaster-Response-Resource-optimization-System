package com.disaster.dto;

import com.disaster.entity.Disaster;
import com.disaster.entity.DisasterAttachment;
import com.disaster.entity.DisasterPriority;
import com.disaster.entity.DisasterStatus;
import java.time.LocalDateTime;
import java.util.List;

public class DisasterResponse {
    private Long id;
    private String disasterType;
    private String description;
    private String severity;
    private String location;
    private double latitude;
    private double longitude;
    private LocalDateTime date;
    private DisasterStatus status;
    private DisasterPriority priority;
    private String reportedBy;
    private String reportId;
    private String reporterName;
    private String reporterMobile;
    private String reporterEmail;
    private String address;
    private String source;
    private List<AttachmentDTO> attachments;

    public static DisasterResponse fromEntity(Disaster disaster) {
        return fromEntity(disaster, List.of());
    }

    public static DisasterResponse fromEntity(Disaster disaster, List<DisasterAttachment> attachments) {
        DisasterResponse dto = new DisasterResponse();
        dto.setId(disaster.getId());
        dto.setDisasterType(disaster.getDisasterType());
        dto.setDescription(disaster.getDescription());
        dto.setSeverity(disaster.getSeverity());
        dto.setLocation(disaster.getLocation());
        dto.setLatitude(disaster.getLatitude());
        dto.setLongitude(disaster.getLongitude());
        dto.setDate(disaster.getDate());
        dto.setStatus(disaster.getStatus());
        dto.setPriority(disaster.getPriority());
        dto.setReportedBy(disaster.getUser() != null ? disaster.getUser().getUsername() : disaster.getReporterName());
        dto.setReportId(disaster.getReportId());
        dto.setReporterName(disaster.getReporterName());
        dto.setReporterMobile(disaster.getReporterMobile());
        dto.setReporterEmail(disaster.getReporterEmail());
        dto.setAddress(disaster.getAddress());
        dto.setSource(disaster.getSource());
        dto.setAttachments(attachments.stream()
                .map(a -> AttachmentDTO.fromEntity(a, false))
                .toList());
        return dto;
    }

    /**
     * Returns a copy with personally-identifiable reporter contact details
     * removed. Used for broadcasts over the unauthenticated WebSocket feed.
     */
    public DisasterResponse redactReporterInfo() {
        DisasterResponse copy = new DisasterResponse();
        copy.setId(this.id);
        copy.setDisasterType(this.disasterType);
        copy.setDescription(this.description);
        copy.setSeverity(this.severity);
        copy.setLocation(this.location);
        copy.setLatitude(this.latitude);
        copy.setLongitude(this.longitude);
        copy.setDate(this.date);
        copy.setStatus(this.status);
        copy.setPriority(this.priority);
        copy.setReportedBy(this.reportedBy);
        copy.setReportId(this.reportId);
        copy.setReporterName(this.reporterName);
        copy.setReporterMobile(null);
        copy.setReporterEmail(null);
        copy.setAddress(this.address);
        copy.setSource(this.source);
        copy.setAttachments(this.attachments);
        return copy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public DisasterStatus getStatus() { return status; }
    public void setStatus(DisasterStatus status) { this.status = status; }
    public DisasterPriority getPriority() { return priority; }
    public void setPriority(DisasterPriority priority) { this.priority = priority; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReporterMobile() { return reporterMobile; }
    public void setReporterMobile(String reporterMobile) { this.reporterMobile = reporterMobile; }
    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public List<AttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDTO> attachments) { this.attachments = attachments; }
}
