package com.disaster.dto.command;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level aggregate payload for the Rescue Team Command Center dashboard.
 * Built in a single request so the frontend can render the whole operations
 * view without fan-out API calls.
 */
public class CommandCenterDTO {

    private List<KpiDTO> kpis = new ArrayList<>();
    private List<ActiveMissionDTO> activeMissions = new ArrayList<>();
    private List<TeamCardDTO> teams = new ArrayList<>();
    private ResponseTimeAnalyticsDTO responseAnalytics = new ResponseTimeAnalyticsDTO();
    private List<EquipmentReadinessDTO> equipmentReadiness = new ArrayList<>();
    private List<VehicleCardDTO> vehicles = new ArrayList<>();
    private List<TeamWorkloadDTO> workloads = new ArrayList<>();
    private List<MissionTimelineDTO> missionTimelines = new ArrayList<>();
    private List<OperationalAlertDTO> alerts = new ArrayList<>();
    private LocalDateTime lastUpdated;

    public CommandCenterDTO() {}

    public List<KpiDTO> getKpis() { return kpis; }
    public void setKpis(List<KpiDTO> kpis) { this.kpis = kpis; }
    public List<ActiveMissionDTO> getActiveMissions() { return activeMissions; }
    public void setActiveMissions(List<ActiveMissionDTO> activeMissions) { this.activeMissions = activeMissions; }
    public List<TeamCardDTO> getTeams() { return teams; }
    public void setTeams(List<TeamCardDTO> teams) { this.teams = teams; }
    public ResponseTimeAnalyticsDTO getResponseAnalytics() { return responseAnalytics; }
    public void setResponseAnalytics(ResponseTimeAnalyticsDTO responseAnalytics) { this.responseAnalytics = responseAnalytics; }
    public List<EquipmentReadinessDTO> getEquipmentReadiness() { return equipmentReadiness; }
    public void setEquipmentReadiness(List<EquipmentReadinessDTO> equipmentReadiness) { this.equipmentReadiness = equipmentReadiness; }
    public List<VehicleCardDTO> getVehicles() { return vehicles; }
    public void setVehicles(List<VehicleCardDTO> vehicles) { this.vehicles = vehicles; }
    public List<TeamWorkloadDTO> getWorkloads() { return workloads; }
    public void setWorkloads(List<TeamWorkloadDTO> workloads) { this.workloads = workloads; }
    public List<MissionTimelineDTO> getMissionTimelines() { return missionTimelines; }
    public void setMissionTimelines(List<MissionTimelineDTO> missionTimelines) { this.missionTimelines = missionTimelines; }
    public List<OperationalAlertDTO> getAlerts() { return alerts; }
    public void setAlerts(List<OperationalAlertDTO> alerts) { this.alerts = alerts; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
