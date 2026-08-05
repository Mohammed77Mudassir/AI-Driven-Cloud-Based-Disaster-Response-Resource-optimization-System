package com.disaster.dto;

import com.disaster.entity.DisasterStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull private DisasterStatus status;
    private String comment;

    public DisasterStatus getStatus() { return status; }
    public void setStatus(DisasterStatus status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
