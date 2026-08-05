package com.disaster.dto.hospital;

/**
 * Fleet-level summary of ambulance resources aggregated from the resource
 * management module. Availability is expressed both in absolute counts and as
 * a percentage of the total fleet.
 */
public class AmbulanceSummaryDTO {

    private int total;
    private int available;
    private int onEmergency;
    private int underMaintenance;
    private double availabilityPercent;

    public AmbulanceSummaryDTO() {}

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public int getOnEmergency() { return onEmergency; }
    public void setOnEmergency(int onEmergency) { this.onEmergency = onEmergency; }
    public int getUnderMaintenance() { return underMaintenance; }
    public void setUnderMaintenance(int underMaintenance) { this.underMaintenance = underMaintenance; }
    public double getAvailabilityPercent() { return availabilityPercent; }
    public void setAvailabilityPercent(double availabilityPercent) { this.availabilityPercent = availabilityPercent; }
}
