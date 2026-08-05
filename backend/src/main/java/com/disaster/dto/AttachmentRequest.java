package com.disaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Attachment payload sent by the report forms. {@code category} is one of
 * IMAGE, VIDEO or PDF; {@code dataUrl} is the base64 data URL of the file.
 */
public class AttachmentRequest {

    @NotBlank(message = "Attachment category is required")
    @Pattern(regexp = "(?i)IMAGE|VIDEO|PDF", message = "Attachment category must be IMAGE, VIDEO or PDF")
    private String category;

    @NotBlank(message = "Attachment filename is required")
    @Size(max = 255)
    private String filename;

    @Size(max = 100)
    private String mimeType;

    private Long size;

    /** Base64 payload cap (approx 5 MB of binary data encoded as base64). */
    @NotBlank(message = "Attachment data is required")
    @Size(max = 7000000, message = "Attachment exceeds the maximum allowed size")
    private String dataUrl;

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
