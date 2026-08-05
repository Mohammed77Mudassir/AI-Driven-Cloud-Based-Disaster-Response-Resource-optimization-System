package com.disaster.dto;

import com.disaster.entity.Volunteer;

public class VolunteerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String skills;
    private boolean available;
    private Long assignedDisasterId;
    private String assignedDisasterName;
    private boolean attended;
    private double latitude;
    private double longitude;

    public VolunteerDTO() {}
    
    public static VolunteerDTO fromEntity(Volunteer v) {
        VolunteerDTO dto = new VolunteerDTO();
        dto.setId(v.getId());
        dto.setName(v.getName());
        dto.setEmail(v.getEmail());
        dto.setPhone(v.getPhone());
        dto.setSkills(v.getSkills());
        dto.setAvailable(v.isAvailable());
        dto.setAttended(v.isAttended());
        dto.setLatitude(v.getLatitude());
        dto.setLongitude(v.getLongitude());
        if (v.getAssignedDisaster() != null) {
            dto.setAssignedDisasterId(v.getAssignedDisaster().getId());
            dto.setAssignedDisasterName(v.getAssignedDisaster().getDisasterType());
        }
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Long getAssignedDisasterId() { return assignedDisasterId; }
    public void setAssignedDisasterId(Long assignedDisasterId) { this.assignedDisasterId = assignedDisasterId; }
    public String getAssignedDisasterName() { return assignedDisasterName; }
    public void setAssignedDisasterName(String assignedDisasterName) { this.assignedDisasterName = assignedDisasterName; }
    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
