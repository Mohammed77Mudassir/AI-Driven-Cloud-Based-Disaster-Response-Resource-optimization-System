package com.disaster.service;

import com.disaster.dto.WeatherDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockWeatherProviderTest {

    private final MockWeatherProvider provider = new MockWeatherProvider(new WeatherRiskService());

    @Test
    void returnsCompleteProviderAgnosticPayload() {
        WeatherDTO dto = provider.getWeather("Bengaluru", 12.9716, 77.5946);

        assertEquals("Bengaluru", dto.getLocation());
        assertEquals(12.9716, dto.getLatitude());
        assertEquals(77.5946, dto.getLongitude());

        assertTrue(dto.getTemperature() >= 26.0 && dto.getTemperature() <= 34.0);
        assertTrue(dto.getFeelsLike() >= dto.getTemperature());
        assertTrue(dto.getHumidity() >= 40 && dto.getHumidity() <= 79);
        assertTrue(dto.getPressure() >= 1004 && dto.getPressure() <= 1015);
        assertTrue(dto.getWindSpeed() >= 6.0 && dto.getWindSpeed() <= 32.0);
        assertTrue(dto.getWindDirection() >= 0 && dto.getWindDirection() <= 360);
        assertTrue(dto.getVisibility() >= 6000 && dto.getVisibility() <= 14999);
        assertTrue(dto.getCloudiness() >= 0 && dto.getCloudiness() <= 100);
        assertTrue(dto.getRainProbability() >= 0 && dto.getRainProbability() <= 100);

        assertFalse(dto.getWeatherCondition().isBlank());
        assertFalse(dto.getWeatherDescription().isBlank());
        assertNotNull(dto.getSunrise());
        assertNotNull(dto.getSunset());
        assertTrue(dto.getTimestamp() > 0);
        assertNotNull(dto.getLastUpdated());
    }

    @Test
    void flagsItselfAsMockFallback() {
        WeatherDTO dto = provider.getWeather("Chennai", 13.0827, 80.2707);

        assertEquals("mock", dto.getProvider());
        assertTrue(dto.isMockData());
        assertTrue(dto.isFallback());
    }

    @Test
    void alwaysRunsRiskAssessment() {
        WeatherDTO dto = provider.getWeather("Delhi", 28.6139, 77.2090);

        assertNotNull(dto.getRiskLevel());
        assertNotNull(dto.getRiskType());
        assertFalse(dto.getRiskMessage().isBlank());
        assertNotNull(dto.getRiskFactors());
        assertNotNull(dto.getRiskReasons());
        assertFalse(dto.getRecommendedActions().isEmpty());
    }
}
