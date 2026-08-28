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
 * End-to-end weather module tests against the real running context.
 *
 * <p>Runs with a forced blank {@code weather.api-key} (mirrors the demo
 * configuration) so the whole chain - controller -> WeatherService ->
 * provider selection -> mock provider -> risk assessment - is exercised
 * without any external API dependency.</p>
 */
@SpringBootTest(properties = "weather.api-key=")
@AutoConfigureMockMvc
class WeatherModuleIntegrationTest {

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

    @Test
    void weatherByLocationReturnsMockPayload() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .param("location", "Bengaluru")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Bengaluru"))
                .andExpect(jsonPath("$.latitude").value(12.9716))
                .andExpect(jsonPath("$.longitude").value(77.5946))
                .andExpect(jsonPath("$.provider").value("mock"))
                .andExpect(jsonPath("$.mockData").value(true))
                .andExpect(jsonPath("$.fallback").value(true))
                .andExpect(jsonPath("$.temperature").isNumber())
                .andExpect(jsonPath("$.feelsLike").isNumber())
                .andExpect(jsonPath("$.humidity").isNumber())
                .andExpect(jsonPath("$.pressure").isNumber())
                .andExpect(jsonPath("$.windSpeed").isNumber())
                .andExpect(jsonPath("$.visibility").isNumber())
                .andExpect(jsonPath("$.weatherCondition").isNotEmpty())
                .andExpect(jsonPath("$.weatherDescription").isNotEmpty())
                .andExpect(jsonPath("$.riskLevel").isNotEmpty())
                .andExpect(jsonPath("$.riskMessage").isNotEmpty())
                .andExpect(jsonPath("$.riskFactors").isArray())
                .andExpect(jsonPath("$.riskReasons").isArray())
                .andExpect(jsonPath("$.recommendedActions").isArray())
                .andExpect(jsonPath("$.recommendedActions[0]").isNotEmpty())
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty());
    }

    @Test
    void weatherByCoordinatesReturnsMockPayload() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .param("latitude", "12.9716")
                        .param("longitude", "77.5946")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Bengaluru"))
                .andExpect(jsonPath("$.latitude").value(12.9716))
                .andExpect(jsonPath("$.longitude").value(77.5946))
                .andExpect(jsonPath("$.provider").value("mock"))
                .andExpect(jsonPath("$.mockData").value(true))
                .andExpect(jsonPath("$.fallback").value(true));
    }

    @Test
    void weatherResolvesKnownCityCoordinates() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .param("location", "Chennai")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(13.0827))
                .andExpect(jsonPath("$.longitude").value(80.2707));
    }

    @Test
    void weatherDefaultsToBengaluruWithoutParams() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Bengaluru"));
    }

    @Test
    void weatherRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("location", "Bengaluru"))
                .andExpect(status().isForbidden());
    }

    @Test
    void weatherRejectsMismatchedCoordinates() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .param("latitude", "12.9716")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void weatherRejectsOutOfRangeCoordinates() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/weather")
                        .param("latitude", "120.0")
                        .param("longitude", "77.5946")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void weatherResponseContainsNoCredentials() throws Exception {
        String token = adminToken();

        MvcResult result = mockMvc.perform(get("/api/weather")
                        .param("location", "Bengaluru")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String json = body.toString().toLowerCase();
        assert !json.contains("apikey");
        assert !json.contains("api_key");
        assert !json.contains("openweathermap.org/data");
        assert !json.contains("secret");
    }
}
