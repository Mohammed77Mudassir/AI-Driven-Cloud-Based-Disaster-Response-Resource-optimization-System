package com.disaster.dto;

public class TeamAvailabilityDTO {
    private Long teamId;
    private String teamName;
    private String teamStatus;
    private int totalMembers;
    private int availableMembers;
    private int totalVehicles;
    private int availableVehicles;
    private int totalEquipmentItems;
    private int availableEquipmentItems;
    private boolean deployable;
    private Long activeMissionId;
    private String activeMissionCode;

    public TeamAvailabilityDTO() {}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamStatus() { return teamStatus; }
    public void setTeamStatus(String teamStatus) { this.teamStatus = teamStatus; }
    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
    public int getAvailableMembers() { return availableMembers; }
    public void setAvailableMembers(int availableMembers) { this.availableMembers = availableMembers; }
    public int getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(int totalVehicles) { this.totalVehicles = totalVehicles; }
    public int getAvailableVehicles() { return availableVehicles; }
    public void setAvailableVehicles(int availableVehicles) { this.availableVehicles = availableVehicles; }
    public int getTotalEquipmentItems() { return totalEquipmentItems; }
    public void setTotalEquipmentItems(int totalEquipmentItems) { this.totalEquipmentItems = totalEquipmentItems; }
    public int getAvailableEquipmentItems() { return availableEquipmentItems; }
    public void setAvailableEquipmentItems(int availableEquipmentItems) { this.availableEquipmentItems = availableEquipmentItems; }
    public boolean isDeployable() { return deployable; }
    public void setDeployable(boolean deployable) { this.deployable = deployable; }
    public Long getActiveMissionId() { return activeMissionId; }
    public void setActiveMissionId(Long activeMissionId) { this.activeMissionId = activeMissionId; }
    public String getActiveMissionCode() { return activeMissionCode; }
    public void setActiveMissionCode(String activeMissionCode) { this.activeMissionCode = activeMissionCode; }
}
