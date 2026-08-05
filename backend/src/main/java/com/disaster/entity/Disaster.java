package com.disaster.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "disasters")
@SQLRestriction("deleted = false")
public class Disaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String disasterType;

    @NotBlank
    @Column(length = 1000)
    private String description;

    @NotBlank
    private String severity;

    @NotBlank
    @Column(length = 500)
    private String location;

    private double latitude;
    private double longitude;

    @NotNull
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private DisasterStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DisasterPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- Public citizen report fields ---
    @Column(name = "report_id", length = 50, unique = true)
    private String reportId;

    @Column(name = "reporter_name", length = 255)
    private String reporterName;

    @Column(name = "reporter_mobile", length = 50)
    private String reporterMobile;

    @Column(name = "reporter_email", length = 255)
    private String reporterEmail;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "image_urls", length = 10000)
    private String imageUrls;

    @Column(name = "video_urls", length = 10000)
    private String videoUrls;

    @Column(name = "pdf_urls", length = 10000)
    private String pdfUrls;

    @Column(name = "source", length = 20)
    private String source = "SYSTEM";

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Disaster() {}

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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
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
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getVideoUrls() { return videoUrls; }
    public void setVideoUrls(String videoUrls) { this.videoUrls = videoUrls; }
    public String getPdfUrls() { return pdfUrls; }
    public void setPdfUrls(String pdfUrls) { this.pdfUrls = pdfUrls; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
