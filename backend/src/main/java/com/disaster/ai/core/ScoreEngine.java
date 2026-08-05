package com.disaster.ai.core;

import com.disaster.ai.model.AnalysisInput;
import com.disaster.ai.model.DamageAssessment;
import com.disaster.ai.model.HeuristicPredictionModel;
import com.disaster.dto.ai.PriorityScoreDTO;
import com.disaster.dto.ai.RiskScoreDTO;
import com.disaster.dto.ai.ScoreFactorDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic scoring engine producing explainable priority and risk scores
 * (0-100) with per-factor weights and contributions.
 */
@Component
public class ScoreEngine {

    private static final Map<String, Integer> SEVERITY_ORDER = Map.of(
            "Low", 1, "Medium", 2, "High", 3, "Critical", 4
    );

    private static final Set<String> TIME_SENSITIVE_TYPES = Set.of(
            "Tsunami", "Earthquake", "Flood", "Cyclone", "Wildfire"
    );

    public PriorityScoreDTO priority(AnalysisInput input, DamageAssessment damage) {
        return priority(input, damage, 0.5);
    }

    public PriorityScoreDTO priority(AnalysisInput input, DamageAssessment damage, double readiness) {
        int sev = severity(input.getSeverity());
        double severityValue = severityScore(sev);
        double populationValue = populationScore(input, damage.getAffectedPopulation());
        double damageValue = damageScore(damage.getDamageLevel());
        double urgencyValue = timeSensitive(input.getDisasterType()) ? 70.0 : 35.0;
        double fragilityValue = 100 - clamp(input.getInfrastructureFactor(), 0, 1) * 100;
        double readinessValue = (1 - clamp(readiness, 0, 1)) * 100;

        List<ScoreFactorDTO> factors = new ArrayList<>();
        factors.add(factor("Severity", "Reported severity of the event", 0.30, severityValue));
        factors.add(factor("Population exposure", "Estimated affected population", 0.20, populationValue));
        factors.add(factor("Damage impact", "Predicted physical damage level", 0.15, damageValue));
        factors.add(factor("Response urgency", "Time sensitivity of the hazard", 0.10, urgencyValue));
        factors.add(factor("Infrastructure fragility", "Weaker infrastructure raises priority", 0.10, fragilityValue));
        factors.add(factor("Local readiness", "Local response capacity; lower readiness raises priority", 0.15, readinessValue));

        double score = factors.stream().mapToDouble(ScoreFactorDTO::getContribution).sum();

        PriorityScoreDTO dto = new PriorityScoreDTO();
        dto.setScore(roundInt(score));
        dto.setLabel(label(dto.getScore()));
        dto.setFactors(factors);
        return dto;
    }

    public RiskScoreDTO risk(AnalysisInput input, DamageAssessment damage, double modelRisk) {
        int sev = severity(input.getSeverity());
        double hazardValue = modelRisk; // model-level likelihood + severity blend
        double exposureValue = populationScore(input, damage.getAffectedPopulation());
        double vulnerabilityValue = 100 - clamp(input.getInfrastructureFactor(), 0, 1) * 100;
        double copingValue = 100 - Math.max(20, 100 - vulnerabilityValue * 0.5);
        boolean weatherPresent = input.getWeatherAlert() != null && !input.getWeatherAlert().isBlank();
        double weatherValue = weatherPresent ? clamp(40.0 + 20.0 * (sev - 1), 0, 100) : 0.0;

        List<ScoreFactorDTO> factors = new ArrayList<>();
        factors.add(factor("Hazard likelihood", "Model risk from type + severity + weather", 0.30, hazardValue));
        factors.add(factor("Exposure", "Population and assets in the hazard zone", 0.25, exposureValue));
        factors.add(factor("Vulnerability", "Fragility of local infrastructure", 0.20, vulnerabilityValue));
        factors.add(factor("Coping capacity", "Inverse of readiness to absorb impact", 0.15, copingValue));
        factors.add(factor("Weather amplification",
                "Active weather alert amplifies downstream risk", 0.10, weatherValue));

        double score = clamp(factors.stream().mapToDouble(ScoreFactorDTO::getContribution).sum(), 0, 100);

        RiskScoreDTO dto = new RiskScoreDTO();
        dto.setScore(roundInt(score));
        dto.setLevel(riskLevel(dto.getScore()));
        dto.setFactors(factors);
        return dto;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ScoreFactorDTO factor(String name, String description, double weight, double value) {
        return new ScoreFactorDTO(name, description, weight, round(value), round(weight * value));
    }

    private int severity(String severity) {
        return SEVERITY_ORDER.getOrDefault(severity == null ? "" : severity, 1);
    }

    private double severityScore(int sev) {
        return 25.0 + 25.0 * (sev - 1);
    }

    private double populationScore(AnalysisInput input, long affectedPopulation) {
        if (input.hasPopulation()) {
            return clamp((Math.log10(input.getPopulation() + 1) / 6.0) * 100, 5, 100);
        }
        return clamp((Math.log10(affectedPopulation + 1) / 6.0) * 100, 5, 100);
    }

    private double damageScore(String damageLevel) {
        return switch (damageLevel == null ? "" : damageLevel) {
            case "Catastrophic" -> 100;
            case "Severe" -> 75;
            case "Moderate" -> 45;
            default -> 15;
        };
    }

    private boolean timeSensitive(String type) {
        return type != null && TIME_SENSITIVE_TYPES.contains(type);
    }

    private String label(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }

    private String riskLevel(int score) {
        if (score >= 80) return "EXTREME";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "ELEVATED";
        return "GUARDED";
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private int roundInt(double v) {
        return (int) Math.round(v);
    }
}
