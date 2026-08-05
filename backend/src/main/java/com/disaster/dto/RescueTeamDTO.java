package com.disaster.dto;

import com.disaster.entity.RescueTeam;
import java.time.LocalDateTime;

public class RescueTeamDTO {
    private Long id;
    private String teamName;
    private String teamLeader;
    private String members;
    private String vehicles;
    private String equipment;
    private String status;
    private String location;
    private double latitude;
    private double longitude;
    private String contactNumber;
    private int memberCount;
    private String specialty;
    private int maxCapacity;
    private LocalDateTime lastLocationUpdateAt;
    private Long assignedDisasterId;
    private String assignedDisasterName;
    private LocalDateTime deployedAt;
    private LocalDateTime returnedAt;

    public RescueTeamDTO() {}

    public static RescueTeamDTO fromEntity(RescueTeam r) {
        RescueTeamDTO dto = new RescueTeamDTO();
        dto.setId(r.getId());
        dto.setTeamName(r.getTeamName());
        dto.setTeamLeader(r.getTeamLeader());
        dto.setMembers(r.getMembers());
        dto.setVehicles(r.getVehicles());
        dto.setEquipment(r.getEquipment());
        dto.setStatus(r.getStatus());
        dto.setLocation(r.getLocation());
        dto.setLatitude(r.getLatitude());
        dto.setLongitude(r.getLongitude());
        dto.setContactNumber(r.getContactNumber());
        dto.setMemberCount(r.getMemberCount());
        dto.setSpecialty(r.getSpecialty());
        dto.setMaxCapacity(r.getMaxCapacity());
        dto.setLastLocationUpdateAt(r.getLastLocationUpdateAt());
        dto.setDeployedAt(r.getDeployedAt());
        dto.setReturnedAt(r.getReturnedAt());
        if (r.getAssignedDisaster() != null) {
            dto.setAssignedDisasterId(r.getAssignedDisaster().getId());
            dto.setAssignedDisasterName(r.getAssignedDisaster().getDisasterType());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamLeader() { return teamLeader; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public String getMembers() { return members; }
    public void setMembers(String members) { this.members = members; }
    public String getVehicles() { return vehicles; }
    public void setVehicles(String vehicles) { this.vehicles = vehicles; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public LocalDateTime getLastLocationUpdateAt() { return lastLocationUpdateAt; }
    public void setLastLocationUpdateAt(LocalDateTime lastLocationUpdateAt) { this.lastLocationUpdateAt = lastLocationUpdateAt; }
    public Long getAssignedDisasterId() { return assignedDisasterId; }
    public void setAssignedDisasterId(Long assignedDisasterId) { this.assignedDisasterId = assignedDisasterId; }
    public String getAssignedDisasterName() { return assignedDisasterName; }
    public void setAssignedDisasterName(String assignedDisasterName) { this.assignedDisasterName = assignedDisasterName; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}
