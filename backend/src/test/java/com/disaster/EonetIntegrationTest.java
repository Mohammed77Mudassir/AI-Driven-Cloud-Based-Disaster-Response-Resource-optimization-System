package com.disaster;

import com.disaster.dto.EonetDTO;
import com.disaster.service.EonetService;
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
 * End-to-end tests for the {@code GET /api/eonet/events} endpoint. The NASA
 * EONET feed is mocked so the suite never touches the network.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EonetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EonetService eonetService;

    private EonetDTO sample() {
        EonetDTO dto = new EonetDTO();
        dto.setEventId("EONET_5634");
        dto.setTitle("Severe storm near Chennai");
        dto.setCategory("Severe Storms");
        dto.setStatus("Open");
        dto.setLatitude(13.0827);
        dto.setLongitude(80.2707);
        dto.setEventDate(1704067200000L);
        dto.setSource("GDACS");
        dto.setMapsUrl("https://www.google.com/maps?q=13.0827,80.2707");
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
        mockMvc.perform(get("/api/eonet/events"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsEventListForAuthenticatedUser() throws Exception {
        when(eonetService.getEvents()).thenReturn(List.of(sample()));

        String token = login("user", "user123");

        mockMvc.perform(get("/api/eonet/events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventId").value("EONET_5634"))
                .andExpect(jsonPath("$[0].title").value("Severe storm near Chennai"))
                .andExpect(jsonPath("$[0].category").value("Severe Storms"))
                .andExpect(jsonPath("$[0].status").value("Open"))
                .andExpect(jsonPath("$[0].latitude").value(13.0827))
                .andExpect(jsonPath("$[0].longitude").value(80.2707))
                .andExpect(jsonPath("$[0].eventDate").isNumber())
                .andExpect(jsonPath("$[0].source").value("GDACS"))
                .andExpect(jsonPath("$[0].mapsUrl").value(containsString("google.com/maps")));
    }

    @Test
    void returnsEmptyListWhenNoEventsInsideIndia() throws Exception {
        when(eonetService.getEvents()).thenReturn(List.of());

        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/eonet/events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
