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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for the Rescue Team Coordination module
 * covering team/member management, leader assignment, vehicle and equipment
 * lifecycle, missions with status workflow + history + resource allocation,
 * shift scheduling / duty roster, live tracking, resource inventory
 * (deploy / return / maintenance) and RBAC enforcement.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RescueTeamCoordinationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

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

    private String userToken() throws Exception {
        return login("user", "user123");
    }

    private Map<String, Object> mutableMap() {
        return new LinkedHashMap<>();
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

    private long createTeam(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamName", "Alpha Rescue Unit",
                                "teamLeader", "Ravi Kumar",
                                "contactNumber", "+911234567890",
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

    private long createVehicle(String token, long teamId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "vehicleType", "AMBULANCE",
                                "registrationNumber", "MH-01-AB-1234",
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

    private long createMember(String token, long teamId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-teams/{id}/members", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Sita Sharma",
                                "role", "Medic",
                                "speciality", "Trauma care",
                                "phone", "+911122334455",
                                "skills", "CPR, Advanced First Aid",
                                "certifications", "EMT-B"))))
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
                                "description", "Evacuate residents near the river bank",
                                "teamId", teamId,
                                "disasterId", disasterId,
                                "priority", "HIGH",
                                "instructions", "Use rescue boats from the north gate"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.missionCode").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createResource(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resourceType", "BOAT",
                                "quantity", 10,
                                "totalQuantity", 10,
                                "location", "Mumbai Dockyard",
                                "latitude", 19.0,
                                "longitude", 72.9,
                                "condition", "GOOD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalQuantity").value(10))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    // ------------------------------------------------------------------
    // Teams, members and leader assignment
    // ------------------------------------------------------------------

    @Test
    void teamCrudLeaderAssignmentAndMemberManagement() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);

        // Read single
        mockMvc.perform(get("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Alpha Rescue Unit"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        // Add members
        long m1 = createMember(token, teamId);
        long m2 = createMember(token, teamId);
        mockMvc.perform(get("/api/rescue-teams/{id}/members", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Assign leader
        mockMvc.perform(put("/api/rescue-teams/members/{memberId}/leader", m1)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("teamId", teamId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamLeader").value("Sita Sharma"));

        // Members list reflects leader flag
        mockMvc.perform(get("/api/rescue-teams/{id}/members", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leader").value(true));

        // Update member
        mockMvc.perform(put("/api/rescue-teams/members/{memberId}", m2)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "Driver"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("Driver"));

        // Team availability
        mockMvc.perform(get("/api/rescue-teams/{id}/availability", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(2))
                .andExpect(jsonPath("$.availableMembers").value(2))
                .andExpect(jsonPath("$.deployable").value(true));

        // Update team
        mockMvc.perform(put("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("teamName", "Alpha Rescue Unit Beta"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Alpha Rescue Unit Beta"));

        // Invalid status rejected
        mockMvc.perform(put("/api/rescue-teams/{id}/status", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "NOT_A_STATUS"))))
                .andExpect(status().isBadRequest());

        // Delete
        mockMvc.perform(delete("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Vehicles
    // ------------------------------------------------------------------

    @Test
    void vehicleCrudAndLifecycle() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long vehicleId = createVehicle(token, teamId);

        // Read
        mockMvc.perform(get("/api/rescue-vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.teamId").value(teamId));

        // Deploy requires a mission -> rejected without missionId
        mockMvc.perform(put("/api/rescue-vehicles/{id}/deploy", vehicleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());

        // Deploy to mission
        mockMvc.perform(put("/api/rescue-vehicles/{id}/deploy", vehicleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("missionId", missionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPLOYED"))
                .andExpect(jsonPath("$.assignedMissionId").value(missionId))
                .andExpect(jsonPath("$.deployedAt").isNotEmpty());

        // Deploy again -> rejected
        mockMvc.perform(put("/api/rescue-vehicles/{id}/deploy", vehicleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("missionId", missionId))))
                .andExpect(status().isBadRequest());

        // Vehicle listed under mission
        mockMvc.perform(get("/api/rescue-vehicles/mission/{missionId}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) vehicleId)));

        // Mission history records the allocation
        mockMvc.perform(get("/api/missions/{id}/events", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventType", hasItem("RESOURCE_ALLOCATED")));

        // Return
        mockMvc.perform(put("/api/rescue-vehicles/{id}/return", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.assignedMissionId").value(nullValue()))
                .andExpect(jsonPath("$.returnedAt").isNotEmpty());

        // Maintenance lifecycle
        mockMvc.perform(put("/api/rescue-vehicles/{id}/maintenance", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_MAINTENANCE"));

        mockMvc.perform(put("/api/rescue-vehicles/{id}/maintenance/complete", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.lastMaintainedAt").isNotEmpty());

        // Filter by status
        mockMvc.perform(get("/api/rescue-vehicles/status/AVAILABLE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) vehicleId)));

        // Delete
        mockMvc.perform(delete("/api/rescue-vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------
    // Equipment
    // ------------------------------------------------------------------

    @Test
    void equipmentCrudAndQuantityLifecycle() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long equipmentId = createEquipment(token, teamId);

        // Deploy requires a mission -> rejected without missionId
        mockMvc.perform(put("/api/rescue-equipment/{id}/deploy", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 3))))
                .andExpect(status().isBadRequest());

        // Deploy 3 units to mission
        mockMvc.perform(put("/api/rescue-equipment/{id}/deploy", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 3,
                                "missionId", missionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andExpect(jsonPath("$.deployedQuantity").value(3))
                .andExpect(jsonPath("$.assignedMissionId").value(missionId));

        // Deploy more than available -> rejected
        mockMvc.perform(put("/api/rescue-equipment/{id}/deploy", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 8,
                                "missionId", missionId))))
                .andExpect(status().isBadRequest());

        // Equipment listed under mission
        mockMvc.perform(get("/api/rescue-equipment/mission/{missionId}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) equipmentId)));

        // Return 3
        mockMvc.perform(put("/api/rescue-equipment/{id}/return", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.deployedQuantity").value(0))
                .andExpect(jsonPath("$.assignedMissionId").value(nullValue()));

        // Maintenance lifecycle
        mockMvc.perform(put("/api/rescue-equipment/{id}/maintenance", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inMaintenanceQuantity").value(2));

        mockMvc.perform(put("/api/rescue-equipment/{id}/maintenance/complete", equipmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inMaintenanceQuantity").value(0))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.lastMaintainedAt").isNotEmpty());

        // By team
        mockMvc.perform(get("/api/rescue-equipment/team/{teamId}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Delete
        mockMvc.perform(delete("/api/rescue-equipment/{id}", equipmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------
    // Missions
    // ------------------------------------------------------------------

    @Test
    void missionCrudWorkflowAndHistory() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);

        // Read
        mockMvc.perform(get("/api/missions/{id}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.disasterId").value(disasterId));

        // Event history has CREATED
        mockMvc.perform(get("/api/missions/{id}/events", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("CREATED"));

        // Invalid transition: PENDING -> IN_PROGRESS
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isBadRequest());

        // Full lifecycle
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ASSIGNED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Team is now ON_MISSION and availability reports the active mission
        mockMvc.perform(get("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_MISSION"));

        mockMvc.perform(get("/api/rescue-teams/{id}/availability", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deployable").value(false))
                .andExpect(jsonPath("$.activeMissionId").value(missionId));

        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        // Team released back to AVAILABLE
        mockMvc.perform(get("/api/rescue-teams/{id}", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        // COMPLETED is terminal
        mockMvc.perform(put("/api/missions/{id}/status", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isBadRequest());

        // Filter by status
        mockMvc.perform(get("/api/missions/status/COMPLETED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) missionId)));

        // Filter by team + status
        mockMvc.perform(get("/api/missions/team/{teamId}/status/COMPLETED", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) missionId)));

        mockMvc.perform(get("/api/missions/team/{teamId}/status/IN_PROGRESS", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Update mission
        mockMvc.perform(put("/api/missions/{id}", missionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("instructions", "Updated instructions"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructions").value("Updated instructions"));

        // Delete
        mockMvc.perform(delete("/api/missions/{id}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------
    // Resource allocation to missions
    // ------------------------------------------------------------------

    @Test
    void resourceCanBeDeployedAndReturnedToMission() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long resourceId = createResource(token);

        // Deploy 4 units to mission
        mockMvc.perform(put("/api/resources/{id}/deploy", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 4,
                                "missionId", missionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6))
                .andExpect(jsonPath("$.deployedQuantity").value(4))
                .andExpect(jsonPath("$.assignedMissionId").value(missionId));

        // Deploy beyond availability -> rejected
        mockMvc.perform(put("/api/resources/{id}/deploy", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 7,
                                "missionId", missionId))))
                .andExpect(status().isBadRequest());

        // Mission history records the allocation
        mockMvc.perform(get("/api/missions/{id}/events", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("RESOURCE_ALLOCATED"));

        // List by mission
        mockMvc.perform(get("/api/resources/mission/{missionId}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Return 4 units
        mockMvc.perform(put("/api/resources/{id}/return", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.deployedQuantity").value(0));
    }

    // ------------------------------------------------------------------
    // Resource inventory (deploy / return / maintenance)
    // ------------------------------------------------------------------

    @Test
    void resourceInventoryMaintenanceLifecycle() throws Exception {
        String token = adminToken();
        long resourceId = createResource(token);

        // Start maintenance on 2 units
        mockMvc.perform(put("/api/resources/{id}/maintenance", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8))
                .andExpect(jsonPath("$.inMaintenanceQuantity").value(2));

        // Complete maintenance
        mockMvc.perform(put("/api/resources/{id}/maintenance/complete", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.inMaintenanceQuantity").value(0))
                .andExpect(jsonPath("$.lastMaintainedAt").isNotEmpty())
                .andExpect(jsonPath("$.condition").value("GOOD"));

        // Update resource
        mockMvc.perform(put("/api/resources/{id}", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resourceType", "BOAT",
                                "quantity", 10,
                                "totalQuantity", 10,
                                "condition", "FAIR",
                                "location", "Mumbai Dockyard"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("FAIR"));

        // Delete
        mockMvc.perform(delete("/api/resources/{id}", resourceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------
    // Resource movement audit trail
    // ------------------------------------------------------------------

    @Test
    void resourceMovementsRecordDeployReturnAndMaintenance() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long resourceId = createResource(token);

        // Deploy 4 to mission -> DEPLOYED movement
        mockMvc.perform(put("/api/resources/{id}/deploy", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 4,
                                "missionId", missionId))))
                .andExpect(status().isOk());

        // Return 1 -> RETURNED movement
        mockMvc.perform(put("/api/resources/{id}/return", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 1))))
                .andExpect(status().isOk());

        // Maintenance start + complete -> movements
        mockMvc.perform(put("/api/resources/{id}/maintenance", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/resources/{id}/maintenance/complete", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isOk());

        // Movements by resource, newest first: MAINTENANCE_COMPLETED,
        // MAINTENANCE_STARTED, RETURNED, DEPLOYED
        mockMvc.perform(get("/api/resources/{id}/movements", resourceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].movementType").value("MAINTENANCE_COMPLETED"))
                .andExpect(jsonPath("$[1].movementType").value("MAINTENANCE_STARTED"))
                .andExpect(jsonPath("$[2].movementType").value("RETURNED"))
                .andExpect(jsonPath("$[3].movementType").value("DEPLOYED"))
                .andExpect(jsonPath("$[3].actor").value("admin"))
                .andExpect(jsonPath("$[3].quantity").value(4))
                .andExpect(jsonPath("$[3].missionId").value(missionId))
                .andExpect(jsonPath("$[3].availableAfter").value(6));

        // All movements include this resource's records
        mockMvc.perform(get("/api/resources/movements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].resourceId", hasItem((int) resourceId)));

        // Movements by mission (deploy + partial return while still assigned)
        mockMvc.perform(get("/api/resources/movements?missionId={missionId}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].movementType", containsInAnyOrder("DEPLOYED", "RETURNED")))
                .andExpect(jsonPath("$[*].resourceId", everyItem(is((int) resourceId))));

        // Movements by type
        mockMvc.perform(get("/api/resources/movements?type=RETURNED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].resourceId", hasItem((int) resourceId)))
                .andExpect(jsonPath("$[*].quantity", hasItem(1)));
    }

    // ------------------------------------------------------------------
    // Shifts / duty roster
    // ------------------------------------------------------------------

    @Test
    void shiftCrudAndDutyRoster() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long memberId = createMember(token, teamId);

        LocalDate today = LocalDate.now();
        String shiftStart = today.atTime(6, 0).toString();
        String shiftEnd = today.atTime(18, 0).toString();

        // Create a shift for today
        MvcResult result = mockMvc.perform(post("/api/shifts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "memberId", memberId,
                                "shiftType", "DAY",
                                "shiftStatus", "SCHEDULED",
                                "shiftStart", shiftStart,
                                "shiftEnd", shiftEnd,
                                "notes", "Morning rotation"))))
                .andExpect(status().isCreated())
                .andReturn();
        long shiftId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Roster for today includes it
        mockMvc.perform(get("/api/shifts/roster")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) shiftId)));

        // Roster for a date range includes it
        mockMvc.perform(get("/api/shifts/roster")
                        .param("from", today.toString())
                        .param("to", today.plusDays(6).toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) shiftId)));

        // Activate
        mockMvc.perform(put("/api/shifts/{id}/status", shiftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftStatus").value("ACTIVE"));

        // Complete
        mockMvc.perform(put("/api/shifts/{id}/status", shiftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk());

        // Completed shifts cannot be mutated
        mockMvc.perform(put("/api/shifts/{id}/status", shiftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isBadRequest());

        // Invalid shift window rejected at creation
        mockMvc.perform(post("/api/shifts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "memberId", memberId,
                                "shiftType", "NIGHT",
                                "shiftStatus", "SCHEDULED"))))
                .andExpect(status().isBadRequest());

        // Delete
        mockMvc.perform(delete("/api/shifts/{id}", shiftId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void resourceFullReturnRetainsMissionOnMovement() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long disasterId = createDisaster(token);
        long missionId = createMission(token, teamId, disasterId);
        long resourceId = createResource(token);

        // Deploy 4 units to mission
        mockMvc.perform(put("/api/resources/{id}/deploy", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "quantity", 4,
                                "missionId", missionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedMissionId").value(missionId));

        // Full return of all 4 units clears the resource assignment
        mockMvc.perform(put("/api/resources/{id}/return", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deployedQuantity").value(0))
                .andExpect(jsonPath("$.assignedMissionId").value(nullValue()));

        // Both movements keep the mission link even though the resource was fully returned
        mockMvc.perform(get("/api/resources/movements?missionId={missionId}", missionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].movementType", containsInAnyOrder("DEPLOYED", "RETURNED")))
                .andExpect(jsonPath("$[*].missionId", everyItem(is((int) missionId))));
    }

    @Test
    void shiftActivationControlsMemberAvailability() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long memberId = createMember(token, teamId);

        // Member starts available
        mockMvc.perform(get("/api/rescue-teams/{id}/availability", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(1))
                .andExpect(jsonPath("$.availableMembers").value(1));

        LocalDate today = LocalDate.now();
        MvcResult result = mockMvc.perform(post("/api/shifts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "memberId", memberId,
                                "shiftType", "DAY",
                                "shiftStatus", "SCHEDULED",
                                "shiftStart", today.atTime(6, 0).toString(),
                                "shiftEnd", today.atTime(18, 0).toString(),
                                "notes", "Availability sync shift"))))
                .andExpect(status().isCreated())
                .andReturn();
        long shiftId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Activating the shift makes the member unavailable
        mockMvc.perform(put("/api/shifts/{id}/status", shiftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftStatus").value("ACTIVE"));

        mockMvc.perform(get("/api/rescue-teams/{id}/availability", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMembers").value(0));

        // Completing the shift frees the member again
        mockMvc.perform(put("/api/shifts/{id}/status", shiftId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shiftStatus").value("COMPLETED"));

        mockMvc.perform(get("/api/rescue-teams/{id}/availability", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMembers").value(1));
    }

    @Test
    void dutyRosterSummaryEndpoint() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        long memberId = createMember(token, teamId);

        LocalDate today = LocalDate.now();
        // Earliest shift of the day so it sorts first in the ascending roster
        MvcResult result = mockMvc.perform(post("/api/shifts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "memberId", memberId,
                                "shiftType", "DAY",
                                "shiftStatus", "SCHEDULED",
                                "shiftStart", today.atTime(0, 1).toString(),
                                "shiftEnd", today.atTime(1, 0).toString(),
                                "notes", "Duty roster summary shift"))))
                .andExpect(status().isCreated())
                .andReturn();
        long shiftId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/shifts/duty-roster")
                        .param("date", today.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.onDuty").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.shifts").isNotEmpty())
                .andExpect(jsonPath("$.shifts[0].id").value(shiftId));
    }

    @Test
    void resourceCreateTracksMaintenanceSchedule() throws Exception {
        String token = adminToken();

        // Creating a usable resource seeds the maintenance schedule automatically
        MvcResult createResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resourceType", "BOAT",
                                "quantity", 10,
                                "totalQuantity", 10,
                                "location", "Mumbai Dockyard",
                                "latitude", 19.0,
                                "longitude", 72.9,
                                "condition", "GOOD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maintenanceDueAt").isNotEmpty())
                .andExpect(jsonPath("$.lastMaintainedAt").isNotEmpty())
                .andReturn();
        long resourceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        LocalDateTime seededDue = objectMapper.treeToValue(created.get("maintenanceDueAt"), LocalDateTime.class);
        assertNotNull(seededDue);
        assertEquals(LocalDate.now().plusMonths(6), seededDue.toLocalDate());

        // Update honors explicit maintenance dates
        LocalDateTime newDue = LocalDateTime.now().plusMonths(9);
        LocalDateTime newLast = LocalDateTime.now();
        MvcResult updateResult = mockMvc.perform(put("/api/resources/{id}", resourceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "maintenanceDueAt", newDue.toString(),
                                "lastMaintainedAt", newLast.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceDueAt").isNotEmpty())
                .andExpect(jsonPath("$.lastMaintainedAt").isNotEmpty())
                .andReturn();
        JsonNode updated = objectMapper.readTree(updateResult.getResponse().getContentAsString());
        LocalDateTime updatedDue = objectMapper.treeToValue(updated.get("maintenanceDueAt"), LocalDateTime.class);
        assertNotNull(updatedDue);
        assertEquals(newDue.toLocalDate(), updatedDue.toLocalDate());
    }

    // ------------------------------------------------------------------
    // Live tracking
    // ------------------------------------------------------------------

    @Test
    void liveTrackingRecordsPositionsAndUpdatesTeam() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);

        mockMvc.perform(post("/api/rescue-teams/{id}/location", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "heading", 90.0,
                                "speed", 12.5,
                                "accuracy", 5.0,
                                "deviceId", "GPS-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId));

        // History
        mockMvc.perform(get("/api/rescue-teams/{id}/locations", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Latest positions for all teams
        mockMvc.perform(get("/api/rescue-teams/locations/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].teamId", hasItem((int) teamId)));

        // Latest position for a single team
        mockMvc.perform(get("/api/rescue-teams/{id}/locations/latest", teamId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamId").value(teamId))
                .andExpect(jsonPath("$[0].latitude").value(19.0760));
    }

    // ------------------------------------------------------------------
    // RBAC
    // ------------------------------------------------------------------

    @Test
    void legacyUserCannotManageRescueTeams() throws Exception {
        String token = userToken();

        // Legacy USER has no RESCUE_TEAM_VIEW/MANAGE
        mockMvc.perform(get("/api/rescue-teams")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/rescue-teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("teamName", "Hacker Unit"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/missions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
