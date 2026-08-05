package com.disaster.dto;

import com.disaster.entity.Shelter;

public class ShelterDTO {
    private Long id;
    private String name;
    private int capacity;
    private int occupancy;
    private boolean foodAvailable;
    private boolean waterAvailable;
    private int medicalKits;
    private boolean powerAvailable;
    private String contact;
    private double latitude;
    private double longitude;
    private String address;
    private int occupancyPercent;

    public ShelterDTO() {}
    
    public static ShelterDTO fromEntity(Shelter s) {
        ShelterDTO dto = new ShelterDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setCapacity(s.getCapacity());
        dto.setOccupancy(s.getOccupancy());
        dto.setFoodAvailable(s.isFoodAvailable());
        dto.setWaterAvailable(s.isWaterAvailable());
        dto.setMedicalKits(s.getMedicalKits());
        dto.setPowerAvailable(s.isPowerAvailable());
        dto.setContact(s.getContact());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setAddress(s.getAddress());
        dto.setOccupancyPercent(s.getCapacity() > 0 ? (s.getOccupancy() * 100 / s.getCapacity()) : 0);
        return dto;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public boolean isFoodAvailable() { return foodAvailable; }
    public void setFoodAvailable(boolean foodAvailable) { this.foodAvailable = foodAvailable; }
    public boolean isWaterAvailable() { return waterAvailable; }
    public void setWaterAvailable(boolean waterAvailable) { this.waterAvailable = waterAvailable; }
    public int getMedicalKits() { return medicalKits; }
    public void setMedicalKits(int medicalKits) { this.medicalKits = medicalKits; }
    public boolean isPowerAvailable() { return powerAvailable; }
    public void setPowerAvailable(boolean powerAvailable) { this.powerAvailable = powerAvailable; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(int occupancyPercent) { this.occupancyPercent = occupancyPercent; }
}
