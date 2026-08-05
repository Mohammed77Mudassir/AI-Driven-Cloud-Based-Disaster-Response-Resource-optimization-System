package com.disaster.dto;

import com.disaster.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public class UserActivityDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime lastLogin;
    private int totalReports;
    private int assignedDisasters;
    private LocalDateTime createdAt;
    private List<AuditLogDTO> recentActivity;

    public UserActivityDTO() {}

    public static UserActivityDTO fromEntity(User u, List<AuditLogDTO> recentActivity) {
        UserActivityDTO dto = new UserActivityDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole() != null ? u.getRole().name() : "USER");
        dto.setLastLogin(u.getLastLogin());
        dto.setTotalReports(u.getTotalReports());
        dto.setAssignedDisasters(u.getAssignedDisasters());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setRecentActivity(recentActivity);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public int getTotalReports() { return totalReports; }
    public void setTotalReports(int totalReports) { this.totalReports = totalReports; }
    public int getAssignedDisasters() { return assignedDisasters; }
    public void setAssignedDisasters(int assignedDisasters) { this.assignedDisasters = assignedDisasters; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<AuditLogDTO> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<AuditLogDTO> recentActivity) { this.recentActivity = recentActivity; }
}
