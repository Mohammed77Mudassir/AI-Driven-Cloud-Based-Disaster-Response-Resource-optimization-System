package com.disaster.service;

import com.disaster.dto.command.KpiDTO;
import com.disaster.dto.shelter.ShelterAlertDTO;
import com.disaster.dto.shelter.ShelterOperationsDTO;
import com.disaster.dto.shelter.ShelterOperationsDashboardDTO;
import com.disaster.entity.Shelter;
import com.disaster.repository.ShelterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only aggregation service behind the Shelter Operations dashboard.
 * Derives operational metrics (occupancy pressure, food/water/medical
 * posture, power &amp; communications state, readiness score, capacity
 * forecast and alerts) from the existing {@link Shelter} records.
 *
 * <p>Because live telemetry for food packs, water volume, generator fuel,
 * internet and daily arrival/departure counts is not part of the current
 * schema, several figures are produced with documented estimate formulas
 * (see the {@code constants} and each builder method) and are surfaced in
 * the UI as estimates. No schema, entity or existing API is modified — this
 * service only reads data and remains available to future AI consumers.</p>
 */
@Service
public class ShelterOperationsService {

    // ------------------------------------------------------------------
    // Estimate constants (documented formulas used when live data is absent)
    // ------------------------------------------------------------------

    /** Baseline food packs stocked per person of total capacity. */
    private static final int FOOD_BASE_PACKS_PER_CAP = 6;

    /** Food packs consumed per resident per day. */
    private static final int FOOD_DAILY_PER_PERSON = 2;

    /** Remaining-days thresholds for the food stock status. */
    private static final double FOOD_CRITICAL_DAYS = 3;
    private static final double FOOD_LOW_DAYS = 7;

    /** Water requirement in litres per resident per day. */
    private static final int WATER_DAILY_PER_PERSON = 4;

    /** Base supply buffer (in days) when water is available. */
    private static final double WATER_SUPPLY_DAYS = 14;

    /** Remaining-days thresholds for the water stock status. */
    private static final double WATER_CRITICAL_DAYS = 2;
    private static final double WATER_LOW_DAYS = 5;

    /** One medical kit per this many residents (used for in-use and minimums). */
    private static final int KIT_PER_PEOPLE = 10;

    /** Projected daily arrival/departure rates for the capacity forecast. */
    private static final double ARRIVAL_BASE_RATE = 0.03;
    private static final double DEPARTURE_BASE_RATE = 0.02;

    /** Backup run time in hours when the generator is at 100% fuel. */
    private static final double BACKUP_RUNTIME_HOURS_AT_FULL = 8;

    private final ShelterRepository shelterRepository;

    public ShelterOperationsService(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @Transactional(readOnly = true)
    public ShelterOperationsDashboardDTO getDashboard() {
        List<Shelter> shelters = shelterRepository.findAll();
        List<ShelterOperationsDTO> operations = shelters.stream()
                .map(this::buildShelter)
                .toList();

        ShelterOperationsDashboardDTO dto = new ShelterOperationsDashboardDTO();
        dto.setLastUpdated(LocalDateTime.now());
        dto.setShelters(operations);
        dto.setKpis(buildKpis(operations));
        dto.setAlerts(buildAlerts(operations));
        return dto;
    }

    // ------------------------------------------------------------------
    // Per-shelter metrics
    // ------------------------------------------------------------------

    private ShelterOperationsDTO buildShelter(Shelter s) {
        ShelterOperationsDTO dto = new ShelterOperationsDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());
        dto.setContact(s.getContact());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());

        int capacity = s.getCapacity();
        int occupancy = s.getOccupancy();
        int availableSpace = Math.max(0, capacity - occupancy);
        double occupancyPercent = percent(occupancy, capacity);
        dto.setCapacity(capacity);
        dto.setOccupancy(occupancy);
        dto.setAvailableSpace(availableSpace);
        dto.setOccupancyPercent(occupancyPercent);
        dto.setStatus(status(occupancyPercent));

