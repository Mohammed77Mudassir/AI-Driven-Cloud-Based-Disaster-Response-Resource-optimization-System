package com.disaster.dto.command;

/**
 * One bucket of the monthly response-time trend series.
 */
public class MonthlyTrendDTO {

    private String month;
    private double averageResponseMinutes;
    private long missionsCompleted;

    public MonthlyTrendDTO() {}

    public MonthlyTrendDTO(String month, double averageResponseMinutes, long missionsCompleted) {
        this.month = month;
        this.averageResponseMinutes = averageResponseMinutes;
        this.missionsCompleted = missionsCompleted;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public double getAverageResponseMinutes() { return averageResponseMinutes; }
    public void setAverageResponseMinutes(double averageResponseMinutes) { this.averageResponseMinutes = averageResponseMinutes; }
    public long getMissionsCompleted() { return missionsCompleted; }
    public void setMissionsCompleted(long missionsCompleted) { this.missionsCompleted = missionsCompleted; }
}
