package com.disaster.ai.core;

import com.disaster.ai.model.AnalysisInput;
import com.disaster.dto.ai.ConfidenceDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic confidence engine. Confidence is derived from three independent,
 * auditable components rather than random noise:
 * <ul>
 *   <li>input completeness (fields supplied)</li>
 *   <li>model coverage (type + severity supported by the model)</li>
 *   <li>data quality (geographic context available)</li>
 * </ul>
 * plus a fixed historical-basis factor that represents how well historical
 * baselines calibrate the estimate. Overall confidence is clamped to
 * {@code [5, 100]} and penalized for unknown disaster types and missing
 * geographic context.
 */
@Component
public class ConfidenceEngine {

    private static final Set<String> KNOWN_TYPES = Set.of(
            "Flood", "Earthquake", "Cyclone", "Wildfire", "Tsunami", "Landslide", "Drought", "Epidemic"
    );

    private static final Map<String, Double> TYPE_CALIBRATION = Map.ofEntries(
            Map.entry("Flood", 0.92),
            Map.entry("Earthquake", 0.88),
            Map.entry("Cyclone", 0.9),
            Map.entry("Wildfire", 0.86),
            Map.entry("Tsunami", 0.94),
            Map.entry("Landslide", 0.84),
            Map.entry("Drought", 0.9),
            Map.entry("Epidemic", 0.82)
    );

    private static final Set<String> SEVERITIES = Set.of("Low", "Medium", "High", "Critical");

    private static final String METHODOLOGY =
            "Overall = 40% input completeness + 30% model coverage + 20% data quality + 10% historical basis, "
                    + "minus penalties for unknown disaster types and missing geographic context.";

    public ConfidenceDTO evaluate(AnalysisInput input) {
        double completeness = inputCompleteness(input);
        double coverage = modelCoverage(input);
        double dataQuality = dataQuality(input);

        double historicalBasis = 0.85
                - 0.06 * Math.max(0, 1 - coverage)          // unknown types weaken the historical basis
                + (input.hasCoordinates() ? 0.05 : -0.1);   // coordinates anchor historical analogies

        historicalBasis = Math.max(30, Math.min(98, historicalBasis));

        double overall = round(
                0.4 * completeness
                + 0.3 * coverage
                + 0.2 * dataQuality
                + 0.1 * historicalBasis);

        boolean unknownType = input.getDisasterType() == null || !KNOWN_TYPES.contains(input.getDisasterType());
        boolean noGeography = !input.hasCoordinates() && !input.hasLocation();

        if (unknownType) overall -= 10;       // unknown types reduce calibration
        if (noGeography) overall -= 10;       // no coordinates AND no location reduces spatial accuracy
        overall = round(clamp(overall, 5, 100));

        double uncertainty = round(clamp(100 - overall, 5, 95));

        ConfidenceDTO dto = new ConfidenceDTO();
        dto.setOverall(overall);
        dto.setInputCompleteness(round(completeness));
        dto.setModelCoverage(round(coverage));
        dto.setDataQuality(round(dataQuality));
        dto.setHistoricalBasis(round(historicalBasis));
        dto.setBasis(describe(overall));
        dto.setMethodology(METHODOLOGY);
        dto.setLimitations(limitations(input, unknownType, noGeography));
        dto.setUncertaintyPercent(uncertainty);
        return dto;
    }

    private double inputCompleteness(AnalysisInput input) {
        double score = 0;
        if (input.getDisasterType() != null && !input.getDisasterType().isBlank()) score += 25;
        if (input.getSeverity() != null && !input.getSeverity().isBlank()) score += 20;
        if (input.hasLocation()) score += 15;
        if (input.hasCoordinates()) score += 20;
        if (input.hasPopulation()) score += 10;
        score += input.getInfrastructureFactor() > 0 ? 10 : 5;
        return score;
    }

    private double modelCoverage(AnalysisInput input) {
        boolean knownType = input.getDisasterType() != null && KNOWN_TYPES.contains(input.getDisasterType());
        boolean knownSeverity = input.getSeverity() != null && SEVERITIES.contains(input.getSeverity());
        double calibration = knownType
                ? TYPE_CALIBRATION.getOrDefault(input.getDisasterType(), 0.85)
                : 0.4;
        return calibration * 100 * (knownSeverity ? 1.0 : 0.75);
    }

    private double dataQuality(AnalysisInput input) {
        double base = input.hasCoordinates() ? 85 : 60;
        if (input.hasLocation()) base += 10;
        if (input.hasPopulation()) base += 5;
        return Math.min(100, base);
    }

    private List<String> limitations(AnalysisInput input, boolean unknownType, boolean noGeography) {
        List<String> limitations = new ArrayList<>();
        limitations.add("Heuristic baselines are not ML-calibrated; treat estimates as planning support rather than ground truth.");
        limitations.add("Damage and timing estimates rely on reported severity and infrastructure assumptions.");
        if (unknownType) limitations.add("Unknown disaster type reduces model coverage and historical calibration.");
        if (noGeography) limitations.add("Missing geographic context reduces the spatial accuracy of the estimates.");
        if (!input.hasPopulation()) limitations.add("Population was not supplied and is derived from historical baselines.");
        return limitations;
    }

    private String describe(double overall) {
        if (overall >= 85) return "High confidence - well-specified scenario with geographic context.";
        if (overall >= 70) return "Moderate confidence - usable estimate, consider supplying coordinates and population.";
        if (overall >= 50) return "Low confidence - incomplete inputs; estimates should be treated as rough.";
        return "Very low confidence - inputs insufficient for reliable estimation.";
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
