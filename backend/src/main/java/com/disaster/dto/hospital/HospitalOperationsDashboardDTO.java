package com.disaster.dto.hospital;

import com.disaster.dto.command.KpiDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only payload for the Hospital Operations dashboard. Aggregation is
 * performed entirely in the service layer and reuses the existing
 * {@link KpiDTO} for the top-level key performance indicators.
 */
public class HospitalOperationsDashboardDTO {

    private LocalDateTime lastUpdated;
    private List<KpiDTO> kpis;
    private List<HospitalOperationsDTO> hospitals;
    private DoctorSummaryDTO doctorSummary;
    private AmbulanceSummaryDTO ambulanceSummary;
    private List<BloodGroupDTO> bloodGroups;
    private int totalBloodUnits;

    public HospitalOperationsDashboardDTO() {}

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<KpiDTO> getKpis() { return kpis; }
    public void setKpis(List<KpiDTO> kpis) { this.kpis = kpis; }
    public List<HospitalOperationsDTO> getHospitals() { return hospitals; }
    public void setHospitals(List<HospitalOperationsDTO> hospitals) { this.hospitals = hospitals; }
    public DoctorSummaryDTO getDoctorSummary() { return doctorSummary; }
    public void setDoctorSummary(DoctorSummaryDTO doctorSummary) { this.doctorSummary = doctorSummary; }
    public AmbulanceSummaryDTO getAmbulanceSummary() { return ambulanceSummary; }
    public void setAmbulanceSummary(AmbulanceSummaryDTO ambulanceSummary) { this.ambulanceSummary = ambulanceSummary; }
    public List<BloodGroupDTO> getBloodGroups() { return bloodGroups; }
    public void setBloodGroups(List<BloodGroupDTO> bloodGroups) { this.bloodGroups = bloodGroups; }
    public int getTotalBloodUnits() { return totalBloodUnits; }
    public void setTotalBloodUnits(int totalBloodUnits) { this.totalBloodUnits = totalBloodUnits; }
}
