package com.disaster;

import com.disaster.dto.FireDTO;
import com.disaster.service.FireService;
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
 * End-to-end tests for the {@code GET /api/fires} endpoint. The NASA FIRMS
 * feed is mocked so the suite never touches the network.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FireIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FireService fireService;

    private FireDTO sample() {
        FireDTO dto = new FireDTO();
        dto.setFireId("firms-NPP-2024-01-01-0102-19.0760-72.8780");
        dto.setLatitude(19.076);
        dto.setLongitude(72.878);
        dto.setBrightness(345.2);
        dto.setConfidence(90.0);
        dto.setAcquisitionDate(1704070920000L);
        dto.setSatellite("NPP");
        dto.setInstrument("VIIRS");
        dto.setFrp(12.4);
        dto.setDayNight("D");
        dto.setSource("NASA FIRMS");
        dto.setMapsUrl("https://www.google.com/maps?q=19.076,72.878");
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
        mockMvc.perform(get("/api/fires"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsFireListForAuthenticatedUser() throws Exception {
        when(fireService.getFires()).thenReturn(List.of(sample()));

        String token = login("user", "user123");

        mockMvc.perform(get("/api/fires")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fireId").value("firms-NPP-2024-01-01-0102-19.0760-72.8780"))
                .andExpect(jsonPath("$[0].latitude").value(19.076))
                .andExpect(jsonPath("$[0].longitude").value(72.878))
                .andExpect(jsonPath("$[0].brightness").value(345.2))
                .andExpect(jsonPath("$[0].confidence").value(90.0))
                .andExpect(jsonPath("$[0].acquisitionDate").isNumber())
                .andExpect(jsonPath("$[0].satellite").value("NPP"))
                .andExpect(jsonPath("$[0].instrument").value("VIIRS"))
                .andExpect(jsonPath("$[0].frp").value(12.4))
                .andExpect(jsonPath("$[0].dayNight").value("D"))
                .andExpect(jsonPath("$[0].source").value("NASA FIRMS"))
                .andExpect(jsonPath("$[0].mapsUrl").value(containsString("google.com/maps")));
    }

    @Test
    void returnsEmptyListWhenNoFiresInsideIndia() throws Exception {
        when(fireService.getFires()).thenReturn(List.of());

        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/fires")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
