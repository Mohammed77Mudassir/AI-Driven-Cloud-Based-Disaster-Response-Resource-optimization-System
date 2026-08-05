package com.disaster.dto.command;

/**
 * Single operational key-performance indicator rendered on the command center
 * dashboard (e.g. "Total Rescue Teams" or "Average Response Time").
 */
public class KpiDTO {

    private String key;
    private String label;
    private double value;
    private String unit;
    private String hint;

    public KpiDTO() {}

    public KpiDTO(String key, String label, double value, String unit, String hint) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.hint = hint;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
}
