package com.disaster.ai.recommendation;

import com.disaster.ai.core.GeoDistance;
import com.disaster.dto.ai.VolunteerRecommendationDTO;
import com.disaster.entity.Volunteer;
import com.disaster.repository.VolunteerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ranks available volunteers by skill match for the disaster type, proximity to
 * the affected zone and prior mission experience. Only volunteers who are
 * currently available are returned.
 */
@Component
public class VolunteerRecommendationEngine {

    private static final double RADIUS_KM = 200;

    /** Average convoy speed in km/h used to derive travel time estimates. */
    private static final double TRAVEL_SPEED_KMH = 40;

    /** Missions threshold beyond which experience is considered saturated. */
    private static final int EXPERIENCE_SATURATION = 10;

    private static final Map<String, List<String>> SKILL_KEYWORDS = Map.of(
            "Flood", List.of("swim", "boat", "rescue", "first aid", "water"),
            "Earthquake", List.of("rescue", "structural", "debris", "first aid", "search"),
            "Cyclone", List.of("rescue", "logistics", "shelter", "evacuation"),
            "Wildfire", List.of("fire", "logistics", "evacuation", "first aid"),
            "Tsunami", List.of("rescue", "boat", "evacuation", "first aid"),
            "Landslide", List.of("rescue", "logistics", "first aid"),
            "Drought", List.of("water", "logistics", "distribution", "nursing"),
            "Epidemic", List.of("medical", "nursing", "hygiene", "quarantine", "first aid")
    );

    private final VolunteerRepository repository;

    public VolunteerRecommendationEngine(VolunteerRepository repository) {
        this.repository = repository;
    }

    public List<VolunteerRecommendationDTO> recommend(String disasterType, double lat, double lng, int limit) {
        List<String> keywords = SKILL_KEYWORDS.getOrDefault(disasterType, List.of("first aid", "rescue"));

        List<VolunteerRecommendationDTO> result = new ArrayList<>();
        for (Volunteer v : repository.findByAvailableTrue()) {
            double distance = GeoDistance.km(lat, lng, v.getLatitude(), v.getLongitude());
            double proximity = GeoDistance.proximity(distance, RADIUS_KM);
            double skillMatch = skillMatch(v.getSkills(), keywords);
            int missionsAttended = v.isAttended() ? 1 : 0;
            double experience = Math.min(1, missionsAttended / (double) EXPERIENCE_SATURATION);

            double score = 100 * (0.50 * skillMatch + 0.30 * proximity + 0.20 * experience);

            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("skillMatch", round(100 * 0.50 * skillMatch));
            breakdown.put("proximity", round(100 * 0.30 * proximity));
            breakdown.put("experience", round(100 * 0.20 * experience));

            VolunteerRecommendationDTO dto = new VolunteerRecommendationDTO();
            dto.setId(v.getId());
            dto.setName(v.getName());
            dto.setPhone(v.getPhone());
            dto.setSkills(v.getSkills());
            dto.setDistanceKm(round(distance));
            dto.setTravelTimeMinutes(round(distance / TRAVEL_SPEED_KMH * 60));
            dto.setSkillMatch(round(skillMatch * 100));
            dto.setMissionsAttended(missionsAttended);
            dto.setAvailable(v.isAvailable());
            dto.setScore(round(score));
            dto.setScoreBreakdown(breakdown);
            dto.setMatchReason(reason(v, missionsAttended));
            result.add(dto);
        }
        result.sort(Comparator.comparingDouble(VolunteerRecommendationDTO::getScore).reversed());
        return result.stream().limit(Math.max(0, limit)).toList();
    }

    private double skillMatch(String skills, List<String> keywords) {
        if (skills == null || skills.isBlank()) return 0.2;
        String lower = skills.toLowerCase(Locale.ROOT);
        long hits = keywords.stream().filter(k -> lower.contains(k)).count();
        return Math.max(0.2, Math.min(1.0, hits / (double) keywords.size() + 0.1));
    }

    private String reason(Volunteer v, int missionsAttended) {
        String base = v.getSkills() == null || v.getSkills().isBlank()
                ? "No skills recorded; considered for general duties"
                : "Skills: " + v.getSkills();
        return base + (missionsAttended > 0
                ? "; " + missionsAttended + " mission(s) attended"
                : "; newcomer - no prior missions recorded");
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
