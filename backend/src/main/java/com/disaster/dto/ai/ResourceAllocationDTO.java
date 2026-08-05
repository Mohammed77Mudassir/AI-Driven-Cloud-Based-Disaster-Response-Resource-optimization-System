package com.disaster.dto.ai;

/**
 * A single resource allocation line: required vs available with the nearest
 * deployable assets, the resulting status and operational guidance.
 */
public class ResourceAllocationDTO {

    private String resourceType;
    private int required;
    private int available;
    private int deficit;
    private String status;
    private String nearestAssets;

    /** Percentage of the requirement covered by live inventory (0-100). */
    private int coveragePercent;

    /** Operational guidance for the resulting status. */
    private String recommendedAction;

    /** Number of available inventory items of this type. */
    private int sourcesCount;

    /** Average distance in km to the nearest available assets (0 when none). */
    private double averageDistanceKm;

    public ResourceAllocationDTO() {}

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public int getRequired() { return required; }
    public void setRequired(int required) { this.required = required; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public int getDeficit() { return deficit; }
    public void setDeficit(int deficit) { this.deficit = deficit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNearestAssets() { return nearestAssets; }
    public void setNearestAssets(String nearestAssets) { this.nearestAssets = nearestAssets; }
    public int getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(int coveragePercent) { this.coveragePercent = coveragePercent; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public int getSourcesCount() { return sourcesCount; }
    public void setSourcesCount(int sourcesCount) { this.sourcesCount = sourcesCount; }
    public double getAverageDistanceKm() { return averageDistanceKm; }
    public void setAverageDistanceKm(double averageDistanceKm) { this.averageDistanceKm = averageDistanceKm; }
}
