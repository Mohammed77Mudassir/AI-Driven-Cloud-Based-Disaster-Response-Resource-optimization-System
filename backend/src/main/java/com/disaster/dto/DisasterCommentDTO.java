package com.disaster.dto;

import com.disaster.entity.DisasterComment;
import java.time.LocalDateTime;

public class DisasterCommentDTO {
    private Long id;
    private Long disasterId;
    private Long authorId;
    private String author;
    private String text;
    private LocalDateTime createdAt;

    public DisasterCommentDTO() {}

    public static DisasterCommentDTO fromEntity(DisasterComment c) {
        DisasterCommentDTO dto = new DisasterCommentDTO();
        dto.setId(c.getId());
        dto.setDisasterId(c.getDisaster().getId());
        dto.setAuthorId(c.getAuthor() != null ? c.getAuthor().getId() : null);
        dto.setAuthor(c.getAuthor() != null ? c.getAuthor().getUsername() : "system");
        dto.setText(c.getText());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
