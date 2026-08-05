package com.disaster.dto.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A ranked volunteer recommendation scored by availability, skill match for the
 * disaster type, proximity to the affected area and prior mission experience.
 */
public class VolunteerRecommendationDTO {

    private Long id;
    private String name;
    private String phone;
    private String skills;
    private double distanceKm;
    private double skillMatch;
    private boolean available;
    private double score;
    private String matchReason;

    /** Number of past missions attended (0 when none recorded). */
    private int missionsAttended;

    /** Estimated driving time at 40 km/h. */
    private double travelTimeMinutes;

    /** Per-component contribution to the final score. */
    private Map<String, Double> scoreBreakdown = new LinkedHashMap<>();

    public VolunteerRecommendationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getSkillMatch() { return skillMatch; }
    public void setSkillMatch(double skillMatch) { this.skillMatch = skillMatch; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public int getMissionsAttended() { return missionsAttended; }
    public void setMissionsAttended(int missionsAttended) { this.missionsAttended = missionsAttended; }
    public double getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(double travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(Map<String, Double> scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
}
