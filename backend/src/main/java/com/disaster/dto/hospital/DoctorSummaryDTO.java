package com.disaster.dto.hospital;

/**
 * Aggregate medical staffing metrics across all registered hospitals.
 * Total headcount is estimated from available doctors using an assumed
 * availability factor when on/off-duty rosters are not tracked live.
 */
public class DoctorSummaryDTO {

    private int totalDoctors;
    private int availableDoctors;
    private int onDuty;
    private int offDuty;
    private double availabilityPercent;

    public DoctorSummaryDTO() {}

    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
    public int getAvailableDoctors() { return availableDoctors; }
    public void setAvailableDoctors(int availableDoctors) { this.availableDoctors = availableDoctors; }
    public int getOnDuty() { return onDuty; }
    public void setOnDuty(int onDuty) { this.onDuty = onDuty; }
    public int getOffDuty() { return offDuty; }
    public void setOffDuty(int offDuty) { this.offDuty = offDuty; }
    public double getAvailabilityPercent() { return availabilityPercent; }
    public void setAvailabilityPercent(double availabilityPercent) { this.availabilityPercent = availabilityPercent; }
}
