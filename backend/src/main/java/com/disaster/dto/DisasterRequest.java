package com.disaster.dto;

import com.disaster.entity.DisasterPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
public class DisasterRequest {

    @NotBlank(message = "Disaster type is required")
    @Size(max = 50, message = "Disaster type cannot exceed 50 characters")
    private String disasterType;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "(?i)low|medium|high|critical", message = "Severity must be one of: Low, Medium, High, Critical")
    private String severity;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location cannot exceed 500 characters")
    private String location;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private double latitude;

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private double longitude;

    private DisasterPriority priority;

    @Valid
    @Size(max = 5, message = "Maximum of 5 attachments allowed")
    private List<AttachmentRequest> attachments = new ArrayList<>();

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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public DisasterPriority getPriority() { return priority; }
    public void setPriority(DisasterPriority priority) { this.priority = priority; }
    public List<AttachmentRequest> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentRequest> attachments) { this.attachments = attachments; }
}
