package com.disaster.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Hospital() {}
    // Builder pattern
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Hospital h = new Hospital();
        public Builder name(String v) { h.name = v; return this; }
        public Builder availableBeds(int v) { h.availableBeds = v; return this; }
        public Builder icuBeds(int v) { h.icuBeds = v; return this; }
        public Builder doctorsAvailable(int v) { h.doctorsAvailable = v; return this; }
        public Builder emergencyContact(String v) { h.emergencyContact = v; return this; }
        public Builder bloodBank(boolean v) { h.bloodBank = v; return this; }
        public Builder latitude(double v) { h.latitude = v; return this; }
        public Builder longitude(double v) { h.longitude = v; return this; }
        public Builder address(String v) { h.address = v; return this; }
        public Hospital build() { return h; }
    }
    // Getters and setters
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
}
