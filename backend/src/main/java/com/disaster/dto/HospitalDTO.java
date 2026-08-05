package com.disaster.dto;

import com.disaster.entity.Hospital;

public class HospitalDTO {
    private Long id;
    private String name;
    private int availableBeds;
    private int icuBeds;
    private int doctorsAvailable;
    private String emergencyContact;
    private boolean bloodBank;
    private double latitude;
    private double longitude;
    private String address;
    private int occupancyPercent;

    public HospitalDTO() {}
    
    public static HospitalDTO fromEntity(Hospital h) {
        HospitalDTO dto = new HospitalDTO();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setAvailableBeds(h.getAvailableBeds());
        dto.setIcuBeds(h.getIcuBeds());
        dto.setDoctorsAvailable(h.getDoctorsAvailable());
        dto.setEmergencyContact(h.getEmergencyContact());
        dto.setBloodBank(h.isBloodBank());
        dto.setLatitude(h.getLatitude());
        dto.setLongitude(h.getLongitude());
        dto.setAddress(h.getAddress());
        int total = h.getAvailableBeds() + h.getIcuBeds();
        dto.setOccupancyPercent(total > 0 ? Math.min(100, 100 - (h.getAvailableBeds() * 100 / total)) : 0);
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }
    public int getIcuBeds() { return icuBeds; }
    public void setIcuBeds(int icuBeds) { this.icuBeds = icuBeds; }
    public int getDoctorsAvailable() { return doctorsAvailable; }
    public void setDoctorsAvailable(int doctorsAvailable) { this.doctorsAvailable = doctorsAvailable; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public boolean isBloodBank() { return bloodBank; }
    public void setBloodBank(boolean bloodBank) { this.bloodBank = bloodBank; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(int occupancyPercent) { this.occupancyPercent = occupancyPercent; }
}
