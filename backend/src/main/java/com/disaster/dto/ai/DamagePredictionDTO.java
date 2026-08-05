package com.disaster.dto.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Damage prediction result (damage level, population, economic loss, casualties,
 * infrastructure impact and the contributing hazard factors). Also exposes a
 * numeric damage percentage with a disruption band and expected secondary
 * hazards for planning.
 */
public class DamagePredictionDTO {

    private String damageLevel;
    private long affectedPopulation;
    private double economicLossINR;
    private long casualtiesEstimate;
    private String infrastructureImpact;
    private List<String> hazardDrivers = new ArrayList<>();

    /** Estimated fraction of the zone damaged, 0-100. */
    private double damagePercent;

    /** Disruption band: LOW / MODERATE / HIGH / SEVERE. */
    private String disruptionLevel;

    /** Likely secondary hazards cascading from the primary event. */
    private List<String> secondaryHazards = new ArrayList<>();

    public DamagePredictionDTO() {}

    public String getDamageLevel() { return damageLevel; }
    public void setDamageLevel(String damageLevel) { this.damageLevel = damageLevel; }
    public long getAffectedPopulation() { return affectedPopulation; }
    public void setAffectedPopulation(long affectedPopulation) { this.affectedPopulation = affectedPopulation; }
    public double getEconomicLossINR() { return economicLossINR; }
    public void setEconomicLossINR(double economicLossINR) { this.economicLossINR = economicLossINR; }
    public long getCasualtiesEstimate() { return casualtiesEstimate; }
    public void setCasualtiesEstimate(long casualtiesEstimate) { this.casualtiesEstimate = casualtiesEstimate; }
    public String getInfrastructureImpact() { return infrastructureImpact; }
    public void setInfrastructureImpact(String infrastructureImpact) { this.infrastructureImpact = infrastructureImpact; }
    public List<String> getHazardDrivers() { return hazardDrivers; }
    public void setHazardDrivers(List<String> hazardDrivers) { this.hazardDrivers = hazardDrivers; }
    public double getDamagePercent() { return damagePercent; }
    public void setDamagePercent(double damagePercent) { this.damagePercent = damagePercent; }
    public String getDisruptionLevel() { return disruptionLevel; }
    public void setDisruptionLevel(String disruptionLevel) { this.disruptionLevel = disruptionLevel; }
    public List<String> getSecondaryHazards() { return secondaryHazards; }
    public void setSecondaryHazards(List<String> secondaryHazards) { this.secondaryHazards = secondaryHazards; }
}
