package com.disaster.dto.command;

/**
 * Equipment inventory posture for a single rescue team.
 */
public class EquipmentReadinessDTO {

    private Long teamId;
    private String teamName;
    private int assigned;
    private int available;
    private int deployed;
    private int maintenance;
    private int missing;
    private double readinessPercent;

    public EquipmentReadinessDTO() {}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getAssigned() { return assigned; }
    public void setAssigned(int assigned) { this.assigned = assigned; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public int getDeployed() { return deployed; }
    public void setDeployed(int deployed) { this.deployed = deployed; }
    public int getMaintenance() { return maintenance; }
    public void setMaintenance(int maintenance) { this.maintenance = maintenance; }
    public int getMissing() { return missing; }
    public void setMissing(int missing) { this.missing = missing; }
    public double getReadinessPercent() { return readinessPercent; }
    public void setReadinessPercent(double readinessPercent) { this.readinessPercent = readinessPercent; }
}
