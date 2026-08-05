package com.disaster.service;

import com.disaster.ai.AIEngineService;
import com.disaster.dto.ResourceRecommendationDTO;
import com.disaster.dto.ai.AIAnalysisRequest;
import com.disaster.dto.ai.HospitalRecommendationDTO;
import com.disaster.dto.ai.RecommendationResponse;
import com.disaster.dto.ai.ResourceAllocationDTO;
import com.disaster.dto.ai.ShelterRecommendationDTO;
import com.disaster.dto.ai.VolunteerRecommendationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Legacy adapter for the resource recommendation contract, now backed by the
 * clean-architecture AI engine and live inventory data.
 */
@Service
public class ResourceOptimizationService {

    private final AIEngineService engine;

    public ResourceOptimizationService(AIEngineService engine) {
        this.engine = engine;
    }

    public ResourceRecommendationDTO recommend(String disasterType, String severity, long population, String location) {
        AIAnalysisRequest request = new AIAnalysisRequest();
        request.setDisasterType(disasterType);
        request.setSeverity(severity);
        request.setLocation(location);
        request.setLatitude(0);
        request.setLongitude(0);
        if (population > 0) {
            request.setPopulation(population);
        }

        RecommendationResponse rec = engine.recommend(request);

        ResourceRecommendationDTO dto = new ResourceRecommendationDTO();
        dto.setDisasterType(disasterType);
        dto.setSeverity(severity);
        dto.setLocation(location);
        dto.setPopulation(population > 0 ? population : 10000);

        for (ResourceAllocationDTO a : rec.getResources()) {
            switch (a.getResourceType()) {
                case "AMBULANCE" -> dto.setAmbulances(a.getRequired());
                case "FIRE_TRUCK" -> dto.setFireTrucks(a.getRequired());
                case "POLICE_TEAM" -> dto.setPoliceTeams(a.getRequired());
                case "MEDICAL_TEAM" -> dto.setMedicalTeams(a.getRequired());
                case "RESCUE_TEAM" -> dto.setRescueWorkers(a.getRequired());
                case "BOAT" -> dto.setBoats(a.getRequired());
                case "HELICOPTER" -> dto.setHelicopters(a.getRequired());
                case "GENERATOR" -> dto.setGenerators(a.getRequired());
                default -> { }
            }
        }
        dto.setVolunteers(rec.getVolunteers().stream().mapToInt(v -> 1).sum() * 20);

        dto.setReasoning(String.format(
                "Optimized against live inventory. %s severity %s event at %s. %d resource lines planned; %d volunteer(s) matched.",
                severity, disasterType,
                location != null && !location.isBlank() ? location : "unknown location",
                rec.getResources().size(), rec.getVolunteers().size()));

        dto.setPriority(rec.getConfidence() != null && rec.getConfidence().getOverall() >= 85
                ? "HIGH" : "MEDIUM");
        dto.setConfidenceScore(rec.getConfidence() != null ? rec.getConfidence().getOverall() : 75);
        dto.setConfidenceBreakdown(rec.getConfidence());

        if (rec.getEvacuation() != null) {
            dto.setEvacuationRecommendation(String.format(
                    "Danger radius %.0f km. %s. Priority zones: %s",
                    rec.getEvacuation().getDangerRadiusKm(),
                    rec.getEvacuation().getEvacuationWindow(),
                    String.join("; ", rec.getEvacuation().getPriorityZones())));
        }
        if (!rec.getHospitals().isEmpty()) {
            HospitalRecommendationDTO top = rec.getHospitals().get(0);
            dto.setHospitalRecommendation(String.format(
                    "Primary: %s (%.1f km, %d beds). Alternates: %s.",
                    top.getName(), top.getDistanceKm(), top.getAvailableBeds(),
                    String.join(", ", rec.getHospitals().stream().skip(1)
                            .map(HospitalRecommendationDTO::getName).toList())));
        }
        if (!rec.getShelters().isEmpty()) {
            int totalSpace = rec.getShelters().stream().mapToInt(ShelterRecommendationDTO::getAvailableSpace).sum();
            dto.setShelterRecommendation(totalSpace + " shelter spaces across "
                    + rec.getShelters().size() + " nearest shelters.");
        }
        if (!rec.getVolunteers().isEmpty()) {
            VolunteerRecommendationDTO top = rec.getVolunteers().get(0);
            dto.setVolunteerRecommendation("Top volunteer: " + top.getName()
                    + " (" + top.getSkills() + ", " + top.getDistanceKm() + " km).");
        }

        dto.setAllocations(rec.getResources());
        return dto;
    }
}
