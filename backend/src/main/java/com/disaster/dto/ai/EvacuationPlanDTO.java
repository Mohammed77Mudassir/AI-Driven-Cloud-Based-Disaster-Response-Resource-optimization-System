package com.disaster.dto.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured evacuation recommendation: danger radius, recommended evacuation
 * window, priority zones, actionable instructions, routes and assembly capacity.
 */
public class EvacuationPlanDTO {

    private double dangerRadiusKm;
    private String evacuationWindow;
    private List<String> priorityZones = new ArrayList<>();
    private List<String> instructions = new ArrayList<>();
    private List<ShelterRecommendationDTO> assemblyPoints = new ArrayList<>();

    /** Estimated number of people needing evacuation. */
    private long atRiskPopulation;

    /** Total capacity of the recommended assembly points. */
    private int assemblyCapacity;

    /** 2-3 professional evacuation routes for the scenario. */
    private List<String> evacuationRoutes = new ArrayList<>();

    public EvacuationPlanDTO() {}

    public double getDangerRadiusKm() { return dangerRadiusKm; }
    public void setDangerRadiusKm(double dangerRadiusKm) { this.dangerRadiusKm = dangerRadiusKm; }
    public String getEvacuationWindow() { return evacuationWindow; }
    public void setEvacuationWindow(String evacuationWindow) { this.evacuationWindow = evacuationWindow; }
    public List<String> getPriorityZones() { return priorityZones; }
    public void setPriorityZones(List<String> priorityZones) { this.priorityZones = priorityZones; }
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    public List<ShelterRecommendationDTO> getAssemblyPoints() { return assemblyPoints; }
    public void setAssemblyPoints(List<ShelterRecommendationDTO> assemblyPoints) { this.assemblyPoints = assemblyPoints; }
    public long getAtRiskPopulation() { return atRiskPopulation; }
    public void setAtRiskPopulation(long atRiskPopulation) { this.atRiskPopulation = atRiskPopulation; }
    public int getAssemblyCapacity() { return assemblyCapacity; }
    public void setAssemblyCapacity(int assemblyCapacity) { this.assemblyCapacity = assemblyCapacity; }
    public List<String> getEvacuationRoutes() { return evacuationRoutes; }
    public void setEvacuationRoutes(List<String> evacuationRoutes) { this.evacuationRoutes = evacuationRoutes; }
}
