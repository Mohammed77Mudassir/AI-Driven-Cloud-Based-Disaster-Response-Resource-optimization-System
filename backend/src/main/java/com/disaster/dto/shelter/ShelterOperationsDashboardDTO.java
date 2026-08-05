package com.disaster.dto.shelter;

import com.disaster.dto.command.KpiDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only payload for the Shelter Operations dashboard. Aggregation is
 * performed entirely in the service layer and reuses the existing
 * {@link KpiDTO} for the top-level key performance indicators.
 */
public class ShelterOperationsDashboardDTO {

    private LocalDateTime lastUpdated;
    private List<KpiDTO> kpis;
    private List<ShelterOperationsDTO> shelters;
    private List<ShelterAlertDTO> alerts;

    public ShelterOperationsDashboardDTO() {}

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<KpiDTO> getKpis() { return kpis; }
    public void setKpis(List<KpiDTO> kpis) { this.kpis = kpis; }
    public List<ShelterOperationsDTO> getShelters() { return shelters; }
    public void setShelters(List<ShelterOperationsDTO> shelters) { this.shelters = shelters; }
    public List<ShelterAlertDTO> getAlerts() { return alerts; }
    public void setAlerts(List<ShelterAlertDTO> alerts) { this.alerts = alerts; }
}
