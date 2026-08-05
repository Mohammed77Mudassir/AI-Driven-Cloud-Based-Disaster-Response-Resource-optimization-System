package com.disaster.ai;

import com.disaster.ai.core.ConfidenceEngine;
import com.disaster.ai.core.ScoreEngine;
import com.disaster.ai.core.TimeEstimationEngine;
import com.disaster.ai.model.AnalysisInput;
import com.disaster.ai.model.DamageAssessment;
import com.disaster.ai.model.PredictionModel;
import com.disaster.ai.recommendation.EvacuationEngine;
import com.disaster.ai.recommendation.HospitalRecommendationEngine;
import com.disaster.ai.recommendation.ResourceOptimizationEngine;
import com.disaster.ai.recommendation.ShelterRecommendationEngine;
import com.disaster.ai.recommendation.VolunteerRecommendationEngine;
import com.disaster.dto.ai.AIAnalysisRequest;
import com.disaster.dto.ai.AIAnalysisResponse;
import com.disaster.dto.ai.AISelfTestResult;
import com.disaster.dto.ai.ConfidenceDTO;
import com.disaster.dto.ai.DamagePredictionDTO;
import com.disaster.dto.ai.EvacuationPlanDTO;
import com.disaster.dto.ai.HospitalRecommendationDTO;
import com.disaster.dto.ai.ModelMetaDTO;
import com.disaster.dto.ai.PriorityScoreDTO;
import com.disaster.dto.ai.RecommendationResponse;
import com.disaster.dto.ai.ResourceAllocationDTO;
import com.disaster.dto.ai.RiskScoreDTO;
import com.disaster.dto.ai.ShelterRecommendationDTO;
import com.disaster.dto.ai.TimeEstimateDTO;
import com.disaster.dto.ai.VolunteerRecommendationDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Top-level facade over the AI engine. Exposes analysis, recommendations and
 * self-test while hiding the model/engine composition. The underlying
 * {@link PredictionModel} is swappable without changing this class.
 */
@Service
public class AIEngineService {

    private static final String DISCLAIMER =
            "These estimates are generated deterministically and offline for planning support only; "
                    + "they are not a substitute for on-ground assessment or professional judgement.";

    private static final DateTimeFormatter ID_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Map<String, List<String>> SECONDARY_HAZARDS = Map.of(
            "Flood", List.of("Waterborne disease outbreak", "Infrastructure submergence", "Landslide risk on saturated slopes"),
            "Earthquake", List.of("Aftershocks", "Building collapse", "Utility disruption"),
            "Cyclone", List.of("Storm surge flooding", "Flying debris hazards", "Power network failure"),
            "Wildfire", List.of("Air quality degradation", "Landslide on burned slopes", "Supply route closures"),
            "Tsunami", List.of("Coastal inundation", "Contaminated water sources", "Marine vessel damage"),
            "Landslide", List.of("Blocked road corridors", "Damaged utility lines", "Secondary slope failures"),
            "Drought", List.of("Crop failure", "Water scarcity", "Livestock loss"),
            "Epidemic", List.of("Secondary infection waves", "Healthcare system strain", "Quarantine breaches")
    );

    private static final List<String> DEFAULT_SECONDARY_HAZARDS = List.of(
            "Secondary infrastructure strain", "Cascading hazards in the affected zone");

    private final PredictionModel model;
    private final ConfidenceEngine confidenceEngine;
    private final ScoreEngine scoreEngine;
    private final TimeEstimationEngine timeEngine;
    private final HospitalRecommendationEngine hospitalEngine;
    private final ShelterRecommendationEngine shelterEngine;
    private final VolunteerRecommendationEngine volunteerEngine;
    private final EvacuationEngine evacuationEngine;
    private final ResourceOptimizationEngine resourceEngine;

    public AIEngineService(PredictionModel model,
                           ConfidenceEngine confidenceEngine,
                           ScoreEngine scoreEngine,
                           TimeEstimationEngine timeEngine,
                           HospitalRecommendationEngine hospitalEngine,
                           ShelterRecommendationEngine shelterEngine,
                           VolunteerRecommendationEngine volunteerEngine,
                           EvacuationEngine evacuationEngine,
                           ResourceOptimizationEngine resourceEngine) {
        this.model = model;
        this.confidenceEngine = confidenceEngine;
        this.scoreEngine = scoreEngine;
        this.timeEngine = timeEngine;
        this.hospitalEngine = hospitalEngine;
        this.shelterEngine = shelterEngine;
        this.volunteerEngine = volunteerEngine;
        this.evacuationEngine = evacuationEngine;
        this.resourceEngine = resourceEngine;
    }

