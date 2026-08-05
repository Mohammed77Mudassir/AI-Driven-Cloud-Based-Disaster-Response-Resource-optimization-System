package com.disaster.ai.recommendation;

import com.disaster.ai.core.GeoDistance;
import com.disaster.dto.ai.ShelterRecommendationDTO;
import com.disaster.entity.Shelter;
import com.disaster.repository.ShelterRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks shelters by proximity, remaining space, life-sustaining amenities
 * (food / water / power) and medical kit availability. Overcrowded shelters
 * are penalized and flagged via {@code overflowRisk}.
 */
@Component
public class ShelterRecommendationEngine {

    private static final double RADIUS_KM = 100;

    /** Average convoy speed in km/h used to derive travel time estimates. */
    private static final double TRAVEL_SPEED_KMH = 40;

    /** Overcrowding threshold: occupancy reaching 90% of capacity. */
    private static final double OVERFLOW_RATIO = 0.9;

    private final ShelterRepository repository;

    public ShelterRecommendationEngine(ShelterRepository repository) {
        this.repository = repository;
    }

    public List<ShelterRecommendationDTO> recommend(double lat, double lng, int limit) {
        List<ShelterRecommendationDTO> result = new ArrayList<>();
        for (Shelter s : repository.findAll()) {
            double distance = GeoDistance.km(lat, lng, s.getLatitude(), s.getLongitude());
            double proximity = GeoDistance.proximity(distance, RADIUS_KM);
            int availableSpace = Math.max(0, s.getCapacity() - s.getOccupancy());
            double spaceRatio = s.getCapacity() > 0 ? Math.min(1, (double) availableSpace / s.getCapacity()) : 0;
            double amenities = ((s.isFoodAvailable() ? 1 : 0) + (s.isWaterAvailable() ? 1 : 0)
                    + (s.isPowerAvailable() ? 1 : 0)) / 3.0;
            double medical = Math.min(1, s.getMedicalKits() / 50.0);

            double occupancyPercent = s.getCapacity() > 0 ? round(s.getOccupancy() * 100.0 / s.getCapacity()) : 100;
            boolean overflowRisk = s.getCapacity() > 0 && s.getOccupancy() >= OVERFLOW_RATIO * s.getCapacity();

            double score = 100 * (0.40 * proximity + 0.25 * spaceRatio + 0.20 * amenities + 0.15 * medical);
            if (overflowRisk) {
                score = Math.max(0, score - 15);
            }

            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("proximity", round(100 * 0.40 * proximity));
            breakdown.put("spaceRatio", round(100 * 0.25 * spaceRatio));
            breakdown.put("amenities", round(100 * 0.20 * amenities));
            breakdown.put("medical", round(100 * 0.15 * medical));
            if (overflowRisk) breakdown.put("overcrowdingPenalty", -15.0);

            ShelterRecommendationDTO dto = new ShelterRecommendationDTO();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setAddress(s.getAddress());
            dto.setContact(s.getContact());
            dto.setDistanceKm(round(distance));
            dto.setTravelTimeMinutes(round(distance / TRAVEL_SPEED_KMH * 60));
            dto.setOccupancyPercent(occupancyPercent);
            dto.setOverflowRisk(overflowRisk);
            dto.setCapacity(s.getCapacity());
            dto.setOccupancy(s.getOccupancy());
            dto.setAvailableSpace(availableSpace);
            dto.setFoodAvailable(s.isFoodAvailable());
            dto.setWaterAvailable(s.isWaterAvailable());
            dto.setPowerAvailable(s.isPowerAvailable());
            dto.setMedicalKits(s.getMedicalKits());
            dto.setScore(round(score));
            dto.setScoreBreakdown(breakdown);
            dto.setMatchReason(reason(s, availableSpace, overflowRisk));
            result.add(dto);
        }
        result.sort(Comparator.comparingDouble(ShelterRecommendationDTO::getScore).reversed());
        return result.stream().limit(Math.max(0, limit)).toList();
    }

    private String reason(Shelter s, int availableSpace, boolean overflowRisk) {
        List<String> parts = new ArrayList<>();
        parts.add(availableSpace + " spaces free");
        if (s.isFoodAvailable()) parts.add("food");
        if (s.isWaterAvailable()) parts.add("water");
        if (s.isPowerAvailable()) parts.add("power");
        if (s.getMedicalKits() > 0) parts.add(s.getMedicalKits() + " medical kits");
        if (overflowRisk) parts.add("overflow risk");
        return String.join(", ", parts);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
