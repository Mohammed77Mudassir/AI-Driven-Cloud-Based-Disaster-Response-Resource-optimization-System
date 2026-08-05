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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Rescue Team Command Center dashboard aggregation
 * endpoint ({@code GET /api/rescue-teams/command-center/dashboard}).
 * Follows the repository convention: tests create their own fixtures, assert on
 * their own ids (never on absolute counts, since the shared database carries
 * seed data from other test classes) and clean up afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CommandCenterIntegrationTest {

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

    private long createTeam(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamName", "Command Center Alpha",
                                "teamLeader", "Ravi Kumar",
                                "location", "Mumbai",
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "memberCount", 0,
                                "maxCapacity", 12,
                                "specialty", "FLOOD_RESCUE",
                                "status", "AVAILABLE"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createDisaster(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "disasterType", "Flood",
                                "description", "Severe flooding near the river bank",
                                "severity", "High",
                                "location", "Mumbai, Maharashtra",
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "priority", "HIGH"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createMission(String token, long teamId, long disasterId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/missions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "River evacuation",
                                "missionType", "EVACUATION",
                                "teamId", teamId,
                                "disasterId", disasterId,
                                "priority", "CRITICAL"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createVehicle(String token, long teamId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "vehicleType", "AMBULANCE",
                                "registrationNumber", "CC-01-TEST-0001",
                                "model", "Ford E-350",
                                "capacity", 4,
                                "fuelLevel", 80,
                                "latitude", 19.0760,
                                "longitude", 72.8777))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createEquipment(String token, long teamId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-equipment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "name", "First Aid Kits",
                                "equipmentType", "MEDICAL",
                                "totalQuantity", 10,
                                "availableQuantity", 10,
                                "deployedQuantity", 0,
                                "inMaintenanceQuantity", 0,
                                "condition", "GOOD"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void commandCenterDashboardAggregatesAllOperationalSections() throws Exception {
        String token = login("admin", "admin123");
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long vehicleId = createVehicle(token, teamId);
        long equipmentId = createEquipment(token, teamId);

        // Assign the vehicle to the mission so the active-mission card reports it
        mockMvc.perform(put("/api/rescue-vehicles/{id}/deploy", vehicleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("missionId", missionId))))
                .andExpect(status().isOk());

        // Advance the mission to IN_PROGRESS so it appears in the active panel
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ASSIGNED"))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rescue-teams/command-center/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty())

                // KPI cards
                .andExpect(jsonPath("$.kpis[*].key", hasItem("TOTAL_TEAMS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("TEAMS_ON_MISSION")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("CRITICAL_MISSIONS")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("VEHICLE_AVAILABILITY")))
                .andExpect(jsonPath("$.kpis[*].key", hasItem("EQUIPMENT_READINESS")))

                // Active missions panel
                .andExpect(jsonPath("$.activeMissions[?(@.missionId == %d)].title", missionId).value("River evacuation"))
                .andExpect(jsonPath("$.activeMissions[?(@.missionId == %d)].status", missionId).value("IN_PROGRESS"))
                .andExpect(jsonPath("$.activeMissions[?(@.missionId == %d)].teamId", missionId).value((int) teamId))
                .andExpect(jsonPath("$.activeMissions[?(@.missionId == %d)].progress", missionId).value(75))
                .andExpect(jsonPath("$.activeMissions[?(@.missionId == %d)].assignedVehicle", missionId).isNotEmpty())

                // Team availability cards
                .andExpect(jsonPath("$.teams[*].teamId", hasItem((int) teamId)))
                .andExpect(jsonPath("$.teams[?(@.teamId == %d)].status", teamId).value("ON_MISSION"))
                .andExpect(jsonPath("$.teams[?(@.teamId == %d)].assignedMissionCode", teamId).isNotEmpty())

                // Response analytics
                .andExpect(jsonPath("$.responseAnalytics.averageResponseMinutes").isNumber())
                .andExpect(jsonPath("$.responseAnalytics.fastestResponseMinutes").isNumber())
                .andExpect(jsonPath("$.responseAnalytics.slowestResponseMinutes").isNumber())
                .andExpect(jsonPath("$.responseAnalytics.completionRate").isNumber())
                .andExpect(jsonPath("$.responseAnalytics.monthlyTrend", hasSize(6)))

                // Equipment readiness
                .andExpect(jsonPath("$.equipmentReadiness[*].teamId", hasItem((int) teamId)))
                .andExpect(jsonPath("$.equipmentReadiness[?(@.teamId == %d)].assigned", teamId).value(10))
                .andExpect(jsonPath("$.equipmentReadiness[?(@.teamId == %d)].readinessPercent", teamId).value(100.0))

                // Vehicle status
                .andExpect(jsonPath("$.vehicles[*].vehicleId", hasItem((int) vehicleId)))
                .andExpect(jsonPath("$.vehicles[?(@.vehicleId == %d)].status", vehicleId).value("DEPLOYED"))
                .andExpect(jsonPath("$.vehicles[?(@.vehicleId == %d)].gpsActive", vehicleId).value(true))

                // Workload
                .andExpect(jsonPath("$.workloads[*].teamId", hasItem((int) teamId)))
                .andExpect(jsonPath("$.workloads[?(@.teamId == %d)].activeMissions", teamId).value(1))
                .andExpect(jsonPath("$.workloads[?(@.teamId == %d)].level", teamId).isNotEmpty())

                // Mission timeline
                .andExpect(jsonPath("$.missionTimelines[?(@.missionId == %d)].milestones[0].label", missionId).value("Mission Created"))
                .andExpect(jsonPath("$.missionTimelines[?(@.missionId == %d)].milestones[4].label", missionId).value("Rescue Started"))
                .andExpect(jsonPath("$.missionTimelines[?(@.missionId == %d)].milestones[4].done", missionId).value(true))
                .andExpect(jsonPath("$.missionTimelines[?(@.missionId == %d)].milestones[5].done", missionId).value(false))

                // Alerts (inactive/overdue team warning should be present for our team)
                .andExpect(jsonPath("$.alerts[?(@.entityId == %d)].category", teamId).value("TEAM_INACTIVE"));

        // Clean up: complete (releases the team) then delete in FK-safe order
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/rescue-vehicles/{id}/return", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/rescue-vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/rescue-equipment/{id}", equipmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/missions/{id}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/disasters/{id}", disasterId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void legacyUserCannotAccessCommandCenter() throws Exception {
        String token = login("user", "user123");
        mockMvc.perform(get("/api/rescue-teams/command-center/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
