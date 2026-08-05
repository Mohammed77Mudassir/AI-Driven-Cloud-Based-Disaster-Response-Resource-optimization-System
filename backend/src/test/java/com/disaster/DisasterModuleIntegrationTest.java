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
 * End-to-end integration tests for the Disaster Reporting module covering
 * public citizen reporting, admin/authorized CRUD, RBAC enforcement, the
 * status workflow, comments, assignment history and server-side
 * search/filter/sort/pagination.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DisasterModuleIntegrationTest {

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
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> mutableMap() {
        return new LinkedHashMap<>();
    }

    // ------------------------------------------------------------------
    // Public citizen reporting (no login required)
    // ------------------------------------------------------------------

    @Test
    void publicReportCanBeSubmittedAndTrackedWithoutAuthentication() throws Exception {
        MvcResult submit = mockMvc.perform(post("/api/public/disasters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reporterName", "Public Citizen",
                                "reporterMobile", "+911234567890",
                                "reporterEmail", "public@example.com",
                                "disasterType", "Earthquake",
                                "severity", "High",
                                "description", "Tremors felt in residential areas",
                                "address", "Guwahati, Assam",
                                "latitude", 26.1445,
                                "longitude", 91.7362))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").isNotEmpty())
                .andReturn();

        String reportId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .get("reportId").asText();

        // Track by report ID without any token
        mockMvc.perform(get("/api/public/disasters/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.reporterName").value("Public Citizen"))
                .andExpect(jsonPath("$.disasterType").value("Earthquake"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/public/disasters/{id}/timeline", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Unknown report id -> 404
        mockMvc.perform(get("/api/public/disasters/{id}", "DMS-NOT-A-REAL-ID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicReportValidationRejectsInvalidPayloads() throws Exception {
        mockMvc.perform(post("/api/public/disasters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reporterName", "",
                                "reporterMobile", "abc",
                                "disasterType", "",
                                "severity", "",
                                "description", "",
                                "address", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reporterName").isNotEmpty())
                .andExpect(jsonPath("$.reporterMobile").isNotEmpty());
    }

    // ------------------------------------------------------------------
    // Admin / authorized CRUD
    // ------------------------------------------------------------------

    @Test
    void adminCanPerformFullDisasterCrud() throws Exception {
        String token = adminToken();
        long id = createDisaster(token);

        // Read single
        mockMvc.perform(get("/api/disasters/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.disasterType").value("Flood"));

        // Read detail (timeline, comments, assignments, attachments)
        mockMvc.perform(get("/api/disasters/{id}/detail", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline").isArray())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.assignments").isArray())
                .andExpect(jsonPath("$.timeline", hasSize(greaterThanOrEqualTo(1))));

        // Update
        mockMvc.perform(put("/api/disasters/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "disasterType", "Flood",
                                "description", "Updated: flooding escalated after continuous rain",
                                "severity", "Critical",
                                "location", "Mumbai, Maharashtra",
                                "latitude", 19.0760,
                                "longitude", 72.8777,
                                "priority", "CRITICAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("Critical"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"));

        // Update priority only
        mockMvc.perform(put("/api/disasters/{id}/priority", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("priority", "LOW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("LOW"));

        // Status flow definition
        mockMvc.perform(get("/api/disasters/status-flow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDING").isArray())
                .andExpect(jsonPath("$.RESOLVED").isEmpty());

        // Delete
        mockMvc.perform(delete("/api/disasters/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/disasters/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Status workflow
    // ------------------------------------------------------------------

    @Test
    void statusWorkflowEnforcesAllowedTransitions() throws Exception {
        String token = adminToken();
        long id = createDisaster(token);

        // Valid: PENDING -> VERIFIED
        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("status", "VERIFIED", "comment", "Verified by field officer"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        // Valid: VERIFIED -> ASSIGNED
        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ASSIGNED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        // Invalid: ASSIGNED -> RESOLVED (must pass through IN_PROGRESS)
        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RESOLVED"))))
                .andExpect(status().isBadRequest());

        // Full lifecycle to RESOLVED
        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RESOLVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // RESOLVED is terminal -> cannot move back
        mockMvc.perform(put("/api/disasters/{id}/status", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PENDING"))))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Comments
    // ------------------------------------------------------------------

    @Test
    void commentsCanBeAddedListedAndDeleted() throws Exception {
        String token = adminToken();
        long id = createDisaster(token);

        mockMvc.perform(post("/api/disasters/{id}/comments", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("text", "Sending rescue boats to the area"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Sending rescue boats to the area"))
                .andExpect(jsonPath("$.author").isNotEmpty());

        mockMvc.perform(get("/api/disasters/{id}/comments", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Blank comment rejected
        mockMvc.perform(post("/api/disasters/{id}/comments", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("text", "  "))))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Assignment history
    // ------------------------------------------------------------------

    @Test
    void assignmentHistoryIsAvailableForAuthorizedUsers() throws Exception {
        String token = adminToken();
        long id = createDisaster(token);

        mockMvc.perform(get("/api/disasters/{id}/assignments", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ------------------------------------------------------------------
    // Search / filter / sort / pagination
    // ------------------------------------------------------------------

    @Test
    void searchFilterSortAndPaginationWorkServerSide() throws Exception {
        String token = adminToken();

        // Seed distinct reports
        for (Map.Entry<String, String> entry : Map.of(
                "Flood", "Mumbai, Maharashtra",
                "Earthquake", "Guwahati, Assam",
                "Cyclone", "Chennai, Tamil Nadu").entrySet()) {
            mockMvc.perform(post("/api/disasters")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "disasterType", entry.getKey(),
                                    "description", entry.getKey() + " event for search test",
                                    "severity", "High",
                                    "location", entry.getValue(),
                                    "latitude", 19.0,
                                    "longitude", 72.0,
                                    "priority", "MEDIUM"))))
                    .andExpect(status().isCreated());
        }

        // Search by keyword
        mockMvc.perform(get("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "mumbai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements").isNumber());

        // Filter by type
        mockMvc.perform(get("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "Flood"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].disasterType").value("Flood"));

        // Filter by status
        mockMvc.perform(get("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))));

        // Filter by priority
        mockMvc.perform(get("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].priority", everyItem(is("MEDIUM"))));

        // Pagination + sorting
        mockMvc.perform(get("/api/disasters")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0").param("size", "2")
                        .param("sortBy", "disasterType").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    // ------------------------------------------------------------------
    // Security / RBAC
    // ------------------------------------------------------------------

    @Test
    void unauthenticatedAccessIsBlocked() throws Exception {
        mockMvc.perform(get("/api/disasters"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void legacyUserCannotAccessAdminOnlyEndpoints() throws Exception {
        String token = userToken();

        // Legacy USER must not see user management (403)
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // Legacy USER must not reach admin control endpoints (403)
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // Legacy USER must not delete disasters (no DISASTER_DELETE permission)
        long id = createDisaster(token);
        mockMvc.perform(delete("/api/disasters/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAcceptsEmailAsIdentifier() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin@disaster.com", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
}
