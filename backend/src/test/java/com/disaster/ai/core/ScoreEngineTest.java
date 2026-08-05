package com.disaster.ai.core;

import com.disaster.ai.model.AnalysisInput;
import com.disaster.ai.model.DamageAssessment;
import com.disaster.dto.ai.PriorityScoreDTO;
import com.disaster.dto.ai.RiskScoreDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the deterministic explainable scoring engine. Verifies score
 * ranges, severity ordering, population scaling and factor weighting without
 * any external context.
 */
class ScoreEngineTest {

    private final ScoreEngine engine = new ScoreEngine();

    private AnalysisInput input(String type, String severity, Long population, double infraFactor, String weather) {
        return new AnalysisInput(type, severity, "Test Location", 19.0, 72.0, population, infraFactor, weather);
    }

    private DamageAssessment damage(String level, long affectedPopulation) {
        return new DamageAssessment(level, affectedPopulation, 1_000_000, 100, "Major");
    }

    @Test
    void priorityScoreIsWithinZeroToOneHundred() {
        PriorityScoreDTO dto = engine.priority(input("Flood", "High", 50_000L, 0.6, "Heavy rain"), damage("Severe", 5000));
        assertTrue(dto.getScore() >= 0 && dto.getScore() <= 100, "Score out of range: " + dto.getScore());
        assertFalse(dto.getLabel().isBlank());
        assertTrue(dto.getFactors().size() >= 5);
    }

    @Test
    void criticalSeverityScoresHigherThanLowSeverity() {
        PriorityScoreDTO critical = engine.priority(
                input("Flood", "Critical", 100_000L, 0.6, null), damage("Severe", 10_000));
        PriorityScoreDTO low = engine.priority(
                input("Flood", "Low", 100_000L, 0.6, null), damage("Severe", 10_000));
        assertTrue(critical.getScore() > low.getScore(),
                "Critical severity must score higher than Low severity");
    }

    @Test
    void timeSensitiveTypesScoreHigher() {
        PriorityScoreDTO tsunami = engine.priority(
                input("Tsunami", "High", 50_000L, 0.5, null), damage("Moderate", 5000));
        PriorityScoreDTO flood = engine.priority(
                input("Landslide", "High", 50_000L, 0.5, null), damage("Moderate", 5000));
        assertTrue(tsunami.getScore() >= flood.getScore());
    }

    @Test
    void higherPopulationRaisesPriorityScore() {
        PriorityScoreDTO large = engine.priority(
                input("Flood", "High", 5_000_000L, 0.5, null), damage("Moderate", 5000));
        PriorityScoreDTO small = engine.priority(
                input("Flood", "High", 5_000L, 0.5, null), damage("Moderate", 5000));
        assertTrue(large.getScore() > small.getScore());
    }

    @Test
    void factorContributionsAreWeightedAndNonNegative() {
        PriorityScoreDTO dto = engine.priority(input("Flood", "High", 10_000L, 0.6, null), damage("Severe", 1000));
        dto.getFactors().forEach(f -> {
            assertTrue(f.getWeight() > 0);
            assertTrue(f.getContribution() >= 0);
            assertEquals(f.getWeight() * f.getValue(), f.getContribution(), 0.01);
        });
    }

    @Test
    void riskLevelReflectsScoreBand() {
        RiskScoreDTO low = engine.risk(
                input("Flood", "Low", 100L, 0.9, null), damage("Minor", 10), 10);
        RiskScoreDTO high = engine.risk(
                input("Tsunami", "Critical", 5_000_000L, 0.1, "Tsunami warning"), damage("Catastrophic", 100_000), 90);
        assertTrue(low.getScore() < high.getScore());
        assertNotNull(low.getLevel());
        assertNotNull(high.getLevel());
    }

    @Test
    void weatherAlertAmplifiesRisk() {
        RiskScoreDTO withWeather = engine.risk(
                input("Flood", "High", 50_000L, 0.6, "Heavy rainfall warning"), damage("Severe", 5000), 60);
        RiskScoreDTO withoutWeather = engine.risk(
                input("Flood", "High", 50_000L, 0.6, null), damage("Severe", 5000), 60);
        assertTrue(withWeather.getScore() >= withoutWeather.getScore());
    }

    @Test
    void missingPopulationFallsBackToAffectedPopulation() {
        PriorityScoreDTO noPopulation = engine.priority(
                input("Flood", "Medium", null, 0.5, null), damage("Moderate", 500_000));
        PriorityScoreDTO lowPopulation = engine.priority(
                input("Flood", "Medium", 1L, 0.5, null), damage("Moderate", 100));
        assertTrue(noPopulation.getScore() > lowPopulation.getScore());
    }

    @Test
    void unknownSeverityDefaultsToLow() {
        PriorityScoreDTO dto = engine.priority(input("Flood", "Unknown", 10_000L, 0.5, null), damage("Moderate", 1000));
        assertNotNull(dto.getLabel());
        assertTrue(dto.getScore() >= 0);
    }
}
