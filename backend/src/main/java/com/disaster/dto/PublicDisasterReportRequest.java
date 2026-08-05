package com.disaster.dto;

import com.disaster.entity.DisasterPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for the public, no-login disaster report flow.
 */
public class PublicDisasterReportRequest {

    @NotBlank(message = "Reporter name is required")
    @Size(max = 100)
    private String reporterName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+0-9\\-() ]{8,15}$", message = "Enter a valid mobile number")
    private String reporterMobile;

    @Email(message = "Enter a valid email address")
    @Size(max = 100)
    private String reporterEmail;

    @NotBlank(message = "Disaster type is required")
    @Size(max = 50)
    private String disasterType;

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "(?i)low|medium|high|critical", message = "Severity must be one of: Low, Medium, High, Critical")
    private String severity;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private DisasterPriority priority;

    @Valid
    @Size(max = 5, message = "Maximum of 5 attachments allowed")
    private List<AttachmentRequest> attachments = new ArrayList<>();

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReporterMobile() { return reporterMobile; }
    public void setReporterMobile(String reporterMobile) { this.reporterMobile = reporterMobile; }
    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }
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
    public DisasterPriority getPriority() { return priority; }
    public void setPriority(DisasterPriority priority) { this.priority = priority; }
    public List<AttachmentRequest> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentRequest> attachments) { this.attachments = attachments; }
}
