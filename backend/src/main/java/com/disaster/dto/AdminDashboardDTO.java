package com.disaster.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregated Emergency Operations Center payload. Combines counts, statistics,
 * health, live connectivity and recent activity feeds in a single response so
 * the admin dashboard can render with one round-trip.
 */
public class AdminDashboardDTO {
    // --- Entity totals ---
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long totalDisasters;
    private long totalRescueTeams;
    private long totalHospitals;
    private long totalShelters;
    private long totalVolunteers;
    private long totalResources;
    private long totalDrones;
    private long pendingDisasters;
    private long activeMissions;
    private long resolvedDisasters;

    // --- Live connectivity ---
    private long connectedUsers;
    private boolean websocketHealthy;

    // --- User statistics ---
    private Map<String, Long> usersByRole;

    // --- Disaster statistics ---
    private Map<String, Long> disastersByStatus;
    private Map<String, Long> disastersBySeverity;
    private Map<String, Long> disastersByType;
    private Map<String, Long> disastersByMonth;

    // --- Resource utilization ---
    private long resourcesAvailable;
    private long resourcesDeployed;
    private long resourcesInMaintenance;
    private long resourceUtilizationPercent;

    // --- System ---
    private String systemUptime;
    private String systemHealthStatus;

    // --- Feeds ---
    private List<AuditLogDTO> recentActivities;
    private List<NotificationDTO> recentNotifications;

    public AdminDashboardDTO() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    public long getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
    public long getTotalDisasters() { return totalDisasters; }
    public void setTotalDisasters(long totalDisasters) { this.totalDisasters = totalDisasters; }
    public long getTotalRescueTeams() { return totalRescueTeams; }
    public void setTotalRescueTeams(long totalRescueTeams) { this.totalRescueTeams = totalRescueTeams; }
    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }
    public long getTotalShelters() { return totalShelters; }
    public void setTotalShelters(long totalShelters) { this.totalShelters = totalShelters; }
    public long getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(long totalVolunteers) { this.totalVolunteers = totalVolunteers; }
    public long getTotalResources() { return totalResources; }
    public void setTotalResources(long totalResources) { this.totalResources = totalResources; }
    public long getTotalDrones() { return totalDrones; }
    public void setTotalDrones(long totalDrones) { this.totalDrones = totalDrones; }
    public long getPendingDisasters() { return pendingDisasters; }
    public void setPendingDisasters(long pendingDisasters) { this.pendingDisasters = pendingDisasters; }
    public long getActiveMissions() { return activeMissions; }
    public void setActiveMissions(long activeMissions) { this.activeMissions = activeMissions; }
    public long getResolvedDisasters() { return resolvedDisasters; }
    public void setResolvedDisasters(long resolvedDisasters) { this.resolvedDisasters = resolvedDisasters; }

    public long getConnectedUsers() { return connectedUsers; }
    public void setConnectedUsers(long connectedUsers) { this.connectedUsers = connectedUsers; }
    public boolean isWebsocketHealthy() { return websocketHealthy; }
    public void setWebsocketHealthy(boolean websocketHealthy) { this.websocketHealthy = websocketHealthy; }

    public Map<String, Long> getUsersByRole() { return usersByRole; }
    public void setUsersByRole(Map<String, Long> usersByRole) { this.usersByRole = usersByRole; }

    public Map<String, Long> getDisastersByStatus() { return disastersByStatus; }
    public void setDisastersByStatus(Map<String, Long> disastersByStatus) { this.disastersByStatus = disastersByStatus; }
    public Map<String, Long> getDisastersBySeverity() { return disastersBySeverity; }
    public void setDisastersBySeverity(Map<String, Long> disastersBySeverity) { this.disastersBySeverity = disastersBySeverity; }
    public Map<String, Long> getDisastersByType() { return disastersByType; }
    public void setDisastersByType(Map<String, Long> disastersByType) { this.disastersByType = disastersByType; }
    public Map<String, Long> getDisastersByMonth() { return disastersByMonth; }
    public void setDisastersByMonth(Map<String, Long> disastersByMonth) { this.disastersByMonth = disastersByMonth; }

    public long getResourcesAvailable() { return resourcesAvailable; }
    public void setResourcesAvailable(long resourcesAvailable) { this.resourcesAvailable = resourcesAvailable; }
    public long getResourcesDeployed() { return resourcesDeployed; }
    public void setResourcesDeployed(long resourcesDeployed) { this.resourcesDeployed = resourcesDeployed; }
    public long getResourcesInMaintenance() { return resourcesInMaintenance; }
    public void setResourcesInMaintenance(long resourcesInMaintenance) { this.resourcesInMaintenance = resourcesInMaintenance; }
    public long getResourceUtilizationPercent() { return resourceUtilizationPercent; }
    public void setResourceUtilizationPercent(long resourceUtilizationPercent) { this.resourceUtilizationPercent = resourceUtilizationPercent; }

    public String getSystemUptime() { return systemUptime; }
    public void setSystemUptime(String systemUptime) { this.systemUptime = systemUptime; }
    public String getSystemHealthStatus() { return systemHealthStatus; }
    public void setSystemHealthStatus(String systemHealthStatus) { this.systemHealthStatus = systemHealthStatus; }

    public List<AuditLogDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<AuditLogDTO> recentActivities) { this.recentActivities = recentActivities; }
    public List<NotificationDTO> getRecentNotifications() { return recentNotifications; }
    public void setRecentNotifications(List<NotificationDTO> recentNotifications) { this.recentNotifications = recentNotifications; }
}
