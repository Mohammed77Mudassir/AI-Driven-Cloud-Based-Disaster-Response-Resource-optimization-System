package com.disaster.service;

import com.disaster.config.WeatherProperties;
import com.disaster.dto.WeatherDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OpenWeatherProviderTest {

    private WeatherProperties properties;
    private RestTemplate restTemplate;
    private MockWeatherProvider mockProvider;
    private OpenWeatherProvider provider;

    @BeforeEach
    void setUp() {
        properties = new WeatherProperties();
        restTemplate = mock(RestTemplate.class);
        mockProvider = mock(MockWeatherProvider.class);
        provider = new OpenWeatherProvider(properties, restTemplate, mockProvider, new WeatherRiskService());
    }

    @Test
    void delegatesToMockWhenApiKeyIsMissing() {
        WeatherDTO expected = new WeatherDTO();
        expected.setProvider("mock");
        expected.setMockData(true);
        expected.setFallback(true);
        when(mockProvider.getWeather("Delhi", 28.6139, 77.2090)).thenReturn(expected);

        WeatherDTO dto = provider.getWeather("Delhi", 28.6139, 77.2090);

        assertEquals("mock", dto.getProvider());
        assertTrue(dto.isMockData());
        assertTrue(dto.isFallback());
        verify(mockProvider, times(1)).getWeather("Delhi", 28.6139, 77.2090);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void usesMockWhenApiKeyIsBlank() {
        properties.setApiKey("   ");

        when(mockProvider.getWeather("Delhi", 28.6139, 77.2090)).thenReturn(new WeatherDTO());

        provider.getWeather("Delhi", 28.6139, 77.2090);

        verify(mockProvider, times(1)).getWeather("Delhi", 28.6139, 77.2090);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void mapsSuccessfulOpenWeatherResponse() {
        properties.setApiKey("test-key");

        Map<String, Object> body = Map.of(
                "main", Map.of("temp", 30.0, "feels_like", 32.0, "humidity", 55, "pressure", 1012),
                "wind", Map.of("speed", 12.0, "deg", 240.0),
                "clouds", Map.of("all", 40),
                "visibility", 10000,
                "weather", List.of(Map.of("main", "Clouds", "description", "broken clouds")),
                "sys", Map.of("sunrise", 1700000000L, "sunset", 1700046000L),
                "dt", 1700000000L);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(body);

        WeatherDTO dto = provider.getWeather("Delhi", 28.6139, 77.2090);

        assertEquals("openweather", dto.getProvider());
        assertFalse(dto.isMockData());
        assertFalse(dto.isFallback());
        assertEquals("Delhi", dto.getLocation());
        assertEquals(28.6139, dto.getLatitude());
        assertEquals(77.2090, dto.getLongitude());
        assertEquals(30.0, dto.getTemperature());
        assertEquals(32.0, dto.getFeelsLike());
        assertEquals(55, dto.getHumidity());
        assertEquals(1012, dto.getPressure());
        assertEquals(12.0, dto.getWindSpeed());
        assertEquals(240.0, dto.getWindDirection());
        assertEquals(40, dto.getCloudiness());
        assertEquals(10000, dto.getVisibility());
        assertEquals("Clouds", dto.getWeatherCondition());
        assertEquals("broken clouds", dto.getWeatherDescription());
        assertNotNull(dto.getSunrise());
        assertNotNull(dto.getSunset());
        assertEquals(1700000000000L, dto.getTimestamp());
        assertNotNull(dto.getRiskLevel());
        assertNotNull(dto.getLastUpdated());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
        verifyNoInteractions(mockProvider);
    }

    @Test
    void fallsBackToMockWhenOpenWeatherFails() {
        properties.setApiKey("test-key");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("connection refused"));

        WeatherDTO fallback = new WeatherDTO();
        fallback.setProvider("mock");
        fallback.setMockData(true);
        fallback.setFallback(true);
        when(mockProvider.getWeather("Delhi", 28.6139, 77.2090)).thenReturn(fallback);

        WeatherDTO dto = provider.getWeather("Delhi", 28.6139, 77.2090);

        assertEquals("mock", dto.getProvider());
        assertTrue(dto.isMockData());
        assertTrue(dto.isFallback());
        verify(mockProvider, times(1)).getWeather("Delhi", 28.6139, 77.2090);
    }

    @Test
    void fallsBackToMockWhenResponseIsEmpty() {
        properties.setApiKey("test-key");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        WeatherDTO fallback = new WeatherDTO();
        fallback.setProvider("mock");
        fallback.setFallback(true);
        when(mockProvider.getWeather("Delhi", 28.6139, 77.2090)).thenReturn(fallback);

        WeatherDTO dto = provider.getWeather("Delhi", 28.6139, 77.2090);

        assertTrue(dto.isFallback());
        verify(mockProvider, times(1)).getWeather("Delhi", 28.6139, 77.2090);
    }

    @Test
    void usesCoordinatesWhenProvided() {
        properties.setApiKey("test-key");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        provider.getWeather("Delhi", 28.6139, 77.2090);

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(captor.capture(), eq(Map.class));
        String url = captor.getValue();
        assertTrue(url.contains("lat=28.6139"));
        assertTrue(url.contains("lon=77.209"));
        assertFalse(url.contains("q=Delhi"));
    }

    @Test
    void usesCityNameWhenNoCoordinates() {
        properties.setApiKey("test-key");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(Map.of());

        provider.getWeather("Bengaluru", 0.0, 0.0);

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(captor.capture(), eq(Map.class));
        String url = captor.getValue();
        assertTrue(url.contains("q=Bengaluru"));
        assertFalse(url.contains("lat="));
    }
}
