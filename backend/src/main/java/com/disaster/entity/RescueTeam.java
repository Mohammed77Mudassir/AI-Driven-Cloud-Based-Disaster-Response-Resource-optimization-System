package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_teams")
public class RescueTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_disaster_id")
    private Disaster assignedDisaster;

    private LocalDateTime deployedAt;

    private LocalDateTime returnedAt;

    @Version
    private Long version = 0L;

    public RescueTeam() {}

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
    public Disaster getAssignedDisaster() { return assignedDisaster; }
    public void setAssignedDisaster(Disaster assignedDisaster) { this.assignedDisaster = assignedDisaster; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
