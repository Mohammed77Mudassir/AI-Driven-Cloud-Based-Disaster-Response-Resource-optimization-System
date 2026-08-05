package com.disaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Hospital Operations dashboard aggregation endpoint
 * ({@code GET /api/hospitals/operations/dashboard}). Follows the repository
 * convention: tests create their own fixtures, assert on their own ids (never
 * on absolute counts, since the shared database carries seed data from other
 * test classes) and clean up afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HospitalOperationsIntegrationTest {

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

    private long createHospital(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/hospitals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Operations Test Hospital",
                                "availableBeds", 120,
                                "icuBeds", 30,
                                "doctorsAvailable", 45,
                                "emergencyContact", "022-0000000",
                                "bloodBank", true,
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "address", "Test City"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void hospitalOperationsDashboardAggregatesOperationalSections() throws Exception {
        String token = login("admin", "admin123");
        long hospitalId = createHospital(token);

        mockMvc.perform(get("/api/hospitals/operations/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty())

                // KPI cards
                .andExpect(jsonPath("$.kpis[*].key", hasItem("TOTAL_HOSPITALS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("AVAILABLE_BEDS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("ICU_BEDS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("DOCTORS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("AMBULANCES")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("BLOOD_UNITS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("EMERGENCY_CAPACITY")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("UTILIZATION")))

                // Per-hospital derived metrics
                .andExpect(jsonPath("$.hospitals[*].id", hasItem((int) hospitalId)))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].totalBeds", hospitalId).value(150))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].occupiedBeds", hospitalId).value(30))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].bedUtilizationPercent", hospitalId).value(20.0))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].icuOccupancyPercent", hospitalId).value(20.0))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].status", hospitalId).value("READY"))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].emergencyCapacityScore", hospitalId).value(100.0))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].emergencyCapacityLevel", hospitalId).value("EXCELLENT"))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].patientLoad", hospitalId).value(20))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].estimatedWaitingTimeMinutes", hospitalId).value(17))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].doctorAvailabilityPercent", hospitalId).value(75.0))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].bloodStockStatus", hospitalId).value("ADEQUATE"))
                .andExpect(jsonPath("$.hospitals[?(@.id == %d)].bloodGroups[*]", hospitalId).value(hasSize(8)))

                // Fleet and staffing summaries
                .andExpect(jsonPath("$.doctorSummary.totalDoctors").isNumber())
                .andExpect(jsonPath("$.doctorSummary.availabilityPercent").isNumber())
                .andExpect(jsonPath("$.ambulanceSummary.total").isNumber())
                .andExpect(jsonPath("$.ambulanceSummary.availabilityPercent").isNumber())

                // Blood inventory roll-up
                .andExpect(jsonPath("$.bloodGroups", hasSize(8)))
                .andExpect(jsonPath("$.bloodGroups[*].group", hasItem("O-")))
                .andExpect(jsonPath("$.totalBloodUnits").isNumber());

        // Clean up
        mockMvc.perform(delete("/api/hospitals/{id}", hospitalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void legacyUserCannotAccessHospitalOperations() throws Exception {
        String token = login("user", "user123");
        mockMvc.perform(get("/api/hospitals/operations/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
