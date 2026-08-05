package com.disaster.dto;

import com.disaster.dto.ai.ConfidenceDTO;
import com.disaster.dto.ai.ResourceAllocationDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

public class ResourceRecommendationDTO {
    @NotBlank(message = "Disaster type is required")
    private String disasterType;
    @NotBlank(message = "Severity is required")
    private String severity;
    private String location;
    @PositiveOrZero
    private long population;
    private int ambulances;
    private int fireTrucks;
    private int policeTeams;
    private int medicalTeams;
    private int rescueWorkers;
    private int volunteers;
    private int boats;
    private int helicopters;
    private int generators;
    private String reasoning;
    private String priority;
    private double confidenceScore;
    private double estimatedResponseTime;
    private double estimatedRecoveryTime;
    private String evacuationRecommendation;
    private String hospitalRecommendation;
    private String shelterRecommendation;
    private String volunteerRecommendation;

    private ConfidenceDTO confidenceBreakdown;
    private List<ResourceAllocationDTO> allocations = new ArrayList<>();

    public ResourceRecommendationDTO() {}
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getPopulation() { return population; }
    public void setPopulation(long population) { this.population = population; }
    public int getAmbulances() { return ambulances; }
    public void setAmbulances(int ambulances) { this.ambulances = ambulances; }
    public int getFireTrucks() { return fireTrucks; }
    public void setFireTrucks(int fireTrucks) { this.fireTrucks = fireTrucks; }
    public int getPoliceTeams() { return policeTeams; }
    public void setPoliceTeams(int policeTeams) { this.policeTeams = policeTeams; }
    public int getMedicalTeams() { return medicalTeams; }
    public void setMedicalTeams(int medicalTeams) { this.medicalTeams = medicalTeams; }
    public int getRescueWorkers() { return rescueWorkers; }
    public void setRescueWorkers(int rescueWorkers) { this.rescueWorkers = rescueWorkers; }
    public int getVolunteers() { return volunteers; }
    public void setVolunteers(int volunteers) { this.volunteers = volunteers; }
    public int getBoats() { return boats; }
    public void setBoats(int boats) { this.boats = boats; }
    public int getHelicopters() { return helicopters; }
    public void setHelicopters(int helicopters) { this.helicopters = helicopters; }
    public int getGenerators() { return generators; }
    public void setGenerators(int generators) { this.generators = generators; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public double getEstimatedResponseTime() { return estimatedResponseTime; }
    public void setEstimatedResponseTime(double estimatedResponseTime) { this.estimatedResponseTime = estimatedResponseTime; }
    public double getEstimatedRecoveryTime() { return estimatedRecoveryTime; }
    public void setEstimatedRecoveryTime(double estimatedRecoveryTime) { this.estimatedRecoveryTime = estimatedRecoveryTime; }
    public String getEvacuationRecommendation() { return evacuationRecommendation; }
    public void setEvacuationRecommendation(String evacuationRecommendation) { this.evacuationRecommendation = evacuationRecommendation; }
    public String getHospitalRecommendation() { return hospitalRecommendation; }
    public void setHospitalRecommendation(String hospitalRecommendation) { this.hospitalRecommendation = hospitalRecommendation; }
    public String getShelterRecommendation() { return shelterRecommendation; }
    public void setShelterRecommendation(String shelterRecommendation) { this.shelterRecommendation = shelterRecommendation; }
    public String getVolunteerRecommendation() { return volunteerRecommendation; }
    public void setVolunteerRecommendation(String volunteerRecommendation) { this.volunteerRecommendation = volunteerRecommendation; }
    public ConfidenceDTO getConfidenceBreakdown() { return confidenceBreakdown; }
    public void setConfidenceBreakdown(ConfidenceDTO confidenceBreakdown) { this.confidenceBreakdown = confidenceBreakdown; }
    public List<ResourceAllocationDTO> getAllocations() { return allocations; }
    public void setAllocations(List<ResourceAllocationDTO> allocations) { this.allocations = allocations; }
}
