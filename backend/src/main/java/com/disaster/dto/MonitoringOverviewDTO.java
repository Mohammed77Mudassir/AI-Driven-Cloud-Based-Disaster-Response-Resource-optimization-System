package com.disaster.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated real-time snapshot for the monitoring dashboard.
 * Delivered by the REST overview endpoint and refreshed over WebSocket
 * via the LOCATION_SNAPSHOT event.
 */
public class MonitoringOverviewDTO {
    private List<LocationDTO> locations = new ArrayList<>();
    private int disasters;
    private int activeDisasters;
    private int drones;
    private int dronesInMission;
    private int hospitals;
    private int shelters;
    private int volunteers;
    private int volunteersAvailable;
    private int resources;
    private int teams;
    private int teamsAvailable;
    private int vehicles;
    private String updatedAt;

    public MonitoringOverviewDTO() {}

    public List<LocationDTO> getLocations() { return locations; }
    public void setLocations(List<LocationDTO> locations) { this.locations = locations; }
    public int getDisasters() { return disasters; }
    public void setDisasters(int disasters) { this.disasters = disasters; }
    public int getActiveDisasters() { return activeDisasters; }
    public void setActiveDisasters(int activeDisasters) { this.activeDisasters = activeDisasters; }
    public int getDrones() { return drones; }
    public void setDrones(int drones) { this.drones = drones; }
    public int getDronesInMission() { return dronesInMission; }
    public void setDronesInMission(int dronesInMission) { this.dronesInMission = dronesInMission; }
    public int getHospitals() { return hospitals; }
    public void setHospitals(int hospitals) { this.hospitals = hospitals; }
    public int getShelters() { return shelters; }
    public void setShelters(int shelters) { this.shelters = shelters; }
    public int getVolunteers() { return volunteers; }
    public void setVolunteers(int volunteers) { this.volunteers = volunteers; }
    public int getVolunteersAvailable() { return volunteersAvailable; }
    public void setVolunteersAvailable(int volunteersAvailable) { this.volunteersAvailable = volunteersAvailable; }
    public int getResources() { return resources; }
    public void setResources(int resources) { this.resources = resources; }
    public int getTeams() { return teams; }
    public void setTeams(int teams) { this.teams = teams; }
    public int getTeamsAvailable() { return teamsAvailable; }
    public void setTeamsAvailable(int teamsAvailable) { this.teamsAvailable = teamsAvailable; }
    public int getVehicles() { return vehicles; }
    public void setVehicles(int vehicles) { this.vehicles = vehicles; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
