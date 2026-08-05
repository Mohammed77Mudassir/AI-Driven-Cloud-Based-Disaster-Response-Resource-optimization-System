package com.disaster.dto;

import com.disaster.dto.ai.ConfidenceDTO;
import com.disaster.dto.ai.ScoreFactorDTO;

import java.util.ArrayList;
import java.util.List;

public class EnhancedPredictionDTO {
    private String disasterType;
    private String severity;
    private String location;
    private Double latitude;
    private Double longitude;
    private int priorityScore;
    private int riskScore;
    private String estimatedDamageLevel;
    private long predictedAffectedPopulation;
    private double estimatedEconomicLoss;
    private double estimatedResponseTime;
    private double estimatedRecoveryTime;
    private double confidencePercentage;
    private String recommendation;

    private ConfidenceDTO confidenceBreakdown;
    private List<ScoreFactorDTO> priorityFactors = new ArrayList<>();
    private List<ScoreFactorDTO> riskFactors = new ArrayList<>();

    public EnhancedPredictionDTO() {}

    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getEstimatedDamageLevel() { return estimatedDamageLevel; }
    public void setEstimatedDamageLevel(String estimatedDamageLevel) { this.estimatedDamageLevel = estimatedDamageLevel; }
    public long getPredictedAffectedPopulation() { return predictedAffectedPopulation; }
    public void setPredictedAffectedPopulation(long predictedAffectedPopulation) { this.predictedAffectedPopulation = predictedAffectedPopulation; }
    public double getEstimatedEconomicLoss() { return estimatedEconomicLoss; }
    public void setEstimatedEconomicLoss(double estimatedEconomicLoss) { this.estimatedEconomicLoss = estimatedEconomicLoss; }
    public double getEstimatedResponseTime() { return estimatedResponseTime; }
    public void setEstimatedResponseTime(double estimatedResponseTime) { this.estimatedResponseTime = estimatedResponseTime; }
    public double getEstimatedRecoveryTime() { return estimatedRecoveryTime; }
    public void setEstimatedRecoveryTime(double estimatedRecoveryTime) { this.estimatedRecoveryTime = estimatedRecoveryTime; }
    public double getConfidencePercentage() { return confidencePercentage; }
    public void setConfidencePercentage(double confidencePercentage) { this.confidencePercentage = confidencePercentage; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public ConfidenceDTO getConfidenceBreakdown() { return confidenceBreakdown; }
    public void setConfidenceBreakdown(ConfidenceDTO confidenceBreakdown) { this.confidenceBreakdown = confidenceBreakdown; }
    public List<ScoreFactorDTO> getPriorityFactors() { return priorityFactors; }
    public void setPriorityFactors(List<ScoreFactorDTO> priorityFactors) { this.priorityFactors = priorityFactors; }
    public List<ScoreFactorDTO> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<ScoreFactorDTO> riskFactors) { this.riskFactors = riskFactors; }
}
