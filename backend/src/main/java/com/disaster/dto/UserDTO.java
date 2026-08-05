package com.disaster.dto;

import com.disaster.entity.Role;
import com.disaster.entity.User;
import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String profilePhoto;
    private String emergencyContact;
    private Role role;
    private boolean active;
    private LocalDateTime lastLogin;
    private int totalReports;
    private int assignedDisasters;
    private LocalDateTime createdAt;

    public UserDTO() {}

    public static UserDTO fromEntity(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setAddress(u.getAddress());
        dto.setProfilePhoto(u.getProfilePhoto());
        dto.setEmergencyContact(u.getEmergencyContact());
        dto.setRole(u.getRole());
        dto.setActive(u.isActive());
        dto.setLastLogin(u.getLastLogin());
        dto.setTotalReports(u.getTotalReports());
        dto.setAssignedDisasters(u.getAssignedDisasters());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public int getTotalReports() { return totalReports; }
    public void setTotalReports(int totalReports) { this.totalReports = totalReports; }
    public int getAssignedDisasters() { return assignedDisasters; }
    public void setAssignedDisasters(int assignedDisasters) { this.assignedDisasters = assignedDisasters; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
