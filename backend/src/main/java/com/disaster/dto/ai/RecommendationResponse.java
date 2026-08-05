package com.disaster.dto.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Professional recommendation envelope combining hospitals, shelters,
 * volunteers, evacuation plan and resource optimization, each scored against
 * live inventory data.
 */
public class RecommendationResponse {

    private String recommendationId;
    private LocalDateTime generatedAt;
    private String disclaimer;

    private String disasterType;
    private String severity;
    private String location;
    private double latitude;
    private double longitude;

    private ModelMetaDTO model;
    private ConfidenceDTO confidence;
    private List<HospitalRecommendationDTO> hospitals = new ArrayList<>();
    private List<ShelterRecommendationDTO> shelters = new ArrayList<>();
    private List<VolunteerRecommendationDTO> volunteers = new ArrayList<>();
    private EvacuationPlanDTO evacuation;
    private List<ResourceAllocationDTO> resources = new ArrayList<>();
    private List<String> summary = new ArrayList<>();

    public RecommendationResponse() {}

    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
    public String getDisasterType() { return disasterType; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public ModelMetaDTO getModel() { return model; }
    public void setModel(ModelMetaDTO model) { this.model = model; }
    public ConfidenceDTO getConfidence() { return confidence; }
    public void setConfidence(ConfidenceDTO confidence) { this.confidence = confidence; }
    public List<HospitalRecommendationDTO> getHospitals() { return hospitals; }
    public void setHospitals(List<HospitalRecommendationDTO> hospitals) { this.hospitals = hospitals; }
    public List<ShelterRecommendationDTO> getShelters() { return shelters; }
    public void setShelters(List<ShelterRecommendationDTO> shelters) { this.shelters = shelters; }
    public List<VolunteerRecommendationDTO> getVolunteers() { return volunteers; }
    public void setVolunteers(List<VolunteerRecommendationDTO> volunteers) { this.volunteers = volunteers; }
    public EvacuationPlanDTO getEvacuation() { return evacuation; }
    public void setEvacuation(EvacuationPlanDTO evacuation) { this.evacuation = evacuation; }
    public List<ResourceAllocationDTO> getResources() { return resources; }
    public void setResources(List<ResourceAllocationDTO> resources) { this.resources = resources; }
    public List<String> getSummary() { return summary; }
    public void setSummary(List<String> summary) { this.summary = summary; }
}
