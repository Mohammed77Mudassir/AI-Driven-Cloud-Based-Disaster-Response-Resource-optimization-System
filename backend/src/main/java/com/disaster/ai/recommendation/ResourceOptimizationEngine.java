package com.disaster.ai.recommendation;

import com.disaster.ai.core.GeoDistance;
import com.disaster.dto.ai.ResourceAllocationDTO;
import com.disaster.entity.Resource;
import com.disaster.enums.ResourceType;
import com.disaster.repository.ResourceRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resource optimization engine. Compares the quantity required for the scenario
 * against live available inventory (resources marked available and unassigned)
 * and produces per-type allocation lines with deficits and nearest assets.
 */
@Component
public class ResourceOptimizationEngine {

    private final ResourceRepository repository;

    public ResourceOptimizationEngine(ResourceRepository repository) {
        this.repository = repository;
    }

    public List<ResourceAllocationDTO> optimize(String disasterType, String severity,
                                                long population, double lat, double lng) {
        int sevMult = severityMultiplier(severity);
        Map<ResourceType, Integer> required = requiredQuantities(disasterType, sevMult, population);

        List<Resource> available = repository.findByAvailableTrue();
        List<ResourceAllocationDTO> result = new ArrayList<>();

        for (ResourceType type : ResourceType.values()) {
            Integer req = required.getOrDefault(type, 0);
            List<Resource> ofType = available.stream()
                    .filter(r -> r.getResourceType() == type)
                    .sorted(Comparator.comparingDouble(r -> GeoDistance.km(lat, lng, r.getLatitude(), r.getLongitude())))
                    .toList();

            int avail = ofType.stream().mapToInt(Resource::getQuantity).sum();
            int deficit = Math.max(0, req - avail);
            String status = status(req, avail);
            int coveragePercent = req > 0 ? (int) Math.round(avail * 100.0 / req) : 100;
            int sourcesCount = ofType.size();
            double averageDistanceKm = sourcesCount > 0
                    ? Math.round(ofType.stream()
                        .mapToDouble(r -> GeoDistance.km(lat, lng, r.getLatitude(), r.getLongitude()))
                        .average().orElse(0))
                    : 0;

            ResourceAllocationDTO dto = new ResourceAllocationDTO();
            dto.setResourceType(type.name());
            dto.setRequired(req);
            dto.setAvailable(avail);
            dto.setDeficit(deficit);
            dto.setStatus(status);
            dto.setCoveragePercent(coveragePercent);
            dto.setRecommendedAction(recommendedAction(status));
            dto.setSourcesCount(sourcesCount);
            dto.setAverageDistanceKm(averageDistanceKm);
            dto.setNearestAssets(nearest(ofType, 2));
            result.add(dto);
        }
        return result;
    }

    private String recommendedAction(String status) {
        return switch (status) {
            case "ADEQUATE" -> "Maintain current posture";
            case "PARTIAL" -> "Reallocate from surplus regions or request external reinforcement";
            case "CRITICAL" -> "Escalate for immediate external reinforcement";
            default -> "No deployment planned";
        };
    }

    private Map<ResourceType, Integer> requiredQuantities(String type, int m, long population) {
        Map<ResourceType, Integer> req = new EnumMap<>(ResourceType.class);
        req.put(ResourceType.AMBULANCE, m * 3);
        req.put(ResourceType.FIRE_TRUCK, switch (type) {
            case "Wildfire" -> m * 8;
            case "Earthquake" -> m * 5;
            default -> m * 2;
        });
        req.put(ResourceType.POLICE_TEAM, m * 2);
        req.put(ResourceType.MEDICAL_TEAM, m * 4);
        req.put(ResourceType.RESCUE_TEAM, m * 5);
        req.put(ResourceType.BOAT, switch (type) {
            case "Flood", "Tsunami" -> m * 10;
            default -> m * 2;
        });
        req.put(ResourceType.HELICOPTER, m);
        req.put(ResourceType.GENERATOR, m * 3);

        double populationScale = 1.0 + clamp(Math.log10(population + 1) / 6.5 - 0.5, 0, 1);
        req.replaceAll((k, v) -> Math.max(1, (int) Math.round(v * populationScale)));
        return req;
    }

    private String status(int required, int available) {
        if (required <= 0) return "NOT_REQUIRED";
        if (available >= required) return "ADEQUATE";
        if (available > 0) return "PARTIAL";
        return "CRITICAL";
    }

    private String nearest(List<Resource> resources, int limit) {
        return resources.stream().limit(limit)
                .map(r -> r.getQuantity() + " x '" + safe(r.getLocation()) + "'")
                .reduce((a, b) -> a + ", " + b)
                .orElse("None available");
    }

    private int severityMultiplier(String severity) {
        return switch (severity == null ? "" : severity) {
            case "Critical" -> 5;
            case "High" -> 4;
            case "Medium" -> 2;
            default -> 1;
        };
    }

    private String safe(String v) {
        return v == null || v.isBlank() ? "unknown location" : v;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
