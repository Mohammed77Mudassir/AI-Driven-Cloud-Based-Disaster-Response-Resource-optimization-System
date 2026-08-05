package com.disaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Shelter Operations dashboard aggregation endpoint
 * ({@code GET /api/shelters/operations/dashboard}). Follows the repository
 * convention: tests create their own fixtures, assert on their own ids (never
 * on absolute counts, since the shared database carries seed data from other
 * test classes) and clean up afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShelterOperationsIntegrationTest {

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
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createShelter(String token, String name, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> shelterBody(String name, int capacity, int occupancy,
                                            boolean food, boolean water, int medicalKits, boolean power) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("capacity", capacity);
        body.put("occupancy", occupancy);
        body.put("foodAvailable", food);
        body.put("waterAvailable", water);
        body.put("medicalKits", medicalKits);
        body.put("powerAvailable", power);
        body.put("contact", "9876500000");
        body.put("latitude", 19.0760);
        body.put("longitude", 72.8777);
        body.put("address", "Ops Test City");
        return body;
    }

    @Test
    void shelterOperationsDashboardAggregatesOperationalSections() throws Exception {
        String token = login("admin", "admin123");

        // Well-provisioned shelter: derived metrics are deterministic except
        // for a handful of documented estimate fields, asserted by range.
        long healthyId = createShelter(token, "Ops Healthy Shelter", shelterBody(
                "Ops Healthy Shelter", 200, 40, true, true, 50, true));

        // Critical shelter: guarantees a deterministic alert set regardless of
        // the id-dependent generator/internet variance.
        long criticalId = createShelter(token, "Ops Critical Shelter", shelterBody(
                "Ops Critical Shelter", 100, 100, false, false, 2, false));

        mockMvc.perform(get("/api/shelters/operations/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty())

                // KPI cards
                .andExpect(jsonPath("$.kpis[*].key", hasItem("TOTAL_SHELTERS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("TOTAL_CAPACITY")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("CURRENT_OCCUPANCY")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("AVAILABLE_SPACE")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("OCCUPANCY_RATE")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("SHELTERS_AVAILABLE")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("SHELTERS_NEAR_FULL")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("FULL_SHELTERS")))

                // Healthy shelter: deterministic metrics
                .andExpect(jsonPath("$.shelters[*].id", hasItem((int) healthyId)))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].occupancyPercent", healthyId).value(20.0))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].status", healthyId).value("READY"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].availableSpace", healthyId).value(160))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].dailyFoodConsumption", healthyId).value(80))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].foodStatus", healthyId).value("SUFFICIENT"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].foodPacks", healthyId).value(hasItem(greaterThanOrEqualTo(1200))))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].foodPacks", healthyId).value(hasItem(lessThanOrEqualTo(1300))))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].dailyWaterRequirement", healthyId).value(160))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].waterStatus", healthyId).value("OK"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].totalMedicalKits", healthyId).value(50))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].medicalKitsInUse", healthyId).value(4))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].availableMedicalKits", healthyId).value(46))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].minRequiredMedicalKits", healthyId).value(20))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].medicalReadinessPercent", healthyId).value(100.0))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].powerStatus", healthyId).value("AVAILABLE"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].gridAvailable", healthyId).value(true))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].readinessLevel", healthyId).value("EXCELLENT"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].readinessScore", healthyId).value(hasItem(greaterThanOrEqualTo(80.0))))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].forecastTrend", healthyId).value("RISING"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].estimatedDaysToFull", healthyId).value(hasItem(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].estimatedDaysToFull", healthyId).value(hasItem(lessThanOrEqualTo(60))))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].estimatedFullDate", healthyId).isNotEmpty())

                // Critical shelter: deterministic alert posture
                .andExpect(jsonPath("$.shelters[*].id", hasItem((int) criticalId)))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].status", criticalId).value("FULL"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].foodStatus", criticalId).value("CRITICAL"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].waterStatus", criticalId).value("CRITICAL"))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].medicalReadinessPercent", criticalId).value(20.0))
                .andExpect(jsonPath("$.shelters[?(@.id == %d)].readinessLevel", criticalId).value("CRITICAL"))

                // Alerts (deterministic categories raised by the critical fixture)
                .andExpect(jsonPath("$.alerts").isArray())
                .andExpect(jsonPath("$.alerts[*].message", hasItem(containsStringIgnoringCase("full"))))
                .andExpect(jsonPath("$.alerts[*].message", hasItem(containsStringIgnoringCase("food"))))
                .andExpect(jsonPath("$.alerts[*].message", hasItem(containsStringIgnoringCase("water"))))
                .andExpect(jsonPath("$.alerts[*].message", hasItem(containsStringIgnoringCase("medical"))))
                .andExpect(jsonPath("$.alerts[*].message", hasItem(containsStringIgnoringCase("communication"))));

        // Clean up
        mockMvc.perform(delete("/api/shelters/{id}", healthyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/shelters/{id}", criticalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void legacyUserCannotAccessShelterOperations() throws Exception {
        String token = login("user", "user123");
        mockMvc.perform(get("/api/shelters/operations/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
