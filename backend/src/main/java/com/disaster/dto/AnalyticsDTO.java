package com.disaster.dto;

import java.util.Map;

public class AnalyticsDTO {
    private long totalDisasters;
    private long totalHospitals;
    private long totalShelters;
    private long totalVolunteers;
    private long totalResources;
    private Map<String, Long> disastersByMonth;
    private Map<String, Long> disastersByType;
    private Map<String, Long> disastersBySeverity;
    private Map<String, Long> disastersByStatus;
    private double averageResponseTimeHours;
    private double averageResolutionTimeHours;
    private long resourceUtilizationPercent;
    private long volunteerActivityPercent;
    private long hospitalOccupancyPercent;
    private long shelterOccupancyPercent;

    public AnalyticsDTO() {}
    
    public long getTotalDisasters() { return totalDisasters; }
    public void setTotalDisasters(long totalDisasters) { this.totalDisasters = totalDisasters; }
    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }
    public long getTotalShelters() { return totalShelters; }
    public void setTotalShelters(long totalShelters) { this.totalShelters = totalShelters; }
    public long getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(long totalVolunteers) { this.totalVolunteers = totalVolunteers; }
    public long getTotalResources() { return totalResources; }
    public void setTotalResources(long totalResources) { this.totalResources = totalResources; }
    public Map<String, Long> getDisastersByMonth() { return disastersByMonth; }
    public void setDisastersByMonth(Map<String, Long> disastersByMonth) { this.disastersByMonth = disastersByMonth; }
    public Map<String, Long> getDisastersByType() { return disastersByType; }
    public void setDisastersByType(Map<String, Long> disastersByType) { this.disastersByType = disastersByType; }
    public Map<String, Long> getDisastersBySeverity() { return disastersBySeverity; }
    public void setDisastersBySeverity(Map<String, Long> disastersBySeverity) { this.disastersBySeverity = disastersBySeverity; }
    public Map<String, Long> getDisastersByStatus() { return disastersByStatus; }
    public void setDisastersByStatus(Map<String, Long> disastersByStatus) { this.disastersByStatus = disastersByStatus; }
    public double getAverageResponseTimeHours() { return averageResponseTimeHours; }
    public void setAverageResponseTimeHours(double averageResponseTimeHours) { this.averageResponseTimeHours = averageResponseTimeHours; }
    public double getAverageResolutionTimeHours() { return averageResolutionTimeHours; }
    public void setAverageResolutionTimeHours(double averageResolutionTimeHours) { this.averageResolutionTimeHours = averageResolutionTimeHours; }
    public long getResourceUtilizationPercent() { return resourceUtilizationPercent; }
    public void setResourceUtilizationPercent(long resourceUtilizationPercent) { this.resourceUtilizationPercent = resourceUtilizationPercent; }
    public long getVolunteerActivityPercent() { return volunteerActivityPercent; }
    public void setVolunteerActivityPercent(long volunteerActivityPercent) { this.volunteerActivityPercent = volunteerActivityPercent; }
    public long getHospitalOccupancyPercent() { return hospitalOccupancyPercent; }
    public void setHospitalOccupancyPercent(long hospitalOccupancyPercent) { this.hospitalOccupancyPercent = hospitalOccupancyPercent; }
    public long getShelterOccupancyPercent() { return shelterOccupancyPercent; }
    public void setShelterOccupancyPercent(long shelterOccupancyPercent) { this.shelterOccupancyPercent = shelterOccupancyPercent; }
}
