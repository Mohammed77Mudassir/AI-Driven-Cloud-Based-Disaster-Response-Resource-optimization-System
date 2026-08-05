package com.disaster.service;

import com.disaster.dto.command.KpiDTO;
import com.disaster.dto.hospital.AmbulanceSummaryDTO;
import com.disaster.dto.hospital.BloodGroupDTO;
import com.disaster.dto.hospital.DoctorSummaryDTO;
import com.disaster.dto.hospital.HospitalOperationsDTO;
import com.disaster.dto.hospital.HospitalOperationsDashboardDTO;
import com.disaster.entity.Hospital;
import com.disaster.entity.Resource;
import com.disaster.enums.ResourceType;
import com.disaster.repository.HospitalRepository;
import com.disaster.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only aggregation service behind the Hospital Operations dashboard.
 * Derives operational metrics (occupancy, ICU pressure, emergency capacity,
 * estimated waiting time, hospital status, blood inventory estimates) from the
 * existing {@link Hospital} records and the ambulance fleet tracked by the
 * resource module.
 *
 * <p>Because live bed/ICU occupancy and blood unit telemetry are not part of
 * the current schema, several figures are produced with documented estimate
 * formulas (see the {@code constants} and each builder method). No schema,
 * entity or existing API is modified — this service only reads data.</p>
 */
@Service
public class HospitalOperationsService {

    // ------------------------------------------------------------------
    // Estimate constants (documented formulas used when live data is absent)
    // ------------------------------------------------------------------

    /** Assumed share of the medical staff that is currently available. */
    private static final double DOCTOR_AVAILABILITY_FACTOR = 0.75;

    /** Reference bed counts used to normalise emergency capacity scoring. */
    private static final double BED_REFERENCE = 40.0;
    private static final double ICU_REFERENCE = 12.0;
    private static final double DOCTOR_REFERENCE = 25.0;

    /** Base blood inventory estimate for a hospital that runs a blood bank. */
    private static final int BLOOD_BASE_UNITS = 140;
    private static final int BLOOD_LOW_THRESHOLD = 12;
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    /** Estimated waiting time model: base load penalty plus staffing penalty. */
    private static final int WAIT_BASE_MAX = 45;
    private static final int WAIT_DOCTOR_PENALTY = 30;

    private final HospitalRepository hospitalRepository;
    private final ResourceRepository resourceRepository;

    public HospitalOperationsService(HospitalRepository hospitalRepository,
                                     ResourceRepository resourceRepository) {
        this.hospitalRepository = hospitalRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public HospitalOperationsDashboardDTO getDashboard() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        AmbulanceSummaryDTO ambulance = buildAmbulanceSummary();

        List<HospitalOperationsDTO> operations = hospitals.stream()
                .map(h -> buildHospital(h, ambulance))
                .toList();

        HospitalOperationsDashboardDTO dto = new HospitalOperationsDashboardDTO();
        dto.setLastUpdated(LocalDateTime.now());
        dto.setHospitals(operations);
        dto.setDoctorSummary(buildDoctorSummary(operations));
        dto.setAmbulanceSummary(ambulance);
        dto.setKpis(buildKpis(operations, ambulance));
        BloodStock stock = buildBloodStock(operations);
        dto.setBloodGroups(stock.groups);
        dto.setTotalBloodUnits(stock.totalUnits);
        return dto;
    }

    // ------------------------------------------------------------------
    // Per-hospital metrics
    // ------------------------------------------------------------------

