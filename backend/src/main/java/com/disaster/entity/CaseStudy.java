package com.disaster.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_studies")
public class CaseStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String disasterType;

    private String location;

    @Column(name = "disaster_year")
    private int disasterYear;

    private String severity;

    private String description;

    @Column(length = 2000)
    private String lessonsLearned;

    private double estimatedDamage;

    private int affectedPopulation;

    private double resourcesUsed;

    private double responseTimeHours;

    private double recoveryTimeDays;

    private String recommendations;

    private LocalDateTime createdAt;

    public CaseStudy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getDisasterYear() { return disasterYear; }
    public void setDisasterYear(int disasterYear) { this.disasterYear = disasterYear; }
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
