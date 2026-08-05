package com.disaster.ai.recommendation;

import com.disaster.ai.model.AnalysisInput;
import com.disaster.dto.ai.EvacuationPlanDTO;
import com.disaster.dto.ai.ShelterRecommendationDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Produces a structured evacuation recommendation: danger radius, evacuation
 * window, priority zones and actionable instructions, using the nearest shelters
 * as assembly points.
 */
@Component
public class EvacuationEngine {

    private static final Map<String, Double> BASE_RADIUS_KM = Map.of(
            "Tsunami", 25.0, "Earthquake", 5.0, "Flood", 8.0,
            "Cyclone", 40.0, "Wildfire", 12.0, "Landslide", 6.0,
            "Drought", 0.0, "Epidemic", 4.0
    );

    private static final Map<String, String> WINDOW = Map.of(
            "Tsunami", "Immediate - within 60 minutes",
            "Earthquake", "Within 6 hours of the event",
            "Flood", "Within 24 hours",
            "Cyclone", "Before landfall - within 12 hours",
            "Wildfire", "Within 6 hours",
            "Landslide", "Within 6 hours",
            "Drought", "Phased relocation over 7 days if water is depleted",
            "Epidemic", "Targeted movement restriction immediately"
    );

    private static final Map<String, List<String>> ROUTES = Map.of(
            "Flood", List.of(
                    "Route A: primary arterial road to assembly point 1",
                    "Route B: elevated corridor bypassing flooded zones",
                    "Route C: railway corridor to district relief hub"),
            "Earthquake", List.of(
                    "Route A: main boulevard to open-ground assembly point",
                    "Route B: secondary road avoiding damaged overpasses",
                    "Route C: ring road to field hospital staging area"),
            "Cyclone", List.of(
                    "Route A: coastal highway to inland cyclone shelter",
                    "Route B: evacuation corridor to elevated community hall",
                    "Route C: railway line to relief staging base"),
            "Wildfire", List.of(
                    "Route A: cleared firebreak road to safe zone",
                    "Route B: downwind arterial road to evacuee center",
                    "Route C: highway shoulder corridor to fire station"),
            "Tsunami", List.of(
                    "Route A: rapid coastal evacuation lane to high ground",
                    "Route B: elevated bridge corridor away from the shoreline",
                    "Route C: rail corridor to inland assembly point"),
            "Landslide", List.of(
                    "Route A: ridgeline road to stable ground",
                    "Route B: alternative corridor bypassing slip zones",
                    "Route C: valley road to medical staging area"),
            "Drought", List.of(
                    "Route A: supply convoy corridor to water distribution point",
                    "Route B: livestock relocation trail to pasture reserve",
                    "Route C: road corridor to relief grain storage"),
            "Epidemic", List.of(
                    "Route A: controlled corridor to quarantine facility",
                    "Route B: staff-only access road to field hospital",
                    "Route C: supply lane to cold-chain storage")
    );

    private static final List<String> DEFAULT_ROUTES = List.of(
            "Route A: main access road to the nearest assembly point",
            "Route B: secondary road to relief staging area");

    public EvacuationPlanDTO plan(AnalysisInput input, List<ShelterRecommendationDTO> assemblyPoints) {
        double base = BASE_RADIUS_KM.getOrDefault(input.getDisasterType(), 5.0);
        int sev = switch (input.getSeverity() == null ? "" : input.getSeverity()) {
            case "Critical" -> 4;
            case "High" -> 3;
            case "Medium" -> 2;
            default -> 1;
        };
        double radius = round(base * (0.6 + 0.15 * (sev - 1)));

        List<ShelterRecommendationDTO> points = assemblyPoints.stream().limit(3).toList();
        int assemblyCapacity = points.stream().mapToInt(ShelterRecommendationDTO::getCapacity).sum();

        List<String> routes = ROUTES.getOrDefault(input.getDisasterType(), DEFAULT_ROUTES);
        int routeCount = Math.min(sev >= 3 ? 3 : 2, routes.size());

        EvacuationPlanDTO dto = new EvacuationPlanDTO();
        dto.setDangerRadiusKm(radius);
        dto.setEvacuationWindow(WINDOW.getOrDefault(input.getDisasterType(),
                "Activate standard evacuation protocol"));
        dto.setPriorityZones(priorityZones(input.getDisasterType(), sev));
        dto.setInstructions(instructions(input.getDisasterType()));
        dto.setAssemblyPoints(points);
        dto.setAssemblyCapacity(assemblyCapacity);
        dto.setEvacuationRoutes(new ArrayList<>(routes.subList(0, routeCount)));
        return dto;
    }

    private List<String> priorityZones(String type, int sev) {
        List<String> zones = new ArrayList<>();
        switch (type) {
            case "Tsunami", "Cyclone", "Flood" -> {
                zones.add("Low-lying and coastal zones (evacuate first)");
                zones.add("Areas within " + (sev >= 3 ? "10" : "5") + " km of water bodies");
                zones.add("Informal settlements without elevated shelter");
            }
            case "Earthquake" -> {
                zones.add("Buildings with pre-existing structural risk");
                zones.add("Dense urban residential blocks");
                zones.add("Areas near utility corridors");
            }
            case "Wildfire" -> {
                zones.add("Communities downwind of the fire line");
                zones.add("Vegetation-adjacent settlements");
                zones.add("Roads in the fire path (clear first)");
            }
            case "Landslide" -> {
                zones.add("Settlements on unstable slopes");
                zones.add("Canyon / debris-flow corridors");
                zones.add("Roads through landslide-prone passes");
            }
            case "Epidemic" -> {
                zones.add("Containment / quarantine clusters");
                zones.add("High-density neighbourhoods");
                zones.add("Care facilities and schools");
            }
            case "Drought" -> {
                zones.add("Areas with fully depleted water sources");
                zones.add("Livestock-dependent communities");
            }
            default -> zones.add("Primary hazard exposure zone");
        }
        return zones;
    }

    private List<String> instructions(String type) {
        List<String> instructions = new ArrayList<>();
        instructions.add("Route evacuees to the nearest open assembly point with transport support.");
        instructions.add("Carry identity documents, medicines and emergency contact details.");
        instructions.add("Follow official announcements; do not return until clearance is issued.");
        switch (type) {
            case "Flood", "Tsunami", "Cyclone" -> instructions.add("Move to elevated ground or designated cyclone shelters; avoid waterlogged routes.");
            case "Earthquake" -> instructions.add("Use open grounds for assembly; keep clear of damaged structures.");
            case "Wildfire" -> instructions.add("Use cleared corridors only; evacuate perpendicular to the wind direction.");
            case "Epidemic" -> instructions.add("Follow quarantine protocols; use masks and maintain distance at assembly points.");
            default -> { }
        }
        return instructions;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
