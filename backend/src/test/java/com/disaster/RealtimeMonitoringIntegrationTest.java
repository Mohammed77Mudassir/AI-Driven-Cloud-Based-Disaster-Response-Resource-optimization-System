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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for the Real-Time Monitoring module:
 * aggregated overview, multi-entity location aggregation, offline distance
 * and ETA calculation, live drone simulation and websocket status.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RealtimeMonitoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private void createDisaster(String token) throws Exception {
        mockMvc.perform(post("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "disasterType", "Flood",
                                "description", "Severe flooding near the river bank",
                                "severity", "Critical",
                                "location", "Mumbai, Maharashtra",
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "priority", "CRITICAL"))))
                .andExpect(status().isCreated());
    }

    private void createHospital(String token) throws Exception {
        mockMvc.perform(post("/api/hospitals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "City General Hospital",
                                "availableBeds", 25,
                                "icuBeds", 5,
                                "doctorsAvailable", 8,
                                "emergencyContact", "+91100",
                                "bloodBank", true,
                                "latitude", 19.08,
                                "longitude", 72.88,
                                "address", "MG Road"))))
                .andExpect(status().isCreated());
    }

    private void createShelter(String token) throws Exception {
        Map<String, Object> body = mutableMap();
        body.put("name", "Community Shelter");
        body.put("capacity", 200);
        body.put("occupancy", 60);
        body.put("foodAvailable", true);
        body.put("waterAvailable", true);
        body.put("medicalKits", 12);
        body.put("powerAvailable", true);
        body.put("contact", "+91200");
        body.put("latitude", 19.09);
        body.put("longitude", 72.89);
        body.put("address", "Station Rd");
        mockMvc.perform(post("/api/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    private void createVolunteer(String token) throws Exception {
        mockMvc.perform(post("/api/volunteers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Priya Nair",
                                "email", "priya@example.com",
                                "phone", "+91300",
                                "skills", "First aid, rescue",
                                "available", true,
                                "latitude", 19.05,
                                "longitude", 72.85))))
                .andExpect(status().isCreated());
    }

    private long createTeam(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/rescue-teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamName", "Kaveri Rescue Unit",
                                "teamLeader", "Ravi Kumar",
                                "contactNumber", "+91400",
                                "location", "Mumbai",
                                "latitude", 19.07,
                                "longitude", 72.87,
                                "memberCount", 6,
                                "maxCapacity", 12,
                                "specialty", "FLOOD_RESCUE",
                                "status", "AVAILABLE"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createDrone(String token) throws Exception {
        mockMvc.perform(post("/api/drones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "droneId", "DRN-100",
                                "status", "IN_MISSION",
                                "battery", 80,
                                "cameraStatus", true,
                                "latitude", 19.10,
                                "longitude", 72.90,
                                "missionStatus", "IN_PROGRESS"))))
                .andExpect(status().isCreated());
    }

    private void createResource(String token) throws Exception {
        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resourceType", "BOAT",
                                "quantity", 10,
                                "totalQuantity", 10,
                                "location", "Mumbai Dockyard",
                                "latitude", 18.99,
                                "longitude", 72.90,
                                "condition", "GOOD"))))
                .andExpect(status().isCreated());
    }

    private void createVehicle(String token, long teamId) throws Exception {
        mockMvc.perform(post("/api/rescue-vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teamId", teamId,
                                "vehicleType", "AMBULANCE",
                                "registrationNumber", "MH-01-XY-0001",
                                "model", "Ford E-350",
                                "capacity", 4,
                                "fuelLevel", 70,
                                "latitude", 19.07,
                                "longitude", 72.87))))
                .andExpect(status().isCreated());
    }

    private Map<String, Object> mutableMap() {
        return new LinkedHashMap<>();
    }

    // ------------------------------------------------------------------
    // Overview / aggregation
    // ------------------------------------------------------------------

    @Test
    void overviewAggregatesEveryEntityType() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        createDisaster(token);
        createHospital(token);
        createShelter(token);
        createVolunteer(token);
        createDrone(token);
        createResource(token);
        createVehicle(token, teamId);

        mockMvc.perform(get("/api/monitoring/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disasters", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeDisasters", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.drones", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.dronesInMission", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.hospitals", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.shelters", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.volunteers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.resources", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.teams", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.vehicles", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void overviewLocationsContainAllEntityKinds() throws Exception {
        String token = adminToken();
        long teamId = createTeam(token);
        createDisaster(token);
        createHospital(token);
        createShelter(token);
        createVolunteer(token);
        createDrone(token);
        createResource(token);
        createVehicle(token, teamId);

        mockMvc.perform(get("/api/monitoring/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("DISASTER")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("DRONE")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("HOSPITAL")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("SHELTER")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("VOLUNTEER")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("RESOURCE")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("RESCUE_TEAM")))
                .andExpect(jsonPath("$.locations[*].entityType", hasItem("VEHICLE")));
    }

    @Test
    void droneLocationsCarryEnrichedFields() throws Exception {
        String token = adminToken();
        createDrone(token);

        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].entityType", hasItem("DRONE")))
                .andExpect(jsonPath("$[?(@.entityType=='DRONE')].name", hasItem("Drone DRN-100")))
                .andExpect(jsonPath("$[?(@.entityType=='DRONE')].battery", hasItem(80)));
    }

    // ------------------------------------------------------------------
    // Distance / ETA
    // ------------------------------------------------------------------

    @Test
    void distanceEndpointUsesHaversine() throws Exception {
        String token = adminToken();
        // Mumbai -> Delhi approx ~1150 km
        mockMvc.perform(get("/api/monitoring/distance")
                        .param("fromLat", "19.0760").param("fromLng", "72.8777")
                        .param("toLat", "28.6139").param("toLng", "77.2090")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm", allOf(greaterThan(1100.0), lessThan(1200.0))))
                .andExpect(jsonPath("$.etaMinutes", allOf(greaterThan(1600.0), lessThan(1900.0))))
                .andExpect(jsonPath("$.initialBearing", notNullValue()));
    }

    @Test
    void etaEndpointRespectsConfiguredSpeed() throws Exception {
        String token = adminToken();

        MvcResult fast = mockMvc.perform(get("/api/monitoring/eta")
                        .param("fromLat", "19.0760").param("fromLng", "72.8777")
                        .param("toLat", "28.6139").param("toLng", "77.2090")
                        .param("speedKmph", "80")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speedKmph").value(80.0))
                .andReturn();

        JsonNode fastNode = objectMapper.readTree(fast.getResponse().getContentAsString());

        MvcResult slow = mockMvc.perform(get("/api/monitoring/eta")
                        .param("fromLat", "19.0760").param("fromLng", "72.8777")
                        .param("toLat", "28.6139").param("toLng", "77.2090")
                        .param("speedKmph", "40")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode slowNode = objectMapper.readTree(slow.getResponse().getContentAsString());

        assertTrue(fastNode.get("etaMinutes").asDouble() < slowNode.get("etaMinutes").asDouble());
        // Same great-circle distance in both responses
        assertTrue(Math.abs(fastNode.get("distanceKm").asDouble() - slowNode.get("distanceKm").asDouble()) < 1.0);
    }

    // ------------------------------------------------------------------
    // Live simulation + websocket status
    // ------------------------------------------------------------------

    @Test
    void liveEndpointMovesInMissionDrones() throws Exception {
        String token = adminToken();
        createDrone(token);

        MvcResult before = mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode dronesBefore = objectMapper.readTree(before.getResponse().getContentAsString());
        double latBefore = dronesBefore.findValues("latitude").get(0).asDouble();

        MvcResult live = mockMvc.perform(get("/api/locations/live")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].entityType").value("DRONE"))
                .andReturn();

        JsonNode liveNode = objectMapper.readTree(live.getResponse().getContentAsString());
        double latAfter = liveNode.get(0).get("latitude").asDouble();
        assertTrue(latAfter != latBefore);
    }

    @Test
    void wsStatusReportsConnections() throws Exception {
        String token = adminToken();
        mockMvc.perform(get("/api/monitoring/ws-status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeConnections", notNullValue()))
                .andExpect(jsonPath("$.heartbeatIntervalSeconds").value(30))
                .andExpect(jsonPath("$.staleTimeoutSeconds").value(90));
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("Expected condition to be true");
    }
}
