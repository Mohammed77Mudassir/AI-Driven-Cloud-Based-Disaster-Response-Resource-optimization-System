package com.disaster.dto.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual lifecycle timeline for a mission: Mission Created -> Team Assigned ->
 * Vehicle Assigned -> Equipment Loaded -> Rescue Started -> Mission Completed.
 */
public class MissionTimelineDTO {

    private Long missionId;
    private String missionCode;
    private String title;
    private String status;
    private List<MilestoneDTO> milestones = new ArrayList<>();

    public MissionTimelineDTO() {}

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<MilestoneDTO> getMilestones() { return milestones; }
    public void setMilestones(List<MilestoneDTO> milestones) { this.milestones = milestones; }
}
