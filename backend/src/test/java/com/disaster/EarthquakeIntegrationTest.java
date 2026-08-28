package com.disaster;

import com.disaster.dto.EarthquakeDTO;
import com.disaster.service.EarthquakeService;
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
 * End-to-end tests for the {@code GET /api/earthquakes} endpoint. The USGS
 * feed is mocked so the suite never touches the network.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EarthquakeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EarthquakeService earthquakeService;

    private EarthquakeDTO sample(double magnitude, String place) {
        EarthquakeDTO dto = new EarthquakeDTO();
        dto.setId("us-test-1");
        dto.setMagnitude(magnitude);
        dto.setLocation(place);
        dto.setLatitude(26.1445);
        dto.setLongitude(91.7362);
        dto.setDepth(12.5);
        dto.setTime(1700000000000L);
        dto.setRiskLevel(EarthquakeDTO.determineRiskLevel(magnitude));
        dto.setSource("USGS");
        dto.setMapsUrl("https://www.google.com/maps?q=26.1445,91.7362");
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
        mockMvc.perform(get("/api/earthquakes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsEarthquakeListForAuthenticatedUser() throws Exception {
        when(earthquakeService.getEarthquakes())
                .thenReturn(List.of(sample(5.8, "22 km NE of Guwahati, Assam"),
                        sample(2.9, "10 km SE of Dehradun, Uttarakhand")));

        String token = login("user", "user123");

        mockMvc.perform(get("/api/earthquakes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].magnitude").value(5.8))
                .andExpect(jsonPath("$[0].location").value("22 km NE of Guwahati, Assam"))
                .andExpect(jsonPath("$[0].latitude").value(26.1445))
                .andExpect(jsonPath("$[0].longitude").value(91.7362))
                .andExpect(jsonPath("$[0].depth").value(12.5))
                .andExpect(jsonPath("$[0].time").isNumber())
                .andExpect(jsonPath("$[0].riskLevel").value("High"))
                .andExpect(jsonPath("$[0].source").value("USGS"))
                .andExpect(jsonPath("$[0].mapsUrl").value(containsString("google.com/maps")))
                .andExpect(jsonPath("$[1].riskLevel").value("Low"));
    }

    @Test
    void returnsEmptyListWhenFeedUnavailable() throws Exception {
        when(earthquakeService.getEarthquakes()).thenReturn(List.of());

        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/earthquakes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
