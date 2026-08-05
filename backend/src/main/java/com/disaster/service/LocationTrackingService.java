package com.disaster.service;

import com.disaster.dto.HeatPointDTO;
import com.disaster.dto.LocationDTO;
import com.disaster.dto.MonitoringOverviewDTO;
import com.disaster.entity.*;
import com.disaster.enums.DroneStatus;
import com.disaster.geo.GeoUtils;
import com.disaster.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central service for the Real-Time Monitoring module.
 * Aggregates every map-able entity (disasters, drones, hospitals, shelters,
 * volunteers, resources, rescue teams, vehicles) and drives the live drone
 * simulation used by both the REST live endpoint and the WebSocket scheduler.
 */
@Service
public class LocationTrackingService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final List<String> ACTIVE_DISASTER_STATUSES =
            List.of("PENDING", "VERIFIED", "ASSIGNED", "RESOURCES_DISPATCHED", "IN_PROGRESS");

    private final DroneRepository droneRepository;
    private final VolunteerRepository volunteerRepository;
    private final ResourceRepository resourceRepository;
    private final DisasterRepository disasterRepository;
    private final HospitalRepository hospitalRepository;
    private final ShelterRepository shelterRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final RescueVehicleRepository rescueVehicleRepository;

    public LocationTrackingService(DroneRepository droneRepository,
                                   VolunteerRepository volunteerRepository,
                                   ResourceRepository resourceRepository,
                                   DisasterRepository disasterRepository,
                                   HospitalRepository hospitalRepository,
                                   ShelterRepository shelterRepository,
                                   RescueTeamRepository rescueTeamRepository,
                                   RescueVehicleRepository rescueVehicleRepository) {
        this.droneRepository = droneRepository;
        this.volunteerRepository = volunteerRepository;
        this.resourceRepository = resourceRepository;
        this.disasterRepository = disasterRepository;
        this.hospitalRepository = hospitalRepository;
        this.shelterRepository = shelterRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.rescueVehicleRepository = rescueVehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> getAllLocations() {
        List<LocationDTO> locations = new ArrayList<>();
        locations.addAll(buildDisasterLocations());
        locations.addAll(buildDroneLocations());
        locations.addAll(buildHospitalLocations());
        locations.addAll(buildShelterLocations());
        locations.addAll(buildVolunteerLocations());
        locations.addAll(buildResourceLocations());
        locations.addAll(buildTeamLocations());
        locations.addAll(buildVehicleLocations());
        return locations;
    }

    /**
     * Steps every in-mission drone toward its assigned disaster (or a smooth
     * drifting path when unassigned) and returns the live positions.
     */
    @Transactional
    public List<LocationDTO> simulateMovement() {
        List<LocationDTO> live = new ArrayList<>();
        for (Drone d : droneRepository.findByStatus(DroneStatus.IN_MISSION)) {
            stepDrone(d);
            droneRepository.save(d);
            live.add(toDroneLocation(d));
        }
        return live;
    }

    private void stepDrone(Drone d) {
        double targetLat = d.getLatitude();
        double targetLng = d.getLongitude();
        if (d.getAssignedDisaster() != null) {
            Disaster disaster = d.getAssignedDisaster();
            if (disaster.getLatitude() != 0 || disaster.getLongitude() != 0) {
                targetLat = disaster.getLatitude();
                targetLng = disaster.getLongitude();
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double distToTarget = GeoUtils.distanceKm(d.getLatitude(), d.getLongitude(), targetLat, targetLng);
        double stepKm = 0.8 + random.nextDouble() * 1.6;
        if (distToTarget <= stepKm) {
            // Loiter around the target so the marker keeps moving smoothly.
            double bearing = random.nextDouble() * 360;
            double[] next = GeoUtils.destination(d.getLatitude(), d.getLongitude(), bearing, 0.5);
            d.setLatitude(round6(next[0]));
            d.setLongitude(round6(next[1]));
        } else {
            double bearing = GeoUtils.bearing(d.getLatitude(), d.getLongitude(), targetLat, targetLng);
            double[] next = GeoUtils.destination(d.getLatitude(), d.getLongitude(), bearing, stepKm);
            d.setLatitude(round6(next[0]));
            d.setLongitude(round6(next[1]));
        }

        d.setBattery(Math.max(0, d.getBattery() - 1));
        if (d.getBattery() <= 0) {
            d.setStatus(DroneStatus.CHARGING);
        }
    }

    /**
     * Lightweight list used for the LOCATION_SNAPSHOT websocket event:
     * active disasters + all drones + deployed teams.
     */
    @Transactional(readOnly = true)
    public List<LocationDTO> getLocationsSnapshot() {
        List<LocationDTO> locations = new ArrayList<>();
        for (Disaster disaster : disasterRepository.findAllByOrderByDateDesc()) {
            if (ACTIVE_DISASTER_STATUSES.contains(disaster.getStatus() != null ? disaster.getStatus().name() : "")) {
                locations.add(toDisasterLocation(disaster));
            }
        }
        for (Drone d : droneRepository.findAll()) {
            if (d.getStatus() == DroneStatus.IN_MISSION) {
                locations.add(toDroneLocation(d));
            }
        }
        for (RescueTeam team : rescueTeamRepository.findByStatus("ON_MISSION")) {
            if (team.getLastLocationUpdateAt() != null && team.getLatitude() != 0) {
                locations.add(toTeamLocation(team));
            }
        }
        return locations;
    }

    /**
     * Weighted points for the heat map overlay. Disasters dominate with a
     * severity-scaled intensity, support entities contribute a lower weight so
     * the map still shows response coverage across the region.
     */
    @Transactional(readOnly = true)
    public List<HeatPointDTO> getHeatmapPoints() {
        List<HeatPointDTO> points = new ArrayList<>();
        for (Disaster d : disasterRepository.findAllByOrderByDateDesc()) {
            if (d.getLatitude() == 0 && d.getLongitude() == 0) continue;
            String severity = d.getSeverity() == null ? "Medium" : d.getSeverity();
            double intensity = switch (severity) {
                case "Critical" -> 1.0;
                case "High" -> 0.8;
                case "Medium" -> 0.55;
                default -> 0.35;
            };
            points.add(new HeatPointDTO(d.getLatitude(), d.getLongitude(), intensity, "DISASTER"));
        }
        for (Hospital h : hospitalRepository.findAll()) {
            if (h.getLatitude() == 0 && h.getLongitude() == 0) continue;
            int beds = h.getAvailableBeds() + h.getIcuBeds();
            points.add(new HeatPointDTO(h.getLatitude(), h.getLongitude(),
                    beds > 0 ? Math.min(0.5, h.getIcuBeds() / (double) beds * 0.5) : 0.2, "HOSPITAL"));
        }
        for (Shelter s : shelterRepository.findAll()) {
            if (s.getLatitude() == 0 && s.getLongitude() == 0) continue;
            int occupancyPercent = s.getCapacity() > 0 ? (int) Math.round(s.getOccupancy() * 100.0 / s.getCapacity()) : 0;
            points.add(new HeatPointDTO(s.getLatitude(), s.getLongitude(),
                    Math.min(0.6, 0.2 + occupancyPercent / 100.0 * 0.4), "SHELTER"));
        }
        for (RescueTeam t : rescueTeamRepository.findByStatus("ON_MISSION")) {
            if (t.getLatitude() == 0 && t.getLongitude() == 0) continue;
            points.add(new HeatPointDTO(t.getLatitude(), t.getLongitude(), 0.5, "RESCUE_TEAM"));
        }
        for (Volunteer v : volunteerRepository.findAll()) {
            if (v.getLatitude() == 0 && v.getLongitude() == 0) continue;
            points.add(new HeatPointDTO(v.getLatitude(), v.getLongitude(),
                    v.isAvailable() ? 0.25 : 0.4, "VOLUNTEER"));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public MonitoringOverviewDTO getOverview() {
        List<LocationDTO> locations = getAllLocations();
        List<Disaster> disasters = disasterRepository.findAllByOrderByDateDesc();

        MonitoringOverviewDTO dto = new MonitoringOverviewDTO();
        dto.setLocations(locations);
        dto.setDisasters(disasters.size());
        dto.setActiveDisasters((int) disasters.stream()
                .filter(d -> d.getStatus() != null && ACTIVE_DISASTER_STATUSES.contains(d.getStatus().name())).count());
        dto.setDrones(droneRepository.findAll().size());
        dto.setDronesInMission(droneRepository.findByStatus(DroneStatus.IN_MISSION).size());
        dto.setHospitals(hospitalRepository.findAll().size());
        dto.setShelters(shelterRepository.findAll().size());
        dto.setVolunteers(volunteerRepository.findAll().size());
        dto.setVolunteersAvailable(volunteerRepository.findByAvailableTrue().size());
        dto.setResources(resourceRepository.findAll().size());
        dto.setTeams(rescueTeamRepository.findAll().size());
        dto.setTeamsAvailable(rescueTeamRepository.findByStatus("AVAILABLE").size());
        dto.setVehicles(rescueVehicleRepository.findAll().size());
        dto.setUpdatedAt(LocalDateTime.now().format(TS));
        return dto;
    }

    // ------------------------------------------------------------------
    // Entity builders
    // ------------------------------------------------------------------

    private List<LocationDTO> buildDisasterLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Disaster d : disasterRepository.findAllByOrderByDateDesc()) {
            if (d.getLatitude() == 0 && d.getLongitude() == 0) continue;
            out.add(toDisasterLocation(d));
        }
        return out;
    }

    private LocationDTO toDisasterLocation(Disaster d) {
        String severity = d.getSeverity() == null ? "Medium" : d.getSeverity();
        LocationDTO loc = base(d, "DISASTER", d.getDisasterType(), d.getLatitude(), d.getLongitude(),
                d.getStatus() != null ? d.getStatus().name() : "PENDING",
                switch (severity) {
                    case "Critical" -> "red";
                    case "High" -> "orange";
                    case "Medium" -> "yellow";
                    default -> "green";
                });
        loc.setSeverity(severity);
        loc.setUpdatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt().format(TS) : d.getDate() != null ? d.getDate().format(TS) : null);
        loc.addExtra("location", d.getLocation());
        loc.addExtra("priority", d.getPriority() != null ? d.getPriority().name() : null);
        return loc;
    }

    private List<LocationDTO> buildDroneLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Drone d : droneRepository.findAll()) {
            out.add(toDroneLocation(d));
        }
        return out;
    }

    private LocationDTO toDroneLocation(Drone d) {
        LocationDTO loc = base(d, "DRONE", "Drone " + d.getDroneId(), d.getLatitude(), d.getLongitude(),
                d.getStatus() != null ? d.getStatus().name() : "AVAILABLE",
                d.getStatus() == DroneStatus.IN_MISSION ? "red"
                        : d.getStatus() == DroneStatus.AVAILABLE ? "green"
                        : d.getStatus() == DroneStatus.CHARGING ? "yellow"
                        : d.getStatus() == DroneStatus.MAINTENANCE ? "orange" : "gray");
        loc.setBattery(d.getBattery());
        if (d.getStatus() == DroneStatus.IN_MISSION) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            loc.setSpeed(20.0 + random.nextDouble() * 20.0);
            loc.setHeading(random.nextDouble() * 360);
        }
        loc.setUpdatedAt(LocalDateTime.now().format(TS));
        loc.addExtra("cameraStatus", d.isCameraStatus());
        loc.addExtra("missionStatus", d.getMissionStatus() != null ? d.getMissionStatus().name() : null);
        return loc;
    }

    private List<LocationDTO> buildHospitalLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Hospital h : hospitalRepository.findAll()) {
            if (h.getLatitude() == 0 && h.getLongitude() == 0) continue;
            LocationDTO loc = base(h, "HOSPITAL", h.getName(), h.getLatitude(), h.getLongitude(),
                    "Beds: " + h.getAvailableBeds(), h.getAvailableBeds() > 10 ? "green" : h.getAvailableBeds() > 3 ? "yellow" : "red");
            loc.setCapacity(h.getAvailableBeds() + h.getIcuBeds());
            loc.setOccupancy(h.getIcuBeds());
            loc.addExtra("icuBeds", h.getIcuBeds());
            loc.addExtra("doctors", h.getDoctorsAvailable());
            loc.addExtra("bloodBank", h.isBloodBank());
            out.add(loc);
        }
        return out;
    }

    private List<LocationDTO> buildShelterLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Shelter s : shelterRepository.findAll()) {
            if (s.getLatitude() == 0 && s.getLongitude() == 0) continue;
            int occupancyPercent = s.getCapacity() > 0 ? (int) Math.round(s.getOccupancy() * 100.0 / s.getCapacity()) : 0;
            LocationDTO loc = base(s, "SHELTER", s.getName(), s.getLatitude(), s.getLongitude(),
                    "Occupancy: " + occupancyPercent + "%",
                    occupancyPercent > 80 ? "red" : occupancyPercent > 50 ? "yellow" : "green");
            loc.setCapacity(s.getCapacity());
            loc.setOccupancy(s.getOccupancy());
            loc.addExtra("food", s.isFoodAvailable());
            loc.addExtra("water", s.isWaterAvailable());
            loc.addExtra("power", s.isPowerAvailable());
            out.add(loc);
        }
        return out;
    }

    private List<LocationDTO> buildVolunteerLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Volunteer v : volunteerRepository.findAll()) {
            if (v.getLatitude() == 0 && v.getLongitude() == 0) continue;
            LocationDTO loc = base(v, "VOLUNTEER", v.getName(), v.getLatitude(), v.getLongitude(),
                    v.isAvailable() ? "AVAILABLE" : "DEPLOYED",
                    v.isAvailable() ? "green" : "orange");
            loc.addExtra("skills", v.getSkills());
            loc.addExtra("assignedDisaster", v.getAssignedDisaster() != null ? v.getAssignedDisaster().getDisasterType() : null);
            out.add(loc);
        }
        return out;
    }

    private List<LocationDTO> buildResourceLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (Resource r : resourceRepository.findAll()) {
            if (r.getLatitude() == 0 && r.getLongitude() == 0) continue;
            String status = r.getInMaintenanceQuantity() > 0 ? "MAINTENANCE"
                    : r.getDeployedQuantity() > 0 ? "DEPLOYED" : "AVAILABLE";
            LocationDTO loc = base(r, "RESOURCE", r.getResourceType() != null ? r.getResourceType().name() : "Resource",
                    r.getLatitude(), r.getLongitude(), status,
                    switch (status) {
                        case "AVAILABLE" -> "green";
                        case "DEPLOYED" -> "orange";
                        default -> "gray";
                    });
            loc.addExtra("quantity", r.getQuantity());
            loc.addExtra("totalQuantity", r.getTotalQuantity());
            loc.addExtra("deployedQuantity", r.getDeployedQuantity());
            loc.addExtra("condition", r.getCondition() != null ? r.getCondition().name() : null);
            out.add(loc);
        }
        return out;
    }

    private List<LocationDTO> buildTeamLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (RescueTeam t : rescueTeamRepository.findAll()) {
            if (t.getLatitude() == 0 && t.getLongitude() == 0) continue;
            out.add(toTeamLocation(t));
        }
        return out;
    }

    private LocationDTO toTeamLocation(RescueTeam t) {
        LocationDTO loc = base(t, "RESCUE_TEAM", t.getTeamName(), t.getLatitude(), t.getLongitude(),
                t.getStatus() != null ? t.getStatus() : "AVAILABLE",
                switch (t.getStatus() == null ? "" : t.getStatus()) {
                    case "ON_MISSION" -> "red";
                    case "AVAILABLE" -> "green";
                    case "ASSIGNED" -> "orange";
                    default -> "yellow";
                });
        loc.setCapacity(t.getMaxCapacity());
        loc.setOccupancy(t.getMemberCount());
        loc.addExtra("leader", t.getTeamLeader());
        loc.addExtra("specialty", t.getSpecialty());
        loc.addExtra("lastUpdate", t.getLastLocationUpdateAt() != null ? t.getLastLocationUpdateAt().format(TS) : null);
        loc.setUpdatedAt(t.getLastLocationUpdateAt() != null ? t.getLastLocationUpdateAt().format(TS) : null);
        return loc;
    }

    private List<LocationDTO> buildVehicleLocations() {
        List<LocationDTO> out = new ArrayList<>();
        for (RescueVehicle v : rescueVehicleRepository.findAll()) {
            if (v.getLatitude() == 0 && v.getLongitude() == 0) continue;
            String status = v.getStatus() != null ? v.getStatus().name() : "AVAILABLE";
            LocationDTO loc = base(v, "VEHICLE", v.getRegistrationNumber() != null ? v.getRegistrationNumber() : "Vehicle",
                    v.getLatitude(), v.getLongitude(), status,
                    switch (status) {
                        case "DEPLOYED" -> "orange";
                        case "IN_MAINTENANCE" -> "gray";
                        case "AVAILABLE" -> "green";
                        default -> "yellow";
                    });
            loc.addExtra("type", v.getVehicleType() != null ? v.getVehicleType().name() : null);
            loc.addExtra("fuelLevel", v.getFuelLevel());
            loc.addExtra("model", v.getModel());
            out.add(loc);
        }
        return out;
    }

    private LocationDTO base(Object entity, String type, String name, double lat, double lng, String status, String color) {
        LocationDTO loc = new LocationDTO();
        loc.setEntityType(type);
        if (entity instanceof com.disaster.entity.Resource r) loc.setEntityId(r.getId());
        else if (entity instanceof com.disaster.entity.Hospital h) loc.setEntityId(h.getId());
        else if (entity instanceof com.disaster.entity.Shelter s) loc.setEntityId(s.getId());
        else if (entity instanceof com.disaster.entity.Volunteer v) loc.setEntityId(v.getId());
        else if (entity instanceof com.disaster.entity.Drone d) loc.setEntityId(d.getId());
        else if (entity instanceof com.disaster.entity.Disaster d) loc.setEntityId(d.getId());
        else if (entity instanceof com.disaster.entity.RescueTeam t) loc.setEntityId(t.getId());
        else if (entity instanceof com.disaster.entity.RescueVehicle v) loc.setEntityId(v.getId());
        loc.setName(name);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setStatus(status);
        loc.setMarkerColor(color);
        return loc;
    }

    private double round6(double value) {
        return Math.round(value * 1_000_000.0) / 1_000_000.0;
    }
}