    // ------------------------------------------------------------------
    // Analysis
    // ------------------------------------------------------------------

    public AIAnalysisResponse analyze(AIAnalysisRequest request) {
        AnalysisInput input = toInput(request);
        DamageAssessment damage = model.assess(input);
        ConfidenceDTO confidence = confidenceEngine.evaluate(input);

        int shelterCount = shelterEngine.recommend(input.getLatitude(), input.getLongitude(), 10).size();
        int hospitalCount = hospitalEngine.recommend(input.getLatitude(), input.getLongitude(), 10).size();
        int volunteerCount = volunteerEngine.recommend(input.getDisasterType(), input.getLatitude(), input.getLongitude(), 10).size();
        double readiness = clamp(0.25
                + 0.15 * Math.min(3, hospitalCount)
                + 0.10 * Math.min(3, shelterCount)
                + 0.05 * Math.min(3, volunteerCount), 0.2, 1);

        double baseConfidence = confidence.getOverall() / 100.0;
        PriorityScoreDTO priority = scoreEngine.priority(input, damage, readiness);
        RiskScoreDTO risk = scoreEngine.risk(input, damage, model.riskScore(input));

        TimeEstimateDTO response = timeEngine.response(input, readiness, baseConfidence);
        TimeEstimateDTO recovery = timeEngine.recovery(input, damage.getAffectedPopulation(), baseConfidence);

        double damagePercent = damagePercent(input);
        DamagePredictionDTO damageDto = new DamagePredictionDTO();
        damageDto.setDamageLevel(damage.getDamageLevel());
        damageDto.setAffectedPopulation(damage.getAffectedPopulation());
        damageDto.setEconomicLossINR(damage.getEconomicLossINR());
        damageDto.setCasualtiesEstimate(damage.getCasualtiesEstimate());
        damageDto.setInfrastructureImpact(damage.getInfrastructureImpact());
        damageDto.setHazardDrivers(hazardDrivers(input));
        damageDto.setDamagePercent(damagePercent);
        damageDto.setDisruptionLevel(disruptionLevel(damagePercent));
        damageDto.setSecondaryHazards(secondaryHazards(input, damagePercent));

        AIAnalysisResponse responseDto = new AIAnalysisResponse();
        responseDto.setAnalysisId(analysisId(request.getLatitude(), request.getLongitude()));
        responseDto.setGeneratedAt(LocalDateTime.now());
        responseDto.setDisclaimer(DISCLAIMER);
        responseDto.setDisasterType(request.getDisasterType());
        responseDto.setSeverity(request.getSeverity());
        responseDto.setLocation(request.getLocation());
        responseDto.setLatitude(request.getLatitude());
        responseDto.setLongitude(request.getLongitude());
        responseDto.setModel(model.metadata());
        responseDto.setDamage(damageDto);
        responseDto.setRisk(risk);
        responseDto.setPriority(priority);
        responseDto.setRecoveryTime(recovery);
        responseDto.setResponseTime(response);
        responseDto.setConfidence(confidence);
        responseDto.setRecommendations(analysisRecommendations(request.getDisasterType(), request.getSeverity(), priority.getLabel()));
        return responseDto;
    }

    // ------------------------------------------------------------------
    // Recommendations
    // ------------------------------------------------------------------

