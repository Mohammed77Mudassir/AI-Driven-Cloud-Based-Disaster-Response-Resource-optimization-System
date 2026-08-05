package com.disaster.dto.hospital;

/**
 * Blood inventory for a single ABO/Rh blood group, used to summarize the
 * blood bank of a hospital and to highlight groups that are running low.
 */
public class BloodGroupDTO {

    private String group;
    private int units;
    private String status;

    public BloodGroupDTO() {}

    public BloodGroupDTO(String group, int units, String status) {
        this.group = group;
        this.units = units;
        this.status = status;
    }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
