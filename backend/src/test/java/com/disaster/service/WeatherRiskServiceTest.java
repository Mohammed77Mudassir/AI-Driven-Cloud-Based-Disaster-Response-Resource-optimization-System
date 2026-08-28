package com.disaster.service;

import com.disaster.dto.WeatherDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherRiskServiceTest {

    private final WeatherRiskService service = new WeatherRiskService();

    private WeatherDTO dto(double temp, double wind, int visibility, double rain, String condition, int humidity) {
        WeatherDTO d = new WeatherDTO();
        d.setTemperature(temp);
        d.setWindSpeed(wind);
        d.setVisibility(visibility);
        d.setRainProbability(rain);
        d.setWeatherCondition(condition);
        d.setHumidity(humidity);
        return d;
    }

    @Test
    void benignWeatherIsLowRisk() {
        WeatherDTO dto = service.assess(dto(26.0, 8.0, 10000, 10.0, "Clear", 45));

        assertEquals("LOW", dto.getRiskLevel());
        assertEquals("NONE", dto.getRiskType());
        assertFalse(dto.getRiskMessage().isBlank());
        assertTrue(dto.getRiskFactors().isEmpty());
        assertTrue(dto.getAlert().isBlank());
    }

    @Test
    void extremeHeatRaisesHeatRisk() {
        WeatherDTO dto = service.assess(dto(43.0, 10.0, 10000, 5.0, "Clear", 30));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("HEAT", dto.getRiskType());
        assertTrue(dto.getAlert().toLowerCase().contains("heat"));
    }

    @Test
    void highWindRaisesWindRisk() {
        WeatherDTO dto = service.assess(dto(28.0, 70.0, 10000, 5.0, "Clear", 40));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("WIND", dto.getRiskType());
    }

    @Test
    void lowVisibilityRaisesVisibilityRisk() {
        WeatherDTO dto = service.assess(dto(22.0, 5.0, 150, 5.0, "Mist", 90));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("VISIBILITY", dto.getRiskType());
    }

    @Test
    void thunderstormRaisesStormRisk() {
        WeatherDTO dto = service.assess(dto(24.0, 15.0, 8000, 85.0, "Thunderstorm", 80));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("STORM", dto.getRiskType());
    }

    @Test
    void heavyRainRaisesRainfallRisk() {
        WeatherDTO dto = service.assess(dto(27.0, 12.0, 7000, 95.0, "Rain", 80));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("RAINFALL", dto.getRiskType());
    }

    @Test
    void dominantFactorWins() {
        WeatherDTO dto = service.assess(dto(44.0, 30.0, 9000, 20.0, "Cloudy", 40));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertEquals("HEAT", dto.getRiskType());
    }

    @Test
    void allRiskFactorsAreCollected() {
        WeatherDTO dto = service.assess(dto(38.0, 45.0, 800, 80.0, "Thunderstorm", 70));

        assertEquals("EXTREME", dto.getRiskLevel());
        assertTrue(dto.getRiskFactors().size() >= 4);
        assertTrue(dto.getRiskFactors().stream().allMatch(f -> f != null && !f.isBlank()));
    }

    @Test
    void mediumHeatWithHighHumidityAddsFactor() {
        WeatherDTO dto = service.assess(dto(34.0, 10.0, 10000, 10.0, "Clear", 70));

        assertEquals("MEDIUM", dto.getRiskLevel());
        assertEquals("HEAT", dto.getRiskType());
        assertEquals(2, dto.getRiskFactors().size());
    }

    @Test
    void riskReasonsArePopulated() {
        WeatherDTO dto = service.assess(dto(38.0, 45.0, 800, 80.0, "Thunderstorm", 70));

        assertFalse(dto.getRiskReasons().isEmpty());
        assertEquals(dto.getRiskFactors(), dto.getRiskReasons());
        assertTrue(dto.getRiskReasons().stream().allMatch(r -> r != null && !r.isBlank()));
    }

    @Test
    void recommendedActionsMatchDetectedHazard() {
        WeatherDTO heat = service.assess(dto(43.0, 10.0, 10000, 5.0, "Clear", 30));
        assertTrue(heat.getRecommendedActions().stream()
                .anyMatch(a -> a.toLowerCase().contains("hydrat")));

        WeatherDTO storm = service.assess(dto(24.0, 15.0, 8000, 85.0, "Thunderstorm", 80));
        assertTrue(storm.getRecommendedActions().stream()
                .anyMatch(a -> a.toLowerCase().contains("indoors") || a.toLowerCase().contains("shelter")));

        WeatherDTO wind = service.assess(dto(28.0, 70.0, 10000, 5.0, "Clear", 40));
        assertTrue(wind.getRecommendedActions().stream()
                .anyMatch(a -> a.toLowerCase().contains("secure")));
    }

    @Test
    void benignWeatherGetsGenericActions() {
        WeatherDTO dto = service.assess(dto(26.0, 8.0, 10000, 10.0, "Clear", 45));

        assertFalse(dto.getRecommendedActions().isEmpty());
        assertTrue(dto.getRecommendedActions().stream().noneMatch(a -> a.toLowerCase().contains("hydrat")));
    }

    @Test
    void assessHandlesNull() {
        assertNull(service.assess(null));
    }
}
