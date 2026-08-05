package com.disaster.ai.core;

import com.disaster.ai.model.AnalysisInput;
import com.disaster.dto.ai.TimeEstimateDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Deterministic time estimation engine for response (hours) and recovery (days).
 * Estimates are anchored to historical baselines per disaster type and adjusted
 * by severity, population scale, infrastructure resilience and local readiness.
 */
@Component
public class TimeEstimationEngine {

    private static final Map<String, Double> RESPONSE_BASE_HOURS = Map.of(
            "Flood", 2.0, "Earthquake", 1.0, "Cyclone", 4.0,
            "Wildfire", 3.0, "Tsunami", 1.0, "Landslide", 3.0,
            "Drought", 24.0, "Epidemic", 12.0
    );

    private static final Map<String, Double> RECOVERY_BASE_DAYS = Map.of(
            "Flood", 45.0, "Earthquake", 120.0, "Cyclone", 60.0,
            "Wildfire", 75.0, "Tsunami", 160.0, "Landslide", 40.0,
            "Drought", 210.0, "Epidemic", 120.0
    );

    private static final Map<String, Integer> SEVERITY_ORDER = Map.of(
            "Low", 1, "Medium", 2, "High", 3, "Critical", 4
    );

    /** Response estimate given local readiness (0..1) and overall input confidence (0..1). */
    public TimeEstimateDTO response(AnalysisInput input, double readiness, double baseConfidence) {
        double base = RESPONSE_BASE_HOURS.getOrDefault(input.getDisasterType(), 4.0);
        int sev = severity(input.getSeverity());
        double urgencyFactor = 1.0 + 0.4 * (sev - 1);          // more severe -> faster response
        double accessFactor = 1.6 - 0.6 * clamp(readiness, 0, 1); // resources nearby speed response

        double value = base / urgencyFactor * accessFactor;
        double baseMargin = 0.18 + 0.05 * Math.max(0, 1 - readiness);
        double margin = baseMargin * (1.6 - 0.6 * clamp(baseConfidence, 0, 1));
        double confidence = round(clamp(baseConfidence, 0, 1) * 100);

        TimeEstimateDTO dto = new TimeEstimateDTO();
        dto.setUnit("hours");
        dto.setValue(round(value));
        dto.setMin(round(value * (1 - margin)));
        dto.setMax(round(value * (1 + margin)));
        dto.setLabel("Estimated time to first response");
        dto.setConfidence(confidence);
        dto.setBasis(String.format(
                "Driven by the %.0fh baseline for %s, %s severity, local readiness of %.0f%% and input confidence of %.0f%%.",
                base, input.getDisasterType(), input.getSeverity(), clamp(readiness, 0, 1) * 100, confidence));
        dto.setNote(String.format(
                "Based on a %.0fh baseline for %s, adjusted for %s severity%s.",
                base, input.getDisasterType(), input.getSeverity(),
                readiness > 0.6 ? " and strong local resource readiness" : ""));
        return dto;
    }

    /** Recovery estimate adjusted for population scale and infrastructure resilience. */
    public TimeEstimateDTO recovery(AnalysisInput input, long affectedPopulation, double baseConfidence) {
        double base = RECOVERY_BASE_DAYS.getOrDefault(input.getDisasterType(), 60.0);
        int sev = severity(input.getSeverity());
        double severityFactor = 1.0 + 0.55 * (sev - 1);
        double populationFactor = 1.0 + clamp(Math.log10(affectedPopulation + 1) / 6.5, 0, 1);
        double infraFactor = 1.6 - 0.6 * clamp(input.getInfrastructureFactor(), 0, 1);

        double value = base * severityFactor * (0.55 + 0.45 * populationFactor) * infraFactor;

        double margin = 0.225 * (1.6 - 0.6 * clamp(baseConfidence, 0, 1));
        double confidence = round(clamp(baseConfidence, 0, 1) * 100);

        TimeEstimateDTO dto = new TimeEstimateDTO();
        dto.setUnit("days");
        dto.setValue(round(value));
        dto.setMin(round(value * (1 - margin)));
        dto.setMax(round(value * (1 + margin)));
        dto.setLabel("Estimated recovery time");
        dto.setConfidence(confidence);
        dto.setBasis(String.format(
                "Driven by the %.0fd baseline for %s, %s severity, ~%d affected people, infrastructure resilience of %.0f%% and input confidence of %.0f%%.",
                base, input.getDisasterType(), input.getSeverity(), affectedPopulation,
                clamp(input.getInfrastructureFactor(), 0, 1) * 100, confidence));
        dto.setNote(String.format(
                "Recovery horizon for ~%d affected people based on %s historical baselines.",
                affectedPopulation, input.getDisasterType()));
        return dto;
    }

    private int severity(String severity) {
        return SEVERITY_ORDER.getOrDefault(severity == null ? "" : severity, 1);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
