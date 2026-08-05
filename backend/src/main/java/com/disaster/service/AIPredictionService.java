package com.disaster.service;

import com.disaster.ai.AIEngineService;
import com.disaster.dto.EnhancedPredictionDTO;
import com.disaster.dto.PredictionDTO;
import com.disaster.dto.ai.AIAnalysisRequest;
import com.disaster.dto.ai.AIAnalysisResponse;
import org.springframework.stereotype.Service;

/**
 * Thin adapter preserving the legacy prediction contract while delegating all
 * computation to the clean-architecture AI engine. Results are now deterministic
 * and include explainable confidence and score breakdowns.
 */
@Service
public class AIPredictionService implements PredictionEngine {

    private final AIEngineService engine;

    public AIPredictionService(AIEngineService engine) {
        this.engine = engine;
    }

    @Override
    public PredictionDTO predict(String disasterType, String severity, String location,
                                 double latitude, double longitude) {
        AIAnalysisRequest request = new AIAnalysisRequest();
        request.setDisasterType(disasterType);
        request.setSeverity(severity);
        request.setLocation(location);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        return map(engine.analyze(request));
    }

    @Override
    public EnhancedPredictionDTO predict(EnhancedPredictionDTO input) {
        AIAnalysisRequest request = new AIAnalysisRequest();
        request.setDisasterType(input.getDisasterType());
        request.setSeverity(input.getSeverity());
        request.setLocation(input.getLocation());
        request.setLatitude(input.getLatitude() != null ? input.getLatitude() : 0.0);
        request.setLongitude(input.getLongitude() != null ? input.getLongitude() : 0.0);
        return mapEnhanced(engine.analyze(request));
    }

    private PredictionDTO map(AIAnalysisResponse a) {
        PredictionDTO dto = new PredictionDTO();
        dto.setDisasterType(a.getDisasterType());
        dto.setSeverity(a.getSeverity());
        dto.setLocation(a.getLocation());
        dto.setLatitude(a.getLatitude());
        dto.setLongitude(a.getLongitude());
        dto.setEstimatedDamageLevel(a.getDamage().getDamageLevel());
        dto.setPredictedAffectedPopulation(a.getDamage().getAffectedPopulation());
        dto.setEstimatedEconomicLoss(a.getDamage().getEconomicLossINR());
        dto.setEstimatedRecoveryTime(friendlyDuration(a.getRecoveryTime().getValue()));
        dto.setConfidenceScore(a.getConfidence().getOverall());
        dto.setPriorityScore(a.getPriority().getScore());
        dto.setRiskScore(a.getRisk().getScore());
        dto.setEstimatedResponseTime(a.getResponseTime().getValue());
        dto.setRecommendation(String.join(" ", a.getRecommendations()));
        dto.setConfidenceBreakdown(a.getConfidence());
        dto.setPriorityFactors(a.getPriority().getFactors());
        dto.setRiskFactors(a.getRisk().getFactors());
        return dto;
    }

    private EnhancedPredictionDTO mapEnhanced(AIAnalysisResponse a) {
        EnhancedPredictionDTO dto = new EnhancedPredictionDTO();
        dto.setDisasterType(a.getDisasterType());
        dto.setSeverity(a.getSeverity());
        dto.setLocation(a.getLocation());
        dto.setLatitude(a.getLatitude());
        dto.setLongitude(a.getLongitude());
        dto.setEstimatedDamageLevel(a.getDamage().getDamageLevel());
        dto.setPredictedAffectedPopulation(a.getDamage().getAffectedPopulation());
        dto.setEstimatedEconomicLoss(a.getDamage().getEconomicLossINR());
        dto.setEstimatedRecoveryTime(a.getRecoveryTime().getValue());
        dto.setPriorityScore(a.getPriority().getScore());
        dto.setRiskScore(a.getRisk().getScore());
        dto.setEstimatedResponseTime(a.getResponseTime().getValue());
        dto.setConfidencePercentage(a.getConfidence().getOverall());
        dto.setRecommendation(String.join(" ", a.getRecommendations()));
        dto.setConfidenceBreakdown(a.getConfidence());
        dto.setPriorityFactors(a.getPriority().getFactors());
        dto.setRiskFactors(a.getRisk().getFactors());
        return dto;
    }

    private String friendlyDuration(double days) {
        if (days < 30) return "< 1 month";
        if (days < 90) return "1-3 months";
        if (days < 180) return "3-6 months";
        return "6+ months";
    }
}
