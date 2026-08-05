package com.disaster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for the AI engine covering damage prediction,
 * risk prediction, priority scoring, resource optimization, recovery/response
 * time estimation, hospital/shelter/volunteer/evacuation recommendations,
 * confidence calculations, the self-test endpoint and RBAC enforcement.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AIEngineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String adminToken() throws Exception {
        return login("admin", "admin123");
    }

    private String scenario() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "disasterType", "Flood",
                "severity", "High",
                "location", "Mumbai, Maharashtra",
                "latitude", 19.0760,
                "longitude", 72.8777,
                "population", 120000,
                "infrastructureFactor", 0.6,
                "weatherAlert", "Heavy rainfall warning"));
    }

    /** Coordinate-derived suffix of an analysis ID, e.g. "ANL-20260802232940-F412" -> "F412". */
    private String locationSuffix(String analysisId) {
        return analysisId.substring(analysisId.lastIndexOf('-') + 1);
    }

    // ------------------------------------------------------------------
    // AI analysis pipeline
    // ------------------------------------------------------------------

    @Test
    void analyzeReturnsCompleteProfessionalResponse() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model.name").isNotEmpty())
                .andExpect(jsonPath("$.model.offline").value(true))
                .andExpect(jsonPath("$.damage.damageLevel").isNotEmpty())
                .andExpect(jsonPath("$.damage.affectedPopulation").isNumber())
                .andExpect(jsonPath("$.damage.affectedPopulation", greaterThan(0)))
                .andExpect(jsonPath("$.damage.economicLossINR").isNumber())
                .andExpect(jsonPath("$.damage.casualtiesEstimate").isNumber())
                .andExpect(jsonPath("$.risk.score").isNumber())
                .andExpect(jsonPath("$.risk.level").isNotEmpty())
                .andExpect(jsonPath("$.risk.factors", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.priority.score").isNumber())
                .andExpect(jsonPath("$.priority.label").isNotEmpty())
                .andExpect(jsonPath("$.priority.factors", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.recoveryTime.value").isNumber())
                .andExpect(jsonPath("$.recoveryTime.min").isNumber())
                .andExpect(jsonPath("$.recoveryTime.max").isNumber())
                .andExpect(jsonPath("$.responseTime.value").isNumber())
                .andExpect(jsonPath("$.confidence.overall").isNumber())
                .andExpect(jsonPath("$.confidence.inputCompleteness").isNumber())
                .andExpect(jsonPath("$.confidence.modelCoverage").isNumber())
                .andExpect(jsonPath("$.confidence.historicalBasis").isNumber())
                .andExpect(jsonPath("$.recommendations", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void analyzeIsDeterministic() throws Exception {
        String token = adminToken();
        String body = scenario();

        MvcResult first = mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode a = objectMapper.readTree(first.getResponse().getContentAsString());
        JsonNode b = objectMapper.readTree(second.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals(
                a.get("damage").get("affectedPopulation").asLong(),
                b.get("damage").get("affectedPopulation").asLong());
        org.junit.jupiter.api.Assertions.assertEquals(
                a.get("priority").get("score").asInt(),
                b.get("priority").get("score").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(
                a.get("confidence").get("overall").asDouble(),
                b.get("confidence").get("overall").asDouble());
    }

    @Test
    void analyzeReturnsProfessionalResponseEnvelope() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId", startsWith("ANL-")))
                .andExpect(jsonPath("$.generatedAt").isNotEmpty())
                .andExpect(jsonPath("$.disclaimer").isNotEmpty())
                .andExpect(jsonPath("$.damage.damagePercent", allOf(
                        greaterThanOrEqualTo(0.0), lessThanOrEqualTo(100.0))))
                .andExpect(jsonPath("$.damage.disruptionLevel", is(anyOf(
                        is("LOW"), is("MODERATE"), is("HIGH"), is("SEVERE")))))
                .andExpect(jsonPath("$.damage.secondaryHazards").isNotEmpty())
                .andExpect(jsonPath("$.confidence.limitations", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.confidence.uncertaintyPercent", allOf(
                        greaterThanOrEqualTo(5.0), lessThanOrEqualTo(95.0))))
                .andExpect(jsonPath("$.responseTime.confidence", allOf(
                        greaterThanOrEqualTo(0.0), lessThanOrEqualTo(100.0))))
                .andExpect(jsonPath("$.recoveryTime.confidence", allOf(
                        greaterThanOrEqualTo(0.0), lessThanOrEqualTo(100.0))));
    }

    @Test
    void deterministicNewFields() throws Exception {
        String token = adminToken();
        String body = scenario();

        MvcResult first = mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode a = objectMapper.readTree(first.getResponse().getContentAsString());
        JsonNode b = objectMapper.readTree(second.getResponse().getContentAsString());
        String aId = a.get("analysisId").asText();
        String bId = b.get("analysisId").asText();
        org.junit.jupiter.api.Assertions.assertTrue(aId.startsWith("ANL-"), "analysisId must have the ANL- prefix");
        org.junit.jupiter.api.Assertions.assertTrue(bId.startsWith("ANL-"), "analysisId must have the ANL- prefix");
        org.junit.jupiter.api.Assertions.assertEquals(
                locationSuffix(aId), locationSuffix(bId),
                "analysisId coordinate suffix must be reproducible for identical inputs");
        org.junit.jupiter.api.Assertions.assertEquals(
                a.get("damage").get("damagePercent").asDouble(),
                b.get("damage").get("damagePercent").asDouble(),
                "damagePercent must be deterministic");
    }

    @Test
    void analyzeRejectsInvalidPayloads() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("severity", "High"))))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Recommendation pipeline
    // ------------------------------------------------------------------

    @Test
    void recommendationsReturnHospitalsSheltersVolunteersEvacuationAndResources() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/ai/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitals", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.hospitals[0].score").isNumber())
                .andExpect(jsonPath("$.hospitals[0].distanceKm").isNumber())
                .andExpect(jsonPath("$.shelters", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.shelters[0].availableSpace").isNumber())
                .andExpect(jsonPath("$.volunteers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.volunteers[0].skillMatch").isNumber())
                .andExpect(jsonPath("$.evacuation.dangerRadiusKm").isNumber())
                .andExpect(jsonPath("$.evacuation.evacuationWindow").isNotEmpty())
                .andExpect(jsonPath("$.evacuation.instructions", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.resources", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.resources[*].resourceType", everyItem(isA(String.class))))
                .andExpect(jsonPath("$.summary", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.confidence.overall").isNumber());
    }

    @Test
    void hospitalRecommendationsAreSortedByScore() throws Exception {
        String token = adminToken();
        MvcResult result = mockMvc.perform(post("/api/ai/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode hospitals = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("hospitals");
        for (int i = 1; i < hospitals.size(); i++) {
            double prev = hospitals.get(i - 1).get("score").asDouble();
            double curr = hospitals.get(i).get("score").asDouble();
            org.junit.jupiter.api.Assertions.assertTrue(prev >= curr, "Hospitals must be sorted by score descending");
        }
    }

    @Test
    void recommendResponsesCarryProfessionalMetadataAndExplainableScores() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/ai/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationId", startsWith("REC-")))
                .andExpect(jsonPath("$.generatedAt").isNotEmpty())
                .andExpect(jsonPath("$.hospitals[0].scoreBreakdown").isNotEmpty())
                .andExpect(jsonPath("$.hospitals[0].travelTimeMinutes", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.shelters[0].overflowRisk").isBoolean())
                .andExpect(jsonPath("$.volunteers[0].missionsAttended", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.resources[*].coveragePercent", everyItem(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.evacuation.evacuationRoutes").isNotEmpty())
                .andExpect(jsonPath("$.evacuation.assemblyCapacity", greaterThan(0)));
    }

    // ------------------------------------------------------------------
    // Model metadata + self-test
    // ------------------------------------------------------------------

    @Test
    void modelMetadataIsOffline() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/ai/models")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HeuristicPredictionModel"))
                .andExpect(jsonPath("$.offline").value(true));
    }

    @Test
    void selfTestExercisesEveryEngineAndPasses() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/ai/self-test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.checks", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$.checks[*].passed", everyItem(is(true))));
    }

    // ------------------------------------------------------------------
    // Legacy endpoints remain healthy and enriched
    // ------------------------------------------------------------------

    @Test
    void legacyPredictionEndpointReturnsEnrichedPayload() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/predictions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "disasterType", "Earthquake",
                                "severity", "Critical",
                                "location", "Guwahati, Assam",
                                "latitude", 26.1445,
                                "longitude", 91.7362))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priorityScore").isNumber())
                .andExpect(jsonPath("$.riskScore").isNumber())
                .andExpect(jsonPath("$.confidenceScore").isNumber())
                .andExpect(jsonPath("$.confidenceBreakdown.overall").isNumber())
                .andExpect(jsonPath("$.priorityFactors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.riskFactors", hasSize(greaterThan(0))));
    }

    @Test
    void legacyRecommendationEndpointReturnsAllocations() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/recommendations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "disasterType", "Flood",
                                "severity", "High",
                                "location", "Mumbai, Maharashtra",
                                "population", 50000))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambulances").isNumber())
                .andExpect(jsonPath("$.boats").isNumber())
                .andExpect(jsonPath("$.confidenceScore").isNumber())
                .andExpect(jsonPath("$.allocations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.allocations[0].status").isNotEmpty());
    }

    // ------------------------------------------------------------------
    // Security / RBAC
    // ------------------------------------------------------------------

    @Test
    void unauthenticatedAccessToAiEndpointsIsBlocked() throws Exception {
        mockMvc.perform(post("/api/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenario()))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/ai/self-test"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/ai/models"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void authorizedUsersWithAiViewCanAccessAiEndpoints() throws Exception {
        String token = login("user", "user123");
        mockMvc.perform(get("/api/ai/models")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
