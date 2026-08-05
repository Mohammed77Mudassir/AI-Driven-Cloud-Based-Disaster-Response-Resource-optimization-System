package com.disaster.dto.ai;

/**
 * Time estimate with a plausible range, expressed in the given unit. The range
 * shrinks as input confidence rises and widens when inputs are sparse.
 */
public class TimeEstimateDTO {

    private String unit;
    private double value;
    private double min;
    private double max;
    private String label;
    private String note;

    /** Confidence of the estimate (0-100), inherited from the analysis confidence. */
    private double confidence;

    /** Short sentence describing what drove the estimate. */
    private String basis;

    public TimeEstimateDTO() {}

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }
}
