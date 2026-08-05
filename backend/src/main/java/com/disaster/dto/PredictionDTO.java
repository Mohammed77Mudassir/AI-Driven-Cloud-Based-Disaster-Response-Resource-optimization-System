package com.disaster.dto;

import com.disaster.dto.ai.ConfidenceDTO;
import com.disaster.dto.ai.ScoreFactorDTO;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class PredictionDTO {
    @NotBlank(message = "Disaster type is required")
    private String disasterType;
    @NotBlank(message = "Severity is required")
    private String severity;
    private String location;
    private double latitude;
    private double longitude;
    private String estimatedDamageLevel;
    private long predictedAffectedPopulation;
    private double estimatedEconomicLoss;
    private String estimatedRecoveryTime;
    private double confidenceScore;
    private int priorityScore;
    private int riskScore;
    private double estimatedResponseTime;
    private String recommendation;

    private ConfidenceDTO confidenceBreakdown;
    private List<ScoreFactorDTO> priorityFactors = new ArrayList<>();
    private List<ScoreFactorDTO> riskFactors = new ArrayList<>();

    public PredictionDTO() {}
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getEstimatedDamageLevel() { return estimatedDamageLevel; }
    public void setEstimatedDamageLevel(String estimatedDamageLevel) { this.estimatedDamageLevel = estimatedDamageLevel; }
    public long getPredictedAffectedPopulation() { return predictedAffectedPopulation; }
    public void setPredictedAffectedPopulation(long predictedAffectedPopulation) { this.predictedAffectedPopulation = predictedAffectedPopulation; }
    public double getEstimatedEconomicLoss() { return estimatedEconomicLoss; }
    public void setEstimatedEconomicLoss(double estimatedEconomicLoss) { this.estimatedEconomicLoss = estimatedEconomicLoss; }
    public String getEstimatedRecoveryTime() { return estimatedRecoveryTime; }
    public void setEstimatedRecoveryTime(String estimatedRecoveryTime) { this.estimatedRecoveryTime = estimatedRecoveryTime; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public double getEstimatedResponseTime() { return estimatedResponseTime; }
    public void setEstimatedResponseTime(double estimatedResponseTime) { this.estimatedResponseTime = estimatedResponseTime; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public ConfidenceDTO getConfidenceBreakdown() { return confidenceBreakdown; }
    public void setConfidenceBreakdown(ConfidenceDTO confidenceBreakdown) { this.confidenceBreakdown = confidenceBreakdown; }
    public List<ScoreFactorDTO> getPriorityFactors() { return priorityFactors; }
    public void setPriorityFactors(List<ScoreFactorDTO> priorityFactors) { this.priorityFactors = priorityFactors; }
    public List<ScoreFactorDTO> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<ScoreFactorDTO> riskFactors) { this.riskFactors = riskFactors; }
}
