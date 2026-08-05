package com.disaster.dto;

import com.disaster.entity.CaseStudy;
import java.time.LocalDateTime;

public class CaseStudyDTO {
    private Long id;
    private String title;
    private String disasterType;
    private String location;
    private int year;
    private String severity;
    private String description;
    private String lessonsLearned;
    private double estimatedDamage;
    private int affectedPopulation;
    private double resourcesUsed;
    private double responseTimeHours;
    private double recoveryTimeDays;
    private String recommendations;
    private LocalDateTime createdAt;

    public CaseStudyDTO() {}

    public static CaseStudyDTO fromEntity(CaseStudy c) {
        CaseStudyDTO dto = new CaseStudyDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDisasterType(c.getDisasterType());
        dto.setLocation(c.getLocation());
        dto.setYear(c.getDisasterYear());
        dto.setSeverity(c.getSeverity());
        dto.setDescription(c.getDescription());
        dto.setLessonsLearned(c.getLessonsLearned());
        dto.setEstimatedDamage(c.getEstimatedDamage());
        dto.setAffectedPopulation(c.getAffectedPopulation());
        dto.setResourcesUsed(c.getResourcesUsed());
        dto.setResponseTimeHours(c.getResponseTimeHours());
        dto.setRecoveryTimeDays(c.getRecoveryTimeDays());
        dto.setRecommendations(c.getRecommendations());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLessonsLearned() { return lessonsLearned; }
    public void setLessonsLearned(String lessonsLearned) { this.lessonsLearned = lessonsLearned; }
    public double getEstimatedDamage() { return estimatedDamage; }
    public void setEstimatedDamage(double estimatedDamage) { this.estimatedDamage = estimatedDamage; }
    public int getAffectedPopulation() { return affectedPopulation; }
    public void setAffectedPopulation(int affectedPopulation) { this.affectedPopulation = affectedPopulation; }
    public double getResourcesUsed() { return resourcesUsed; }
    public void setResourcesUsed(double resourcesUsed) { this.resourcesUsed = resourcesUsed; }
    public double getResponseTimeHours() { return responseTimeHours; }
    public void setResponseTimeHours(double responseTimeHours) { this.responseTimeHours = responseTimeHours; }
    public double getRecoveryTimeDays() { return recoveryTimeDays; }
    public void setRecoveryTimeDays(double recoveryTimeDays) { this.recoveryTimeDays = recoveryTimeDays; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
