package com.disaster.dto.ai;

/**
 * A single explainable factor contributing to a priority / risk score.
 */
public class ScoreFactorDTO {

    private String name;
    private String description;
    private double weight;
    private double value;
    private double contribution;

    public ScoreFactorDTO() {}

    public ScoreFactorDTO(String name, String description, double weight, double value, double contribution) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.value = value;
        this.contribution = contribution;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public double getContribution() { return contribution; }
    public void setContribution(double contribution) { this.contribution = contribution; }
}
