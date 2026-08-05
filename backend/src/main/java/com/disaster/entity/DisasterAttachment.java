package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A file attachment (image, video or PDF) attached to a disaster report.
 * The binary payload is stored as a base64 data URL in TEXT format so that
 * both the public (no-login) and authorized report flows can upload evidence
 * without a separate file server. Metadata is always exposed; the payload is
 * only returned on detail views.
 */
@Entity
@Table(name = "disaster_attachments")
public class DisasterAttachment {

    public enum Category { IMAGE, VIDEO, PDF }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id")
    private Disaster disaster;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Category category;

    @Column(length = 255)
    private String filename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    private Long size;

    @Column(name = "data_url", columnDefinition = "TEXT")
    private String dataUrl;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public DisasterAttachment() {}

    public DisasterAttachment(Disaster disaster, Category category, String filename,
                              String mimeType, Long size, String dataUrl) {
        this.disaster = disaster;
        this.category = category;
        this.filename = filename;
        this.mimeType = mimeType;
        this.size = size;
        this.dataUrl = dataUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getDataUrl() { return dataUrl; }
    public void setDataUrl(String dataUrl) { this.dataUrl = dataUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}