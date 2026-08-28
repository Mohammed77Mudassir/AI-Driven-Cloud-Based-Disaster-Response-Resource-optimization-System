package com.disaster;

import com.disaster.dto.AirQualityDTO;
import com.disaster.service.AirQualityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for the {@code GET /api/air-quality} endpoint. The OpenAQ
 * feed is mocked so the suite never touches the network.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AirQualityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AirQualityService airQualityService;

    private AirQualityDTO sample() {
        AirQualityDTO dto = new AirQualityDTO();
        dto.setStationName("Delhi - ITO");
        dto.setLatitude(28.62);
        dto.setLongitude(77.23);
        dto.setPm25(210.5);
        dto.setPm10(320.0);
        dto.setNo2(45.0);
        dto.setO3(30.0);
        dto.setCo(1.5);
        dto.setAqi(370);
        dto.setCategory("Very Poor");
        dto.setTimestamp(1704103200000L);
        dto.setSource("OpenAQ");
        dto.setMapsUrl("https://www.google.com/maps?q=28.62,77.23");
        return dto;
    }

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

    @Test
    void endpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/air-quality"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsAirQualityListForAuthenticatedUser() throws Exception {
        when(airQualityService.getAirQuality()).thenReturn(List.of(sample()));

        String token = login("user", "user123");

        mockMvc.perform(get("/api/air-quality")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stationName").value("Delhi - ITO"))
                .andExpect(jsonPath("$[0].latitude").value(28.62))
                .andExpect(jsonPath("$[0].longitude").value(77.23))
                .andExpect(jsonPath("$[0].pm25").value(210.5))
                .andExpect(jsonPath("$[0].pm10").value(320.0))
                .andExpect(jsonPath("$[0].no2").value(45.0))
                .andExpect(jsonPath("$[0].o3").value(30.0))
                .andExpect(jsonPath("$[0].co").value(1.5))
                .andExpect(jsonPath("$[0].aqi").value(370))
                .andExpect(jsonPath("$[0].category").value("Very Poor"))
                .andExpect(jsonPath("$[0].timestamp").isNumber())
                .andExpect(jsonPath("$[0].source").value("OpenAQ"))
                .andExpect(jsonPath("$[0].mapsUrl").value(containsString("google.com/maps")));
    }

    @Test
    void returnsEmptyListWhenNoStationsAvailable() throws Exception {
        when(airQualityService.getAirQuality()).thenReturn(List.of());

        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/air-quality")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
