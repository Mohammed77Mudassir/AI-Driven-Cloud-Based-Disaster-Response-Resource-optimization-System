package com.disaster.dto;

import com.disaster.entity.TeamMember;
import java.time.LocalDateTime;

public class TeamMemberDTO {
    private Long id;
    private String name;
    private String role;
    private String speciality;
    private String phone;
    private boolean available;
    private boolean isLeader;
    private String skills;
    private String certifications;
    private LocalDateTime joinedAt;
    private Long teamId;
    private String teamName;

    public TeamMemberDTO() {}

    public static TeamMemberDTO fromEntity(TeamMember t) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setRole(t.getRole());
        dto.setSpeciality(t.getSpeciality());
        dto.setPhone(t.getPhone());
        dto.setAvailable(t.isAvailable());
        dto.setLeader(t.isLeader());
        dto.setSkills(t.getSkills());
        dto.setCertifications(t.getCertifications());
        dto.setJoinedAt(t.getJoinedAt());
        if (t.getTeam() != null) {
            dto.setTeamId(t.getTeam().getId());
            dto.setTeamName(t.getTeam().getTeamName());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public boolean isLeader() { return isLeader; }
    public void setLeader(boolean leader) { isLeader = leader; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
}
