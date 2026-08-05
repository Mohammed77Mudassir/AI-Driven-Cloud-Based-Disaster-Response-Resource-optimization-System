package com.disaster;

import com.disaster.service.WebSocketSessionService;
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
 * End-to-end tests for every Admin / Emergency Operations Center feature:
 * dashboard statistics, settings CRUD and RBAC.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebSocketSessionService sessionService;

    private String login(String username, String password) throws Exception {
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
    void dashboardReturnsEveryEocSection() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeUsers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.usersByRole.ADMIN", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.disastersByStatus", notNullValue()))
                .andExpect(jsonPath("$.disastersBySeverity", notNullValue()))
                .andExpect(jsonPath("$.disastersByMonth", notNullValue()))
                .andExpect(jsonPath("$.resourceUtilizationPercent", notNullValue()))
                .andExpect(jsonPath("$.resourcesAvailable", notNullValue()))
                .andExpect(jsonPath("$.systemUptime", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.systemHealthStatus", anyOf(is("UP"), is("DOWN"), is("DEGRADED"))))
                .andExpect(jsonPath("$.connectedUsers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.websocketHealthy", is(anyOf(is(true), is(false)))))
                .andExpect(jsonPath("$.recentActivities", notNullValue()))
                .andExpect(jsonPath("$.recentNotifications", notNullValue()));
    }

    @Test
    @Order(2)
    void connectedUsersReflectedInDashboard() throws Exception {
        String token = login("admin", "admin123");
        int before = sessionService.getActiveSessionCount();

        sessionService.addSession("eoc-dashboard-session");
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectedUsers", greaterThanOrEqualTo(before + 1)));
        sessionService.removeSession("eoc-dashboard-session");
    }

    @Test
    @Order(3)
    void settingsCrudWorks() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/admin/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        // Update a setting.
        mockMvc.perform(put("/api/admin/settings/test.key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"hello-eoc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingValue").value("hello-eoc"));

        // Update again to a different value (idempotent).
        mockMvc.perform(put("/api/admin/settings/test.key")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingValue").value("updated"));
    }

    @Test
    @Order(4)
    void nonAdminIsForbiddenFromAdminApi() throws Exception {
        String token = login("user", "user123");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
