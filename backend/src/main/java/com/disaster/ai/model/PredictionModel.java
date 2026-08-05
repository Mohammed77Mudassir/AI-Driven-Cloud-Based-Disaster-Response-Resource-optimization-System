package com.disaster.ai.model;

import com.disaster.dto.ai.ModelMetaDTO;

/**
 * Contract every prediction model (heuristic today, machine learning tomorrow)
 * must implement. Keeping AI services behind this interface is what makes the
 * engine replaceable without touching controllers or the orchestration layer.
 */
public interface PredictionModel {

    ModelMetaDTO metadata();

    /**
     * Core damage prediction. Implementations must be deterministic and offline.
     */
    DamageAssessment assess(AnalysisInput input);

    /**
     * Normalized 0..100 risk score for the scenario.
     */
    double riskScore(AnalysisInput input);
}
