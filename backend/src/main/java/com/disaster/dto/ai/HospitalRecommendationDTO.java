package com.disaster.dto.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A ranked hospital recommendation scored by proximity, capacity, ICU strength,
 * doctors available and blood bank support, with explainable score breakdown.
 */
public class HospitalRecommendationDTO {

    private Long id;
    private String name;
    private String address;
    private String emergencyContact;
    private double distanceKm;
    private int availableBeds;
    private int icuBeds;
    private int doctorsAvailable;
    private boolean bloodBank;
    private double score;
    private String matchReason;

    /** Estimated driving time at 40 km/h. */
    private double travelTimeMinutes;

    /** Occupancy proxy (0-100) using available beds relative to available + ICU. */
    private double occupancyPercent;

    /** Per-component contribution to the final score. */
    private Map<String, Double> scoreBreakdown = new LinkedHashMap<>();

    public HospitalRecommendationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }
    public int getIcuBeds() { return icuBeds; }
    public void setIcuBeds(int icuBeds) { this.icuBeds = icuBeds; }
    public int getDoctorsAvailable() { return doctorsAvailable; }
    public void setDoctorsAvailable(int doctorsAvailable) { this.doctorsAvailable = doctorsAvailable; }
    public boolean isBloodBank() { return bloodBank; }
    public void setBloodBank(boolean bloodBank) { this.bloodBank = bloodBank; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public double getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(double travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }
    public double getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(double occupancyPercent) { this.occupancyPercent = occupancyPercent; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(Map<String, Double> scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
}
