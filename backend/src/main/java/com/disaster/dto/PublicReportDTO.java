package com.disaster.dto;

import com.disaster.entity.Disaster;
import com.disaster.entity.DisasterAttachment;
import com.disaster.entity.DisasterPriority;
import com.disaster.entity.DisasterStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public-facing response for a citizen report. Deliberately does NOT expose
 * internal user information; only the report id, submitted content and the
 * current status lifecycle are shared.
 */
public class PublicReportDTO {

    private String reportId;
    private String reporterName;
    private String disasterType;
    private String severity;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime date;
    private DisasterStatus status;
    private DisasterPriority priority;
    private List<AttachmentDTO> attachments;

    public static PublicReportDTO fromEntity(Disaster d) {
        return fromEntity(d, List.of(), false);
    }

    public static PublicReportDTO fromEntity(Disaster d, List<DisasterAttachment> attachments, boolean includeData) {
        PublicReportDTO dto = new PublicReportDTO();
        dto.setReportId(d.getReportId());
        dto.setReporterName(d.getReporterName());
        dto.setDisasterType(d.getDisasterType());
        dto.setSeverity(d.getSeverity());
        dto.setDescription(d.getDescription());
        dto.setAddress(d.getAddress() != null ? d.getAddress() : d.getLocation());
        dto.setLatitude(d.getLatitude());
        dto.setLongitude(d.getLongitude());
        dto.setDate(d.getDate());
        dto.setStatus(d.getStatus());
        dto.setPriority(d.getPriority());
        dto.setAttachments(attachments.stream()
                .map(a -> AttachmentDTO.fromEntity(a, includeData))
                .toList());
        return dto;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public DisasterStatus getStatus() { return status; }
    public void setStatus(DisasterStatus status) { this.status = status; }
    public DisasterPriority getPriority() { return priority; }
    public void setPriority(DisasterPriority priority) { this.priority = priority; }
    public List<AttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDTO> attachments) { this.attachments = attachments; }
}
