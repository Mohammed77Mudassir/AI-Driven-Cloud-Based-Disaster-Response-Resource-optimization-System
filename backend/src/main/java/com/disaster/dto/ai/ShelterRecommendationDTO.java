package com.disaster.dto.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A ranked shelter recommendation scored by proximity, remaining capacity,
 * food/water/power availability and medical kit supplies, with an explainable
 * score breakdown and overcrowding flag.
 */
public class ShelterRecommendationDTO {

    private Long id;
    private String name;
    private String address;
    private String contact;
    private double distanceKm;
    private int capacity;
    private int occupancy;
    private int availableSpace;
    private boolean foodAvailable;
    private boolean waterAvailable;
    private boolean powerAvailable;
    private int medicalKits;
    private double score;
    private String matchReason;

    /** Estimated driving time at 40 km/h. */
    private double travelTimeMinutes;

    /** Current occupancy as a percentage of capacity (0-100). */
    private double occupancyPercent;

    /** True when occupancy reaches at least 90% of capacity. */
    private boolean overflowRisk;

    /** Per-component contribution to the final score. */
    private Map<String, Double> scoreBreakdown = new LinkedHashMap<>();

    public ShelterRecommendationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public int getAvailableSpace() { return availableSpace; }
    public void setAvailableSpace(int availableSpace) { this.availableSpace = availableSpace; }
    public boolean isFoodAvailable() { return foodAvailable; }
    public void setFoodAvailable(boolean foodAvailable) { this.foodAvailable = foodAvailable; }
    public boolean isWaterAvailable() { return waterAvailable; }
    public void setWaterAvailable(boolean waterAvailable) { this.waterAvailable = waterAvailable; }
    public boolean isPowerAvailable() { return powerAvailable; }
    public void setPowerAvailable(boolean powerAvailable) { this.powerAvailable = powerAvailable; }
    public int getMedicalKits() { return medicalKits; }
    public void setMedicalKits(int medicalKits) { this.medicalKits = medicalKits; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public double getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(double travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }
    public double getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(double occupancyPercent) { this.occupancyPercent = occupancyPercent; }
    public boolean isOverflowRisk() { return overflowRisk; }
    public void setOverflowRisk(boolean overflowRisk) { this.overflowRisk = overflowRisk; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(Map<String, Double> scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
}