    private HospitalOperationsDTO buildHospital(Hospital h, AmbulanceSummaryDTO ambulance) {
        HospitalOperationsDTO dto = HospitalOperationsDTO.fromEntity(h);

        int availableBeds = h.getAvailableBeds();
        int icuAvailable = h.getIcuBeds();
        int doctorsAvailable = h.getDoctorsAvailable();

        // Bed utilization (occupied = total capacity minus what is available).
        int totalBeds = availableBeds + icuAvailable;
        int occupiedBeds = Math.max(0, totalBeds - availableBeds);
        double bedUtilization = percent(occupiedBeds, totalBeds);
        dto.setTotalBeds(totalBeds);
        dto.setOccupiedBeds(occupiedBeds);
        dto.setBedUtilizationPercent(bedUtilization);

        // ICU occupancy mirrors the overall occupancy estimate.
        double icuOccupancy = bedUtilization;
        int totalIcu = icuAvailable <= 0 ? 0
                : icuOccupancy >= 100 ? icuAvailable * 5
                : (int) Math.round(icuAvailable / (1 - icuOccupancy / 100.0));
        dto.setTotalIcuBeds(totalIcu);
        dto.setOccupiedIcuBeds(Math.max(0, totalIcu - icuAvailable));
        dto.setIcuOccupancyPercent(icuOccupancy);

        // Doctor availability (on/off duty derived from an assumed availability).
        int totalDoctors = doctorsAvailable > 0
                ? (int) Math.round(doctorsAvailable / DOCTOR_AVAILABILITY_FACTOR) : 0;
        dto.setTotalDoctors(totalDoctors);
        dto.setDoctorsOnDuty(doctorsAvailable);
        dto.setDoctorsOffDuty(Math.max(0, totalDoctors - doctorsAvailable));
        dto.setDoctorAvailabilityPercent(totalDoctors > 0
                ? round1(doctorsAvailable * 100.0 / totalDoctors) : 0.0);

        // Emergency capacity score from beds, ICU, doctors and fleet readiness.
        double bedScore = Math.min(1, availableBeds / BED_REFERENCE);
        double icuScore = Math.min(1, icuAvailable / ICU_REFERENCE);
        double doctorScore = Math.min(1, doctorsAvailable / DOCTOR_REFERENCE);
        double ambScore = ambulance.getTotal() > 0
                ? ambulance.getAvailable() / (double) ambulance.getTotal() : 0.0;
        double capacity = 100.0 * (0.35 * bedScore + 0.25 * icuScore + 0.25 * doctorScore + 0.15 * ambScore);
        dto.setEmergencyCapacityScore(round1(capacity));
        dto.setEmergencyCapacityLevel(level(capacity));

        // Patient load and estimated waiting time.
        dto.setPatientLoad((int) Math.round(bedUtilization));
        double loadRatio = bedUtilization / 100.0;
        double staffingRatio = 1 - dto.getDoctorAvailabilityPercent() / 100.0;
        dto.setEstimatedWaitingTimeMinutes((int) Math.round(loadRatio * WAIT_BASE_MAX + staffingRatio * WAIT_DOCTOR_PENALTY));

        // Hospital operational status.
        dto.setStatus(status(bedUtilization));

        // Blood bank estimate.
        if (h.isBloodBank()) {
            List<BloodGroupDTO> groups = buildBloodGroups(h.getId(), totalBeds);
            dto.setBloodUnits(groups.stream().mapToInt(BloodGroupDTO::getUnits).sum());
            dto.setBloodStockStatus(groups.stream().anyMatch(g -> "LOW".equals(g.getStatus())) ? "LOW" : "ADEQUATE");
            dto.setBloodGroups(groups);
        } else {
            dto.setBloodUnits(0);
            dto.setBloodStockStatus("N/A");
            dto.setBloodGroups(List.of());
        }
        return dto;
    }

