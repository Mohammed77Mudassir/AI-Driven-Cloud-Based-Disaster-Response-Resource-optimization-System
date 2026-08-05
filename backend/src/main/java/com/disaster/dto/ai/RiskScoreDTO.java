package com.disaster.dto.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Risk prediction (0-100) with severity band and explainable factors.
 */
public class RiskScoreDTO {

    private int score;
    private String level;
    private List<ScoreFactorDTO> factors = new ArrayList<>();

    public RiskScoreDTO() {}

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public List<ScoreFactorDTO> getFactors() { return factors; }
    public void setFactors(List<ScoreFactorDTO> factors) { this.factors = factors; }
}
