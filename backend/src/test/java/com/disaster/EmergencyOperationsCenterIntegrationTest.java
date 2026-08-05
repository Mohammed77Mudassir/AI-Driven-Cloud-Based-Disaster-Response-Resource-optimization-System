package com.disaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
 * End-to-end tests for the Emergency Operations Center dashboard:
 * notifications, audit logs, exports and the RBAC denial matrix.
 * Complements {@link AdminDashboardIntegrationTest} (dashboard stats,
 * settings CRUD) so every admin feature is covered.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmergencyOperationsCenterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @Order(1)
    void dashboardExposesEveryEocWidgetSource() throws Exception {
        String token = token("admin", "admin123");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMissions", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalRescueTeams", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalHospitals", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalShelters", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalVolunteers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalDrones", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.resourcesAvailable", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.resourcesDeployed", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.resourcesInMaintenance", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.resourceUtilizationPercent", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100))))
                .andExpect(jsonPath("$.usersByRole", notNullValue()))
                .andExpect(jsonPath("$.disastersByStatus", notNullValue()))
                .andExpect(jsonPath("$.disastersBySeverity", notNullValue()))
                .andExpect(jsonPath("$.disastersByType", notNullValue()))
                .andExpect(jsonPath("$.disastersByMonth", notNullValue()))
                .andExpect(jsonPath("$.connectedUsers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.recentActivities", notNullValue()))
                .andExpect(jsonPath("$.recentNotifications", notNullValue()));
    }

    @Test
    @Order(2)
    void notificationsEndpointReturnsUnreadCountAndLatest() throws Exception {
        String token = token("admin", "admin123");

        mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.notifications", isA(java.util.List.class)));
    }

    @Test
    @Order(3)
    void auditLogsReturnHistoryForAdmin() throws Exception {
        String token = token("admin", "admin123");

        mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        mockMvc.perform(get("/api/audit-logs/user/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(4)
    void exportEndpointsReturnDownloadableFiles() throws Exception {
        String token = token("admin", "admin123");

        mockMvc.perform(get("/api/exports/csv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                .andExpect(content().string(containsString("ID,Type")));

        mockMvc.perform(get("/api/exports/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/pdf")))
                .andExpect(content().string(startsWith("%PDF-")));

        mockMvc.perform(get("/api/exports/excel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/vnd.ms-excel")));
    }

    @Test
    @Order(5)
    void nonAdminIsForbiddenFromEveryEocFeature() throws Exception {
        String token = token("user", "user123");

        mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/audit-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/exports/csv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/settings/eoc.denied")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"nope\"}"))
                .andExpect(status().isForbidden());
    }
}