    private List<BloodGroupDTO> buildBloodGroups(Long hospitalId, int hospitalLoad) {
        int totalUnits = BLOOD_BASE_UNITS + (int) ((hospitalId % 7) * 10);
        int base = totalUnits / BLOOD_GROUPS.length;
        List<BloodGroupDTO> groups = new ArrayList<>();
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            // Deterministic variation so shortages vary between hospitals.
            int units = Math.max(0, base + ((i * 3 + (int) (hospitalId % 5)) % 7) - 3);
            // Heavily loaded hospitals draw down stock faster.
            units = Math.max(0, units - Math.max(0, (hospitalLoad - 60) / 15));
            groups.add(new BloodGroupDTO(BLOOD_GROUPS[i], units, units < BLOOD_LOW_THRESHOLD ? "LOW" : "OK"));
        }
        return groups;
    }

    // ------------------------------------------------------------------
    // Fleet / staffing summaries
    // ------------------------------------------------------------------

    private AmbulanceSummaryDTO buildAmbulanceSummary() {
        AmbulanceSummaryDTO summary = new AmbulanceSummaryDTO();
        int available = 0, deployed = 0, maintenance = 0, total = 0;
        for (Resource r : resourceRepository.findByResourceType(ResourceType.AMBULANCE)) {
            available += Math.max(0, r.getQuantity());
            deployed += Math.max(0, r.getDeployedQuantity());
            maintenance += Math.max(0, r.getInMaintenanceQuantity());
            total += Math.max(r.getTotalQuantity(),
                    r.getQuantity() + r.getDeployedQuantity() + r.getInMaintenanceQuantity());
        }
        summary.setTotal(total);
        summary.setAvailable(available);
        summary.setOnEmergency(deployed);
        summary.setUnderMaintenance(maintenance);
        summary.setAvailabilityPercent(total > 0 ? round1(available * 100.0 / total) : 0.0);
        return summary;
    }

    private DoctorSummaryDTO buildDoctorSummary(List<HospitalOperationsDTO> operations) {
        DoctorSummaryDTO summary = new DoctorSummaryDTO();
        int total = 0, available = 0;
        for (HospitalOperationsDTO h : operations) {
            total += h.getTotalDoctors();
            available += h.getDoctorsAvailable();
        }
        summary.setTotalDoctors(total);
        summary.setAvailableDoctors(available);
        summary.setOnDuty(available);
        summary.setOffDuty(Math.max(0, total - available));
        summary.setAvailabilityPercent(total > 0 ? round1(available * 100.0 / total) : 0.0);
        return summary;
    }

    // ------------------------------------------------------------------
    // Blood inventory roll-up
    // ------------------------------------------------------------------

    private BloodStock buildBloodStock(List<HospitalOperationsDTO> operations) {
        int[] units = new int[BLOOD_GROUPS.length];
        int bloodBanks = 0;
        for (HospitalOperationsDTO h : operations) {
            if (!h.isBloodBankPresent()) continue;
            bloodBanks++;
            for (BloodGroupDTO g : h.getBloodGroups()) {
                int idx = indexOf(g.getGroup());
                if (idx >= 0) units[idx] += g.getUnits();
            }
        }
        int threshold = Math.max(BLOOD_LOW_THRESHOLD, BLOOD_LOW_THRESHOLD * bloodBanks);
        List<BloodGroupDTO> groups = new ArrayList<>();
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            groups.add(new BloodGroupDTO(BLOOD_GROUPS[i], units[i],
                    bloodBanks > 0 && units[i] < threshold ? "LOW" : "OK"));
        }
        return new BloodStock(groups, java.util.Arrays.stream(units).sum());
    }

    private static int indexOf(String group) {
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            if (BLOOD_GROUPS[i].equals(group)) return i;
        }
        return -1;
    }

    private record BloodStock(List<BloodGroupDTO> groups, int totalUnits) {}

    // ------------------------------------------------------------------
    // KPIs
    // ------------------------------------------------------------------

    private List<KpiDTO> buildKpis(List<HospitalOperationsDTO> operations, AmbulanceSummaryDTO ambulance) {
        int totalHospitals = operations.size();
        int availableBeds = operations.stream().mapToInt(HospitalOperationsDTO::getAvailableBeds).sum();
        int icuAvailable = operations.stream().mapToInt(HospitalOperationsDTO::getAvailableIcuBeds).sum();
        int doctorsAvailable = operations.stream().mapToInt(HospitalOperationsDTO::getDoctorsAvailable).sum();
        double avgCapacity = operations.isEmpty() ? 0.0
                : round1(operations.stream().mapToDouble(HospitalOperationsDTO::getEmergencyCapacityScore).average().orElse(0));
        double avgUtilization = operations.isEmpty() ? 0.0
                : round1(operations.stream().mapToDouble(HospitalOperationsDTO::getBedUtilizationPercent).average().orElse(0));
        BloodStock stock = buildBloodStock(operations);

        List<KpiDTO> kpis = new ArrayList<>();
        kpis.add(new KpiDTO("TOTAL_HOSPITALS", "Total Hospitals", totalHospitals, "", null));
        kpis.add(new KpiDTO("AVAILABLE_BEDS", "Total Available Beds", availableBeds, "", null));
        kpis.add(new KpiDTO("ICU_BEDS", "ICU Beds Available", icuAvailable, "", null));
        kpis.add(new KpiDTO("DOCTORS", "Doctors Available", doctorsAvailable, "", null));
        kpis.add(new KpiDTO("AMBULANCES", "Ambulances Available", ambulance.getAvailable(), "", null));
        kpis.add(new KpiDTO("BLOOD_UNITS", "Blood Units Available", stock.totalUnits, "units", null));
        kpis.add(new KpiDTO("EMERGENCY_CAPACITY", "Emergency Capacity", avgCapacity, "%", null));
        kpis.add(new KpiDTO("UTILIZATION", "Hospital Utilization", avgUtilization, "%", null));
        return kpis;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static double percent(int part, int total) {
        return total > 0 ? round1(part * 100.0 / total) : 0.0;
    }

    private static String level(double score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "MODERATE";
        return "CRITICAL";
    }

    private static String status(double utilization) {
        if (utilization < 50) return "READY";
        if (utilization < 70) return "BUSY";
        if (utilization <= 85) return "NEAR_CAPACITY";
        return "FULL";
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
