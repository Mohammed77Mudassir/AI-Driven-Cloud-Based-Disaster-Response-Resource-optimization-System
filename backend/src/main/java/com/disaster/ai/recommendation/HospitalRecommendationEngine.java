package com.disaster.ai.recommendation;

import com.disaster.ai.core.GeoDistance;
import com.disaster.dto.ai.HospitalRecommendationDTO;
import com.disaster.entity.Hospital;
import com.disaster.repository.HospitalRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks hospitals for a disaster scenario by a weighted composite of proximity,
 * bed capacity, ICU strength, doctors available and blood bank support. Each
 * component contribution is exposed in {@code scoreBreakdown} for explainability.
 */
@Component
public class HospitalRecommendationEngine {

    private static final double RADIUS_KM = 150;

    /** Average convoy speed in km/h used to derive travel time estimates. */
    private static final double TRAVEL_SPEED_KMH = 40;

    private final HospitalRepository repository;

    public HospitalRecommendationEngine(HospitalRepository repository) {
        this.repository = repository;
    }

    public List<HospitalRecommendationDTO> recommend(double lat, double lng, int limit) {
        List<HospitalRecommendationDTO> result = new ArrayList<>();
        for (Hospital h : repository.findAll()) {
            double distance = GeoDistance.km(lat, lng, h.getLatitude(), h.getLongitude());
            double proximity = GeoDistance.proximity(distance, RADIUS_KM);
            double beds = Math.min(1, h.getAvailableBeds() / 60.0);
            double icu = Math.min(1, h.getIcuBeds() / 25.0);
            double doctors = Math.min(1, h.getDoctorsAvailable() / 40.0);
            double blood = h.isBloodBank() ? 1.0 : 0.0;

            double score = 100 * (0.40 * proximity + 0.25 * beds + 0.15 * icu + 0.10 * doctors + 0.10 * blood);

            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("proximity", round(100 * 0.40 * proximity));
            breakdown.put("bedCapacity", round(100 * 0.25 * beds));
            breakdown.put("icuCapacity", round(100 * 0.15 * icu));
            breakdown.put("doctors", round(100 * 0.10 * doctors));
            breakdown.put("bloodBank", round(100 * 0.10 * blood));

            int totalBeds = h.getAvailableBeds() + h.getIcuBeds();
            double occupancy = totalBeds > 0 ? round((1 - h.getAvailableBeds() / (double) totalBeds) * 100) : 100;

            HospitalRecommendationDTO dto = new HospitalRecommendationDTO();
            dto.setId(h.getId());
            dto.setName(h.getName());
            dto.setAddress(h.getAddress());
            dto.setEmergencyContact(h.getEmergencyContact());
            dto.setDistanceKm(round(distance));
            dto.setTravelTimeMinutes(round(distance / TRAVEL_SPEED_KMH * 60));
            dto.setOccupancyPercent(occupancy);
            dto.setAvailableBeds(h.getAvailableBeds());
            dto.setIcuBeds(h.getIcuBeds());
            dto.setDoctorsAvailable(h.getDoctorsAvailable());
            dto.setBloodBank(h.isBloodBank());
            dto.setScore(round(score));
            dto.setScoreBreakdown(breakdown);
            dto.setMatchReason(reason(h, proximity, distance));
            result.add(dto);
        }
        result.sort(Comparator.comparingDouble(HospitalRecommendationDTO::getScore).reversed());
        return result.stream().limit(Math.max(0, limit)).toList();
    }

    private String reason(Hospital h, double proximity, double distance) {
        List<String> parts = new ArrayList<>();
        parts.add(String.format("%.1f km away (%.0f%% proximity)", distance, proximity * 100));
        parts.add(h.getAvailableBeds() + " beds free");
        if (h.getIcuBeds() > 0) parts.add(h.getIcuBeds() + " ICU");
        if (h.getDoctorsAvailable() > 0) parts.add(h.getDoctorsAvailable() + " doctors");
        if (h.isBloodBank()) parts.add("blood bank");
        return String.join(", ", parts);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
