package com.disaster.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shelters")
public class Shelter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Shelter() {}
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Shelter s = new Shelter();
        public Builder name(String v) { s.name = v; return this; }
        public Builder capacity(int v) { s.capacity = v; return this; }
        public Builder occupancy(int v) { s.occupancy = v; return this; }
        public Builder foodAvailable(boolean v) { s.foodAvailable = v; return this; }
        public Builder waterAvailable(boolean v) { s.waterAvailable = v; return this; }
        public Builder medicalKits(int v) { s.medicalKits = v; return this; }
        public Builder powerAvailable(boolean v) { s.powerAvailable = v; return this; }
        public Builder contact(String v) { s.contact = v; return this; }
        public Builder latitude(double v) { s.latitude = v; return this; }
        public Builder longitude(double v) { s.longitude = v; return this; }
        public Builder address(String v) { s.address = v; return this; }
        public Shelter build() { return s; }
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
}
