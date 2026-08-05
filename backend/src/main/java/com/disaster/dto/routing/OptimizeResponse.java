package com.disaster.dto.routing;

import com.disaster.dto.RouteRequest;

import java.util.List;

/**
 * Result of multi-destination (TSP) optimization: the suggested visiting order
 * for a rescue team together with the distance/time savings compared with the
 * original order.
 */
public class OptimizeResponse {
    /** Labels describing each stop, e.g. "Start", "Hospital", "Shelter", "Destination". */
    private List<String> orderLabels;
    /** Ordered stops (excluding the start point). */
    private List<RouteRequest.Point> optimizedOrder;
    private double optimizedDistanceKm;
    private double optimizedTimeMinutes;
    private double originalDistanceKm;
    private double savingsKm;
    private double savingsPercent;
    private String algorithm;

    public OptimizeResponse() {}

    public List<String> getOrderLabels() { return orderLabels; }
    public void setOrderLabels(List<String> orderLabels) { this.orderLabels = orderLabels; }
    public List<RouteRequest.Point> getOptimizedOrder() { return optimizedOrder; }
    public void setOptimizedOrder(List<RouteRequest.Point> optimizedOrder) { this.optimizedOrder = optimizedOrder; }
    public double getOptimizedDistanceKm() { return optimizedDistanceKm; }
    public void setOptimizedDistanceKm(double optimizedDistanceKm) { this.optimizedDistanceKm = optimizedDistanceKm; }
    public double getOptimizedTimeMinutes() { return optimizedTimeMinutes; }
    public void setOptimizedTimeMinutes(double optimizedTimeMinutes) { this.optimizedTimeMinutes = optimizedTimeMinutes; }
    public double getOriginalDistanceKm() { return originalDistanceKm; }
    public void setOriginalDistanceKm(double originalDistanceKm) { this.originalDistanceKm = originalDistanceKm; }
    public double getSavingsKm() { return savingsKm; }
    public void setSavingsKm(double savingsKm) { this.savingsKm = savingsKm; }
    public double getSavingsPercent() { return savingsPercent; }
    public void setSavingsPercent(double savingsPercent) { this.savingsPercent = savingsPercent; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
