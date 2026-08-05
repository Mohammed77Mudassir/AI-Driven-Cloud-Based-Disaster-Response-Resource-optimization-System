package com.disaster.dto.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Explainable confidence breakdown. Every score is derived deterministically
 * from input quality and model coverage so results are reproducible.
 */
public class ConfidenceDTO {

    /** Overall confidence 0-100. */
    private double overall;

    /** Quality of the supplied inputs (fields filled, coordinates present). */
    private double inputCompleteness;

    /** How well the model covers the requested disaster type / severity. */
    private double modelCoverage;

    /** Quality of the underlying data sources (geography, inventory). */
    private double dataQuality;

    /** Whether historical baselines contributed to the estimate. */
    private double historicalBasis;

    /** Human readable basis, e.g. "High - coordinates and population supplied". */
    private String basis;

    /** Short sentence describing the weighted formula behind the overall score. */
    private String methodology;

    /** Known weaknesses of the estimate, e.g. unknown types, missing geography. */
    private List<String> limitations = new ArrayList<>();

    /** Implied uncertainty band (0-100), wider when confidence is lower. */
    private double uncertaintyPercent;

    public ConfidenceDTO() {}

    public double getOverall() { return overall; }
    public void setOverall(double overall) { this.overall = overall; }
    public double getInputCompleteness() { return inputCompleteness; }
    public void setInputCompleteness(double inputCompleteness) { this.inputCompleteness = inputCompleteness; }
    public double getModelCoverage() { return modelCoverage; }
    public void setModelCoverage(double modelCoverage) { this.modelCoverage = modelCoverage; }
    public double getDataQuality() { return dataQuality; }
    public void setDataQuality(double dataQuality) { this.dataQuality = dataQuality; }
    public double getHistoricalBasis() { return historicalBasis; }
    public void setHistoricalBasis(double historicalBasis) { this.historicalBasis = historicalBasis; }
    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }
    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }
    public List<String> getLimitations() { return limitations; }
    public void setLimitations(List<String> limitations) { this.limitations = limitations; }
    public double getUncertaintyPercent() { return uncertaintyPercent; }
    public void setUncertaintyPercent(double uncertaintyPercent) { this.uncertaintyPercent = uncertaintyPercent; }
}
