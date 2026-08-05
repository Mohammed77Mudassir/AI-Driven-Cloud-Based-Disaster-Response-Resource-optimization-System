package com.disaster.dto.command;

/**
 * Calculated workload profile for a rescue team (LOW / MEDIUM / HIGH /
 * CRITICAL) based on active missions, shift hours, team size and resource
 * utilization.
 */
public class TeamWorkloadDTO {

    private Long teamId;
    private String teamName;
    private int activeMissions;
    private double shiftHours;
    private int teamSize;
    private double resourceUsagePercent;
    private double score;
    private String level;

    public TeamWorkloadDTO() {}

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getActiveMissions() { return activeMissions; }
    public void setActiveMissions(int activeMissions) { this.activeMissions = activeMissions; }
    public double getShiftHours() { return shiftHours; }
    public void setShiftHours(double shiftHours) { this.shiftHours = shiftHours; }
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
    public double getResourceUsagePercent() { return resourceUsagePercent; }
    public void setResourceUsagePercent(double resourceUsagePercent) { this.resourceUsagePercent = resourceUsagePercent; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