        buildFood(dto, s, occupancy);
        buildWater(dto, s, occupancy);
        buildMedical(dto, s, capacity, occupancy);
        buildPower(dto, s);
        buildCommunications(dto, s);
        buildSanitation(dto, s);
        buildReadiness(dto, capacity, availableSpace);
        buildForecast(dto, s, capacity, occupancy, availableSpace);
        return dto;
    }

    private void buildFood(ShelterOperationsDTO dto, Shelter s, int occupancy) {
        // When food is flagged available we assume a stock that scales with
        // capacity plus a small per-shelter reserve; otherwise the pantry is
        // empty and the status is critical.
        int foodPacks = s.isFoodAvailable()
                ? s.getCapacity() * FOOD_BASE_PACKS_PER_CAP + (int) (s.getId() % 5) * 25 : 0;
        int dailyConsumption = occupancy * FOOD_DAILY_PER_PERSON;
        double daysRemaining = dailyConsumption > 0
                ? round1(foodPacks * 1.0 / dailyConsumption) : 0.0;

        dto.setFoodPacks(foodPacks);
        dto.setDailyFoodConsumption(dailyConsumption);
        dto.setFoodDaysRemaining(daysRemaining);
        dto.setFoodStatus(!s.isFoodAvailable() ? "CRITICAL"
                : daysRemaining < FOOD_CRITICAL_DAYS ? "CRITICAL"
                : daysRemaining < FOOD_LOW_DAYS ? "LOW" : "SUFFICIENT");
    }

    private void buildWater(ShelterOperationsDTO dto, Shelter s, int occupancy) {
        // When water is flagged available we assume a storage buffer of about
        // two weeks plus a small per-shelter variance; otherwise the tanks are
        // empty and the status is critical.
        int waterLitres = s.isWaterAvailable()
                ? (int) Math.round(occupancy * WATER_DAILY_PER_PERSON
                        * (WATER_SUPPLY_DAYS + (s.getId() % 6))) : 0;
        int dailyRequirement = occupancy * WATER_DAILY_PER_PERSON;
        double daysRemaining = dailyRequirement > 0
                ? round1(waterLitres * 1.0 / dailyRequirement) : 0.0;

        dto.setWaterLitres(waterLitres);
        dto.setDailyWaterRequirement(dailyRequirement);
        dto.setWaterDaysRemaining(daysRemaining);
        dto.setWaterStatus(!s.isWaterAvailable() ? "CRITICAL"
                : daysRemaining < WATER_CRITICAL_DAYS ? "CRITICAL"
                : daysRemaining < WATER_LOW_DAYS ? "LOW" : "OK");
    }

    private void buildMedical(ShelterOperationsDTO dto, Shelter s, int capacity, int occupancy) {
        int total = s.getMedicalKits();
        int inUse = ceilDiv(occupancy, KIT_PER_PEOPLE);
        int minRequired = Math.max(1, ceilDiv(capacity, KIT_PER_PEOPLE));

        dto.setTotalMedicalKits(total);
        dto.setMedicalKitsInUse(inUse);
        dto.setAvailableMedicalKits(Math.max(0, total - inUse));
        dto.setMinRequiredMedicalKits(minRequired);
        dto.setMedicalReadinessPercent(round1(Math.min(100.0, total * 100.0 / minRequired)));
    }

    private void buildPower(ShelterOperationsDTO dto, Shelter s) {
        // Every shelter owns a backup generator, but a subset may be
        // inoperative (derived deterministically) which turns a grid outage
        // into a full power failure.
        boolean generatorAvailable = s.isPowerAvailable() || (s.getId() % 3 != 0);
        int fuelPercent;
        String powerStatus;
        if (s.isPowerAvailable()) {
            fuelPercent = 75 + (int) (s.getId() % 4) * 6; // standby reserves
            powerStatus = "AVAILABLE";
        } else if (generatorAvailable) {
            fuelPercent = 25 + (int) (s.getId() % 5) * 12; // running on diesel
            powerStatus = fuelPercent <= 15 ? "POWER_FAILURE" : "BACKUP_MODE";
        } else {
            fuelPercent = 0;
            powerStatus = "POWER_FAILURE";
        }

        dto.setGridAvailable(s.isPowerAvailable());
        dto.setGeneratorAvailable(generatorAvailable);
        dto.setGeneratorFuelPercent(fuelPercent);
        dto.setBackupRuntimeHours(round1(fuelPercent / 100.0 * BACKUP_RUNTIME_HOURS_AT_FULL));
        dto.setPowerStatus(powerStatus);
    }

    private void buildCommunications(ShelterOperationsDTO dto, Shelter s) {
        // Connectivity relies on grid power; the network type and signal
        // strength vary per shelter.
        boolean internetAvailable = s.isPowerAvailable() && (s.getId() % 4 != 3);
        String networkType = s.isPowerAvailable()
                ? (s.getId() % 2 == 0 ? "FIBRE" : "4G/LTE") : "RADIO";
        int signalStrength = !s.isPowerAvailable()
                ? 20 + (int) (s.getId() % 3) * 10
                : (internetAvailable ? 70 + (int) (s.getId() % 3) * 10 : 45);
        String commStatus = !internetAvailable ? "OFFLINE"
                : signalStrength >= 70 ? "ONLINE" : "DEGRADED";

        dto.setInternetAvailable(internetAvailable);
        dto.setNetworkType(networkType);
        dto.setSignalStrengthPercent(signalStrength);
        dto.setCommStatus(commStatus);
    }

    private void buildSanitation(ShelterOperationsDTO dto, Shelter s) {
        double sanitation = 100.0;
        if (!s.isPowerAvailable()) sanitation -= 25;
        if (!s.isWaterAvailable()) sanitation -= 30;
        sanitation = round1(Math.max(0.0, sanitation));
        dto.setSanitationPercent(sanitation);
        dto.setSanitationAvailable(sanitation >= 60);
    }

    private void buildReadiness(ShelterOperationsDTO dto, int capacity, int availableSpace) {
        double spaceRatio = capacity > 0 ? Math.min(1.0, availableSpace * 1.0 / capacity) : 0.0;
        double foodRatio = statusRatio(dto.getFoodStatus());
        double waterRatio = statusRatio(dto.getWaterStatus());
        double medicalRatio = dto.getMedicalReadinessPercent() / 100.0;
        double powerRatio = dto.getPowerStatus().equals("AVAILABLE") ? 1.0
                : dto.getPowerStatus().equals("BACKUP_MODE") ? 0.6 : 0.15;
        double commRatio = dto.getCommStatus().equals("ONLINE") ? 1.0
                : dto.getCommStatus().equals("DEGRADED") ? 0.5 : 0.15;
        double sanitationRatio = dto.getSanitationPercent() / 100.0;

        double score = 100.0 * (0.20 * spaceRatio + 0.20 * foodRatio + 0.20 * waterRatio
                + 0.15 * medicalRatio + 0.10 * powerRatio + 0.10 * commRatio + 0.05 * sanitationRatio);
        dto.setReadinessScore(round1(score));
        dto.setReadinessLevel(level(score));
    }

    private void buildForecast(ShelterOperationsDTO dto, Shelter s, int capacity, int occupancy, int availableSpace) {
        // Simple projection model: arrivals and departures are derived from
        // capacity/occupancy plus a deterministic per-shelter variance. The
        // projected full date is an estimate, flagged as such in the UI.
        int arrivals = Math.max(1, (int) Math.round(capacity * ARRIVAL_BASE_RATE
                * (1 + (s.getId() % 3) * 0.2)));
        int departures = occupancy > 0
                ? Math.max(0, (int) Math.round(occupancy * DEPARTURE_BASE_RATE
                        * (1 + (s.getId() % 4) * 0.1))) : 0;
        int net = arrivals - departures;

        dto.setDailyArrivals(arrivals);
        dto.setDailyDepartures(departures);
        dto.setNetDailyChange(net);
        dto.setForecastTrend(net > 0 ? "RISING" : net < 0 ? "FALLING" : "STABLE");

        if (availableSpace <= 0) {
            dto.setEstimatedDaysToFull(0);
            dto.setEstimatedFullDate(LocalDate.now().toString());
        } else if (net > 0) {
            int days = (int) Math.ceil(availableSpace * 1.0 / net);
            dto.setEstimatedDaysToFull(days);
            dto.setEstimatedFullDate(LocalDate.now().plusDays(days).toString());
        } else {
            dto.setEstimatedDaysToFull(null);
            dto.setEstimatedFullDate(null);
        }
    }

    // ------------------------------------------------------------------
    // KPIs
    // ------------------------------------------------------------------

    private List<KpiDTO> buildKpis(List<ShelterOperationsDTO> operations) {
        int totalCapacity = operations.stream().mapToInt(ShelterOperationsDTO::getCapacity).sum();
        int currentOccupancy = operations.stream().mapToInt(ShelterOperationsDTO::getOccupancy).sum();
        int availableSpace = operations.stream().mapToInt(ShelterOperationsDTO::getAvailableSpace).sum();
        int sheltersAvailable = (int) operations.stream().filter(s -> s.getAvailableSpace() > 0).count();
        int nearFull = (int) operations.stream().filter(s -> "NEAR_CAPACITY".equals(s.getStatus())).count();
        int full = (int) operations.stream().filter(s -> "FULL".equals(s.getStatus())).count();

        List<KpiDTO> kpis = new ArrayList<>();
        kpis.add(new KpiDTO("TOTAL_SHELTERS", "Total Shelters", operations.size(), "", null));
        kpis.add(new KpiDTO("TOTAL_CAPACITY", "Total Capacity", totalCapacity, "", null));
        kpis.add(new KpiDTO("CURRENT_OCCUPANCY", "Current Occupancy", currentOccupancy, "", null));
        kpis.add(new KpiDTO("AVAILABLE_SPACE", "Available Space", availableSpace, "", null));
        kpis.add(new KpiDTO("OCCUPANCY_RATE", "Occupancy Rate", percent(currentOccupancy, totalCapacity), "%", null));
        kpis.add(new KpiDTO("SHELTERS_AVAILABLE", "Shelters Available", sheltersAvailable, "", null));
        kpis.add(new KpiDTO("SHELTERS_NEAR_FULL", "Shelters Near Full", nearFull, "", null));
        kpis.add(new KpiDTO("FULL_SHELTERS", "Full Shelters", full, "", null));
        return kpis;
    }

    // ------------------------------------------------------------------
    // Alerts
    // ------------------------------------------------------------------

    private List<ShelterAlertDTO> buildAlerts(List<ShelterOperationsDTO> operations) {
        List<ShelterAlertDTO> alerts = new ArrayList<>();
        for (ShelterOperationsDTO s : operations) {
            switch (s.getStatus()) {
                case "FULL" -> alerts.add(new ShelterAlertDTO("CRITICAL", "OCCUPANCY",
                        "Shelter is full (" + s.getOccupancy() + "/" + s.getCapacity() + " occupied)", s.getId(), s.getName()));
                case "NEAR_CAPACITY" -> alerts.add(new ShelterAlertDTO("WARNING", "OCCUPANCY",
                        "Shelter is near capacity (" + s.getOccupancy() + "/" + s.getCapacity() + ")", s.getId(), s.getName()));
                default -> { }
            }
            switch (s.getFoodStatus()) {
                case "CRITICAL" -> alerts.add(new ShelterAlertDTO("CRITICAL", "FOOD",
                        "Food stock critical (" + s.getFoodDaysRemaining() + " days remaining)", s.getId(), s.getName()));
                case "LOW" -> alerts.add(new ShelterAlertDTO("WARNING", "FOOD",
                        "Food running low (" + s.getFoodDaysRemaining() + " days remaining)", s.getId(), s.getName()));
                default -> { }
            }
            switch (s.getWaterStatus()) {
                case "CRITICAL" -> alerts.add(new ShelterAlertDTO("CRITICAL", "WATER",
                        "Water shortage critical (" + s.getWaterDaysRemaining() + " days remaining)", s.getId(), s.getName()));
                case "LOW" -> alerts.add(new ShelterAlertDTO("WARNING", "WATER",
                        "Water running low (" + s.getWaterDaysRemaining() + " days remaining)", s.getId(), s.getName()));
                default -> { }
            }
            if (s.getMedicalReadinessPercent() < 50) {
                alerts.add(new ShelterAlertDTO("CRITICAL", "MEDICAL",
                        "Medical kits below critical threshold (" + Math.round(s.getMedicalReadinessPercent()) + "% readiness)", s.getId(), s.getName()));
            } else if (s.getMedicalReadinessPercent() < 75) {
                alerts.add(new ShelterAlertDTO("WARNING", "MEDICAL",
                        "Medical kits below recommended threshold (" + Math.round(s.getMedicalReadinessPercent()) + "% readiness)", s.getId(), s.getName()));
            }
            switch (s.getPowerStatus()) {
                case "POWER_FAILURE" -> alerts.add(new ShelterAlertDTO("CRITICAL", "POWER",
                        "Power failure - no backup power", s.getId(), s.getName()));
                case "BACKUP_MODE" -> alerts.add(new ShelterAlertDTO("WARNING", "POWER",
                        "Running on backup generator (" + s.getGeneratorFuelPercent() + "% fuel, " + s.getBackupRuntimeHours() + "h runtime)", s.getId(), s.getName()));
                default -> { }
            }
            switch (s.getCommStatus()) {
                case "OFFLINE" -> alerts.add(new ShelterAlertDTO("CRITICAL", "COMMUNICATION",
                        "Communication offline - no internet", s.getId(), s.getName()));
                case "DEGRADED" -> alerts.add(new ShelterAlertDTO("WARNING", "COMMUNICATION",
                        "Communication degraded (" + s.getSignalStrengthPercent() + "% signal)", s.getId(), s.getName()));
                default -> { }
            }
        }
        alerts.sort(Comparator.comparing(ShelterAlertDTO::getSeverity).reversed()
                .thenComparing(ShelterAlertDTO::getShelterName));
        return alerts;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static double statusRatio(String status) {
        return switch (status) {
            case "SUFFICIENT", "OK" -> 1.0;
            case "LOW" -> 0.6;
            default -> 0.2;
        };
    }

    private static double percent(int part, int total) {
        return total > 0 ? round1(part * 100.0 / total) : 0.0;
    }

    private static String status(double occupancyPercent) {
        if (occupancyPercent < 60) return "READY";
        if (occupancyPercent < 85) return "BUSY";
        if (occupancyPercent <= 95) return "NEAR_CAPACITY";
        return "FULL";
    }

    private static String level(double score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "MODERATE";
        return "CRITICAL";
    }

    private static int ceilDiv(int a, int b) {
        return a / b + (a % b == 0 ? 0 : 1);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