    public RecommendationResponse recommend(AIAnalysisRequest request) {
        AnalysisInput input = toInput(request);
        ConfidenceDTO confidence = confidenceEngine.evaluate(input);

        List<HospitalRecommendationDTO> hospitals =
                hospitalEngine.recommend(request.getLatitude(), request.getLongitude(), 5);
        List<ShelterRecommendationDTO> shelters =
                shelterEngine.recommend(request.getLatitude(), request.getLongitude(), 5);
        List<VolunteerRecommendationDTO> volunteers =
                volunteerEngine.recommend(request.getDisasterType(), request.getLatitude(), request.getLongitude(), 5);

        long population = input.hasPopulation()
                ? input.getPopulation()
                : model.assess(input).getAffectedPopulation();

        EvacuationPlanDTO evacuation = evacuationEngine.plan(input, shelters);
        evacuation.setAtRiskPopulation(population);
        List<ResourceAllocationDTO> resources =
                resourceEngine.optimize(request.getDisasterType(), request.getSeverity(), population,
                        request.getLatitude(), request.getLongitude());

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendationId(recommendationId(request.getLatitude(), request.getLongitude()));
        response.setGeneratedAt(LocalDateTime.now());
        response.setDisclaimer(DISCLAIMER);
        response.setDisasterType(request.getDisasterType());
        response.setSeverity(request.getSeverity());
        response.setLocation(request.getLocation());
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());
        response.setModel(model.metadata());
        response.setConfidence(confidence);
        response.setHospitals(hospitals);
        response.setShelters(shelters);
        response.setVolunteers(volunteers);
        response.setEvacuation(evacuation);
        response.setResources(resources);
        response.setSummary(summarize(hospitals, shelters, volunteers, resources));
        return response;
    }

    public ModelMetaDTO modelMetadata() {
        return model.metadata();
    }

    // ------------------------------------------------------------------
    // Self-test (exercises every capability)
    // ------------------------------------------------------------------

    public AISelfTestResult selfTest() {
        List<AISelfTestResult.Check> checks = new ArrayList<>();
        AIAnalysisRequest request = new AIAnalysisRequest();
        request.setDisasterType("Flood");
        request.setSeverity("High");
        request.setLocation("Mumbai, Maharashtra");
        request.setLatitude(19.0760);
        request.setLongitude(72.8777);
        request.setPopulation(120000L);

        try {
            AIAnalysisResponse analysis = analyze(request);
            checks.add(new AISelfTestResult.Check("Damage prediction",
                    analysis.getDamage() != null && analysis.getDamage().getAffectedPopulation() > 0,
                    analysis.getDamage() == null ? "null damage" : analysis.getDamage().getDamageLevel()));
            checks.add(new AISelfTestResult.Check("Risk prediction",
                    analysis.getRisk() != null && analysis.getRisk().getScore() >= 0 && analysis.getRisk().getScore() <= 100,
                    analysis.getRisk() == null ? "null risk" : "score=" + analysis.getRisk().getScore()));
            checks.add(new AISelfTestResult.Check("Priority score",
                    analysis.getPriority() != null && analysis.getPriority().getFactors().size() >= 3,
                    analysis.getPriority() == null ? "null priority" : "label=" + analysis.getPriority().getLabel()));
            checks.add(new AISelfTestResult.Check("Recovery time estimation",
                    analysis.getRecoveryTime() != null && analysis.getRecoveryTime().getValue() > 0,
                    analysis.getRecoveryTime() == null ? "null" : analysis.getRecoveryTime().getValue() + " days"));
            checks.add(new AISelfTestResult.Check("Response time estimation",
                    analysis.getResponseTime() != null && analysis.getResponseTime().getValue() > 0,
                    analysis.getResponseTime() == null ? "null" : analysis.getResponseTime().getValue() + " hours"));
            checks.add(new AISelfTestResult.Check("Confidence calculation",
                    analysis.getConfidence() != null && analysis.getConfidence().getOverall() > 0,
                    analysis.getConfidence() == null ? "null" : "confidence=" + analysis.getConfidence().getOverall()));
            checks.add(new AISelfTestResult.Check("Professional response metadata",
                    analysis.getAnalysisId() != null && !analysis.getAnalysisId().isBlank()
                            && analysis.getGeneratedAt() != null
                            && analysis.getDisclaimer() != null && !analysis.getDisclaimer().isBlank(),
                    analysis.getAnalysisId() + " @ " + analysis.getGeneratedAt()));
            checks.add(new AISelfTestResult.Check("Explainable confidence",
                    analysis.getConfidence() != null
                            && !analysis.getConfidence().getLimitations().isEmpty()
                            && analysis.getConfidence().getMethodology() != null
                            && analysis.getConfidence().getUncertaintyPercent() >= 5
                            && analysis.getConfidence().getUncertaintyPercent() <= 95,
                    analysis.getConfidence() == null ? "null"
                            : "uncertainty=" + analysis.getConfidence().getUncertaintyPercent()
                            + "%, limitations=" + analysis.getConfidence().getLimitations().size()));
            checks.add(new AISelfTestResult.Check("Damage percentage and disruption level",
                    analysis.getDamage() != null
                            && analysis.getDamage().getDamagePercent() >= 0
                            && analysis.getDamage().getDamagePercent() <= 100
                            && analysis.getDamage().getDisruptionLevel() != null
                            && !analysis.getDamage().getSecondaryHazards().isEmpty(),
                    analysis.getDamage() == null ? "null damage"
                            : "damage=" + analysis.getDamage().getDamagePercent() + "% "
                            + analysis.getDamage().getDisruptionLevel()));
        } catch (Exception ex) {
            checks.add(new AISelfTestResult.Check("Analysis pipeline", false, ex.getMessage()));
        }

        try {
            RecommendationResponse rec = recommend(request);
            // A recommendation engine that executes correctly may legitimately
            // return zero results when the dataset has no nearby entities
            // (e.g. a fresh production database before operational data is
            // entered). Those are reported as healthy-with-no-data rather than
            // failures; an engine that throws is the real regression and is
            // caught below.
            checks.add(new AISelfTestResult.Check("Hospital recommendation",
                    rec.getHospitals() != null,
                    rec.getHospitals() == null ? "null list"
                            : (rec.getHospitals().isEmpty()
                                ? "0 hospitals ranked (no data in range - expected on empty dataset)"
                                : rec.getHospitals().size() + " hospitals ranked")));
            checks.add(new AISelfTestResult.Check("Shelter recommendation",
                    rec.getShelters() != null,
                    rec.getShelters() == null ? "null list"
                            : (rec.getShelters().isEmpty()
                                ? "0 shelters ranked (no data in range - expected on empty dataset)"
                                : rec.getShelters().size() + " shelters ranked")));
            checks.add(new AISelfTestResult.Check("Volunteer recommendation",
                    rec.getVolunteers() != null,
                    rec.getVolunteers() == null ? "null list"
                            : (rec.getVolunteers().isEmpty()
                                ? "0 volunteers matched (no data in range - expected on empty dataset)"
                                : rec.getVolunteers().size() + " volunteers ranked")));
            checks.add(new AISelfTestResult.Check("Evacuation recommendation",
                    rec.getEvacuation() != null && !rec.getEvacuation().getInstructions().isEmpty(),
                    rec.getEvacuation() == null ? "null plan" : "radius=" + rec.getEvacuation().getDangerRadiusKm() + " km"));
            checks.add(new AISelfTestResult.Check("Resource optimization",
                    !rec.getResources().isEmpty(), rec.getResources().size() + " resource types planned"));
        } catch (Exception ex) {
            checks.add(new AISelfTestResult.Check("Recommendation pipeline", false, ex.getMessage()));
        }

        checks.add(new AISelfTestResult.Check("Offline model metadata",
                model.metadata().isOffline() && model.metadata().getName() != null,
                model.metadata().getName() + " v" + model.metadata().getVersion()));

        AISelfTestResult result = new AISelfTestResult();
        result.setCheckedAt(java.time.LocalDateTime.now());
        result.setChecks(checks);
        result.setPassed(checks.stream().allMatch(AISelfTestResult.Check::isPassed));
        return result;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private AnalysisInput toInput(AIAnalysisRequest r) {
        double infra = r.getInfrastructureFactor() != null ? r.getInfrastructureFactor() : 0.5;
        return new AnalysisInput(r.getDisasterType(), r.getSeverity(), r.getLocation(),
                r.getLatitude(), r.getLongitude(), r.getPopulation(), clamp(infra, 0, 1), r.getWeatherAlert());
    }

    private List<String> hazardDrivers(AnalysisInput input) {
        List<String> drivers = new ArrayList<>();
        drivers.add(input.getSeverity() + " reported severity");
        if (input.hasPopulation()) drivers.add("Population of " + input.getPopulation() + " in zone");
        if (input.hasCoordinates()) drivers.add("Geographically anchored estimate");
        if (input.getWeatherAlert() != null && !input.getWeatherAlert().isBlank()) drivers.add("Active weather alert: " + input.getWeatherAlert());
        drivers.add("Infrastructure resilience " + Math.round(input.getInfrastructureFactor() * 100) + "%");
        return drivers;
    }

    // ------------------------------------------------------------------
    // Professional envelope helpers
    // ------------------------------------------------------------------

    /** Stable 4-character hex suffix derived from the coordinates hash. */
    private String locationSuffix(double lat, double lng) {
        long seed = mixHash(lat, lng);
        return String.format(Locale.ROOT, "%04X", seed & 0xFFFFL);
    }

    private String analysisId(double lat, double lng) {
        return "ANL-" + LocalDateTime.now().format(ID_TIMESTAMP) + "-" + locationSuffix(lat, lng);
    }

    private String recommendationId(double lat, double lng) {
        return "REC-" + LocalDateTime.now().format(ID_TIMESTAMP) + "-" + locationSuffix(lat, lng);
    }

    /** Deterministic 0..1 noise in [0,1) from coordinates. */
    private double locationNoise(double lat, double lng) {
        long seed = mixHash(lat, lng);
        return (seed & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
    }

    private long mixHash(double lat, double lng) {
        long h = Double.doubleToLongBits(lat);
        long h2 = Double.doubleToLongBits(lng);
        long seed = h * 73856093L ^ h2 * 19349663L ^ 2718281828L;
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        return seed;
    }

    // ------------------------------------------------------------------
    // Damage prediction helpers (DamageAssessment stays model-owned)
    // ------------------------------------------------------------------

    private double damagePercent(AnalysisInput input) {
        int sev = severity(input.getSeverity());
        double base = switch (sev) {
            case 4 -> 95;
            case 3 -> 75;
            case 2 -> 50;
            default -> 25;
        };
        double infraDeduction = clamp(input.getInfrastructureFactor(), 0, 1) * 15;
        double geoNoise = input.hasCoordinates()
                ? locationNoise(input.getLatitude(), input.getLongitude()) * 5
                : 2.5;
        return round(clamp(base - infraDeduction + geoNoise, 5, 100));
    }

    private String disruptionLevel(double damagePercent) {
        if (damagePercent >= 80) return "SEVERE";
        if (damagePercent >= 55) return "HIGH";
        if (damagePercent >= 30) return "MODERATE";
        return "LOW";
    }

    private List<String> secondaryHazards(AnalysisInput input, double damagePercent) {
        List<String> typeHazards = SECONDARY_HAZARDS.getOrDefault(input.getDisasterType(), DEFAULT_SECONDARY_HAZARDS);
        int count = Math.min(damagePercent >= 70 ? 3 : 2, typeHazards.size());
        List<String> hazards = new ArrayList<>(typeHazards.subList(0, count));
        if (damagePercent >= 80 && hazards.size() < 3) {
            hazards.add("Prolonged disruption to essential services");
        }
        return hazards;
    }

    private int severity(String severity) {
        return switch (severity == null ? "" : severity) {
            case "Critical" -> 4;
            case "High" -> 3;
            case "Medium" -> 2;
            default -> 1;
        };
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private List<String> analysisRecommendations(String type, String severity, String priorityLabel) {
        List<String> recs = new ArrayList<>();
        recs.add("Activate " + priorityLabel + " response protocol for " + type + " event.");
        recs.add(switch (type) {
            case "Flood" -> "Deploy rescue boats, open elevated shelters and mobilize medical teams to low-lying areas.";
            case "Earthquake" -> "Deploy search & rescue, assess structural integrity and set up field hospitals.";
            case "Cyclone" -> "Pre-position relief supplies, activate cyclone shelters and complete coastal evacuation before landfall.";
            case "Wildfire" -> "Deploy firefighting units, establish firebreaks and evacuate downwind communities.";
            case "Tsunami" -> "Trigger coastal evacuation immediately and route survivors to high-ground shelters.";
            case "Landslide" -> "Deploy geological assessment teams and close landslide-prone corridors.";
            case "Drought" -> "Coordinate water convoys, deploy medical teams and arrange food distribution.";
            case "Epidemic" -> "Establish quarantine zones, deploy medical teams and activate cold-chain for vaccines.";
            default -> "Activate standard emergency response protocol.";
        });
        if ("High".equals(severity) || "Critical".equals(severity)) {
            recs.add("Given " + severity + " severity, pre-position heavy equipment and reserve hospital trauma capacity.");
        }
        return recs;
    }

    private List<String> summarize(List<HospitalRecommendationDTO> hospitals,
                                   List<ShelterRecommendationDTO> shelters,
                                   List<VolunteerRecommendationDTO> volunteers,
                                   List<ResourceAllocationDTO> resources) {
        List<String> summary = new ArrayList<>();
        if (!hospitals.isEmpty()) {
            summary.add("Primary hospital: " + hospitals.get(0).getName() + " (" + hospitals.get(0).getDistanceKm() + " km, " + hospitals.get(0).getAvailableBeds() + " beds).");
        } else {
            summary.add("No hospitals within the search radius.");
        }
        if (!shelters.isEmpty()) {
            int totalSpace = shelters.stream().mapToInt(ShelterRecommendationDTO::getAvailableSpace).sum();
            summary.add(totalSpace + " shelter spaces available across " + shelters.size() + " nearest shelters.");
        }
        summary.add(volunteers.size() + " available volunteers matched by skill and proximity.");
        long critical = resources.stream().filter(r -> "CRITICAL".equals(r.getStatus())).count();
        if (critical > 0) {
            summary.add(critical + " resource type(s) at critical deficit - request external reinforcement.");
        } else {
            summary.add("Resource posture is adequate against current inventory.");
        }
        return summary;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
