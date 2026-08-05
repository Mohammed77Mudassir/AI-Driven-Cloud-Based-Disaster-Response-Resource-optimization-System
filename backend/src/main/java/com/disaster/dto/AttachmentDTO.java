package com.disaster.dto;

import com.disaster.entity.DisasterAttachment;

/**
 * Attachment metadata. The base64 {@code dataUrl} payload is populated only on
 * detail responses, never in list responses, to keep list payloads small.
 */
public class AttachmentDTO {
    private Long id;
    private String category;
    private String filename;
    private String mimeType;
    private Long size;
    private String dataUrl;

    public AttachmentDTO() {}

    public static AttachmentDTO fromEntity(DisasterAttachment a, boolean includeData) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(a.getId());
        dto.setCategory(a.getCategory() != null ? a.getCategory().name() : null);
        dto.setFilename(a.getFilename());
        dto.setMimeType(a.getMimeType());
        dto.setSize(a.getSize());
        dto.setDataUrl(includeData ? a.getDataUrl() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getDataUrl() { return dataUrl; }
    public void setDataUrl(String dataUrl) { this.dataUrl = dataUrl; }
}
