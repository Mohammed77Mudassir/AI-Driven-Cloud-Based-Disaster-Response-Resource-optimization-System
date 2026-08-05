package com.disaster.dto.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Priority score (0-100) with an explainable factor breakdown.
 */
public class PriorityScoreDTO {

    private int score;
    private String label;
    private List<ScoreFactorDTO> factors = new ArrayList<>();

    public PriorityScoreDTO() {}

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public List<ScoreFactorDTO> getFactors() { return factors; }
    public void setFactors(List<ScoreFactorDTO> factors) { this.factors = factors; }
}
