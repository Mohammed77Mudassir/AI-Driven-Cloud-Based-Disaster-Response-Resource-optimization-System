package com.disaster.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class CaseStudyRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Disaster type is required")
    private String disasterType;
    @NotBlank(message = "Location is required")
    private String location;
    @Min(value = 1950, message = "Year must be 1950 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private int year;
    @NotBlank(message = "Severity is required")
    private String severity;
    private String description;
    private String lessonsLearned;
    @PositiveOrZero
    private double estimatedDamage;
    @PositiveOrZero
    private int affectedPopulation;
    @PositiveOrZero
    private double resourcesUsed;
    @PositiveOrZero
    private double responseTimeHours;
    @PositiveOrZero
    private double recoveryTimeDays;
    private String recommendations;

    public CaseStudyRequest() {}

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
}
