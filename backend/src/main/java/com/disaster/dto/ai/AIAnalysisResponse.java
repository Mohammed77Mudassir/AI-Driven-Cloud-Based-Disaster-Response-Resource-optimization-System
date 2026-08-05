package com.disaster.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete AI analysis response for a single disaster scenario. A professional
 * envelope carrying damage, risk, priority, time estimates and recommendations,
 * together with model metadata and explainable confidence.
 */
public class AIAnalysisResponse {

    private String analysisId;
    private LocalDateTime generatedAt;
    private String disclaimer;

    private String disasterType;
    private String severity;
    private String location;
    private double latitude;
    private double longitude;

    private ModelMetaDTO model;
    private DamagePredictionDTO damage;
    private RiskScoreDTO risk;
    private PriorityScoreDTO priority;
    private TimeEstimateDTO recoveryTime;
    private TimeEstimateDTO responseTime;
    private ConfidenceDTO confidence;
    private List<String> recommendations = new ArrayList<>();

    public AIAnalysisResponse() {}

    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
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
    public ModelMetaDTO getModel() { return model; }
    public void setModel(ModelMetaDTO model) { this.model = model; }
    public DamagePredictionDTO getDamage() { return damage; }
    public void setDamage(DamagePredictionDTO damage) { this.damage = damage; }
    public RiskScoreDTO getRisk() { return risk; }
    public void setRisk(RiskScoreDTO risk) { this.risk = risk; }
    public PriorityScoreDTO getPriority() { return priority; }
    public void setPriority(PriorityScoreDTO priority) { this.priority = priority; }
    public TimeEstimateDTO getRecoveryTime() { return recoveryTime; }
    public void setRecoveryTime(TimeEstimateDTO recoveryTime) { this.recoveryTime = recoveryTime; }
    public TimeEstimateDTO getResponseTime() { return responseTime; }
    public void setResponseTime(TimeEstimateDTO responseTime) { this.responseTime = responseTime; }
    public ConfidenceDTO getConfidence() { return confidence; }
    public void setConfidence(ConfidenceDTO confidence) { this.confidence = confidence; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
