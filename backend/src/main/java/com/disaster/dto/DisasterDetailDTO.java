package com.disaster.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full disaster detail envelope used by the authorized detail view. Includes
 * attachment payloads, status timeline, discussion comments and rescue team
 * assignment history in a single response.
 */
public class DisasterDetailDTO {
    private Long id;
    private String disasterType;
    private String description;
    private String severity;
    private String status;
    private String priority;
    private String location;
    private double latitude;
    private double longitude;
    private LocalDateTime date;
    private String reportedBy;
    private String reportId;
    private String reporterName;
    private String reporterMobile;
    private String reporterEmail;
    private String address;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentDTO> attachments;
    private List<StatusTimelineDTO> timeline;
    private List<DisasterCommentDTO> comments;
    private List<DisasterAssignmentDTO> assignments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<AttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDTO> attachments) { this.attachments = attachments; }
    public List<StatusTimelineDTO> getTimeline() { return timeline; }
    public void setTimeline(List<StatusTimelineDTO> timeline) { this.timeline = timeline; }
    public List<DisasterCommentDTO> getComments() { return comments; }
    public void setComments(List<DisasterCommentDTO> comments) { this.comments = comments; }
    public List<DisasterAssignmentDTO> getAssignments() { return assignments; }
    public void setAssignments(List<DisasterAssignmentDTO> assignments) { this.assignments = assignments; }
}
