package com.disaster.ai.model;

/**
 * Damage assessment result produced by a {@link PredictionModel}.
 */
public class DamageAssessment {

    private final String damageLevel;
    private final long affectedPopulation;
    private final double economicLossINR;
    private final long casualtiesEstimate;
    private final String infrastructureImpact;

    public DamageAssessment(String damageLevel, long affectedPopulation,
                            double economicLossINR, long casualtiesEstimate,
                            String infrastructureImpact) {
        this.damageLevel = damageLevel;
        this.affectedPopulation = affectedPopulation;
        this.economicLossINR = economicLossINR;
        this.casualtiesEstimate = casualtiesEstimate;
        this.infrastructureImpact = infrastructureImpact;
    }

    public String getDamageLevel() { return damageLevel; }
    public long getAffectedPopulation() { return affectedPopulation; }
    public double getEconomicLossINR() { return economicLossINR; }
    public long getCasualtiesEstimate() { return casualtiesEstimate; }
    public String getInfrastructureImpact() { return infrastructureImpact; }
}
