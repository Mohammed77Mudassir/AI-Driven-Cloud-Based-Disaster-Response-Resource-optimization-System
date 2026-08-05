package com.disaster.dto.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Response-time analytics: averages, extremes, monthly trend and mission
 * completion rate.
 */
public class ResponseTimeAnalyticsDTO {

    private double averageResponseMinutes;
    private double fastestResponseMinutes;
    private double slowestResponseMinutes;
    private double averageMissionDurationMinutes;
    private double completionRate;
    private List<MonthlyTrendDTO> monthlyTrend = new ArrayList<>();

    public ResponseTimeAnalyticsDTO() {}

    public double getAverageResponseMinutes() { return averageResponseMinutes; }
    public void setAverageResponseMinutes(double averageResponseMinutes) { this.averageResponseMinutes = averageResponseMinutes; }
    public double getFastestResponseMinutes() { return fastestResponseMinutes; }
    public void setFastestResponseMinutes(double fastestResponseMinutes) { this.fastestResponseMinutes = fastestResponseMinutes; }
    public double getSlowestResponseMinutes() { return slowestResponseMinutes; }
    public void setSlowestResponseMinutes(double slowestResponseMinutes) { this.slowestResponseMinutes = slowestResponseMinutes; }
    public double getAverageMissionDurationMinutes() { return averageMissionDurationMinutes; }
    public void setAverageMissionDurationMinutes(double averageMissionDurationMinutes) { this.averageMissionDurationMinutes = averageMissionDurationMinutes; }
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    public List<MonthlyTrendDTO> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<MonthlyTrendDTO> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
}
