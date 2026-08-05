package com.disaster.ai.model;

import com.disaster.dto.ai.ModelMetaDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Default offline prediction model. It is deterministic: given the same inputs it
 * always returns the same estimates (no {@code Math.random()}). Geographic
 * variance is derived from a stable hash of the coordinates so results differ
 * between locations but never fluctuate between calls.
 *
 * <p>To replace this with a real ML model, implement {@link PredictionModel}
 * and mark it {@code @Primary} (or activate a Spring profile) — controllers and
 * the orchestration layer do not need to change.
 */
@Component
public class HeuristicPredictionModel implements PredictionModel {

    public static final String VERSION = "1.1.0";

    private static final Map<String, Integer> SEVERITY_ORDER = Map.of(
            "Low", 1, "Medium", 2, "High", 3, "Critical", 4
    );

    private static final Map<String, String[]> DAMAGE_BY_TYPE = Map.of(
            "Flood", new String[]{"Minor", "Moderate", "Severe", "Catastrophic"},
            "Earthquake", new String[]{"Minor", "Moderate", "Severe", "Catastrophic"},
            "Cyclone", new String[]{"Minor", "Moderate", "Severe", "Severe"},
            "Wildfire", new String[]{"Minor", "Moderate", "Severe", "Severe"},
            "Tsunami", new String[]{"Moderate", "Severe", "Catastrophic", "Catastrophic"},
            "Landslide", new String[]{"Minor", "Moderate", "Moderate", "Severe"},
            "Drought", new String[]{"Minor", "Minor", "Moderate", "Severe"},
            "Epidemic", new String[]{"Minor", "Moderate", "Severe", "Catastrophic"}
    );

    private static final Map<String, Long> BASE_POPULATION = Map.of(
            "Flood", 50000L, "Earthquake", 120000L, "Cyclone", 80000L,
            "Wildfire", 15000L, "Tsunami", 220000L, "Landslide", 12000L,
            "Drought", 400000L, "Epidemic", 900000L
    );

    private static final Map<String, Double> PER_CAPITA_LOSS_INR = Map.of(
            "Flood", 45000d, "Earthquake", 90000d, "Cyclone", 60000d,
            "Wildfire", 70000d, "Tsunami", 120000d, "Landslide", 55000d,
            "Drought", 18000d, "Epidemic", 35000d
    );

    private static final Map<String, Double> CASUALTY_RATE = Map.of(
            "Flood", 0.0012, "Earthquake", 0.006, "Cyclone", 0.002,
            "Wildfire", 0.0008, "Tsunami", 0.012, "Landslide", 0.004,
            "Drought", 0.0002, "Epidemic", 0.008
    );

    private static final Map<String, Integer> INFRASTRUCTURE_IMPACT = Map.of(
            "Flood", 80, "Earthquake", 90, "Cyclone", 65,
            "Wildfire", 45, "Tsunami", 85, "Landslide", 55,
            "Drought", 20, "Epidemic", 30
    );

    @Override
    public ModelMetaDTO metadata() {
        return new ModelMetaDTO(
                "HeuristicPredictionModel",
                VERSION,
                "RULE_BASED",
                true,
                "Deterministic offline rule-based model calibrated on historical disaster baselines. "
                        + "Replaceable by an ML model implementing PredictionModel."
        );
    }

    @Override
    public DamageAssessment assess(AnalysisInput input) {
        int sev = severity(input.getSeverity());
        double geoNoise = input.hasCoordinates()
                ? pseudoRandom(input.getLatitude(), input.getLongitude(), 11)
                : 0.5;
        double infr = clamp(input.getInfrastructureFactor(), 0, 1);

        String[] levels = DAMAGE_BY_TYPE.getOrDefault(input.getDisasterType(),
                new String[]{"Minor", "Moderate", "Severe", "Severe"});
        String damageLevel = levels[sev - 1];

        long base = BASE_POPULATION.getOrDefault(input.getDisasterType(), 50000L);
        long population = input.hasPopulation()
                ? input.getPopulation()
                : Math.round(base * sev * (0.7 + 0.6 * geoNoise));

        double severityLossFactor = 0.6 + 0.35 * (sev - 1);
        double infrLossFactor = 1.35 - 0.5 * infr;
        double perCapita = PER_CAPITA_LOSS_INR.getOrDefault(input.getDisasterType(), 40000d);
        double economicLoss = population * perCapita * severityLossFactor * infrLossFactor
                * (0.85 + 0.3 * geoNoise);

        double casualtyRate = CASUALTY_RATE.getOrDefault(input.getDisasterType(), 0.002) * sev
                * (1.25 - 0.45 * infr);
        long casualties = Math.round(population * casualtyRate);

        String infraImpact = switch (severityForLabel(damageLevel)) {
            case 4 -> "Critical infrastructure extensively damaged (power, water, roads, comms).";
            case 3 -> "Major infrastructure disruption; partial service restoration expected within weeks.";
            case 2 -> "Moderate infrastructure strain; localized outages likely.";
            default -> "Minor infrastructure impact; services largely intact.";
        };

        return new DamageAssessment(damageLevel, population, round(economicLoss), casualties, infraImpact);
    }

    @Override
    public double riskScore(AnalysisInput input) {
        int sev = severity(input.getSeverity());
        double exposure = 0.4 + 0.6 * severityForLabel(severityLabel(sev)) / 4.0;
        double populationFactor = input.hasPopulation()
                ? clamp(Math.log10(input.getPopulation() + 1) / 6.0, 0, 1)
                : 0.45;
        double vulnerability = 1.0 - clamp(input.getInfrastructureFactor(), 0, 1);
        double hazardLikelihood = 0.55 + 0.45 * (sev - 1) / 3.0;
        double weatherBoost = input.getWeatherAlert() != null && !input.getWeatherAlert().isBlank() ? 0.12 : 0.0;

        double risk = (0.35 * severityScore(sev)
                + 0.25 * (exposure * 100)
                + 0.2 * (vulnerability * 100)
                + 0.2 * (hazardLikelihood * 100))
                * (1 + weatherBoost);

        return round(clamp(risk, 0, 100));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private int severity(String severity) {
        return SEVERITY_ORDER.getOrDefault(severity == null ? "" : severity, 1);
    }

    private double severityScore(int sev) {
        return 25.0 + 25.0 * (sev - 1);
    }

    private int severityForLabel(String label) {
        return switch (label) {
            case "Catastrophic" -> 4;
            case "Severe" -> 3;
            case "Moderate" -> 2;
            default -> 1;
        };
    }

    private String severityLabel(int sev) {
        return switch (sev) {
            case 4 -> "Catastrophic";
            case 3 -> "Severe";
            case 2 -> "Moderate";
            default -> "Minor";
        };
    }

    /** Stable deterministic hash in [0,1) from coordinates so results are reproducible. */
    private double pseudoRandom(double lat, double lng, int salt) {
        long h = Double.doubleToLongBits(lat);
        long h2 = Double.doubleToLongBits(lng);
        long seed = h * 73856093L ^ h2 * 19349663L ^ (long) salt * 83492791L;
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        return Math.abs((double) seed / Long.MAX_VALUE);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static List<String> supportedTypes() {
        return List.copyOf(DAMAGE_BY_TYPE.keySet());
    }
}
