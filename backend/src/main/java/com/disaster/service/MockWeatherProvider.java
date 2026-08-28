package com.disaster.service;

import com.disaster.dto.WeatherDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

/**
 * Deterministic-ish simulated weather provider used when OpenWeather is not
 * configured or unreachable.
 *
 * <p>Values are seeded from the location + a 30-second window, so a refresh
 * produces slightly newer data while staying stable enough for the charts.
 * Every payload is explicitly flagged {@code mockData=true / fallback=true}
 * and {@code provider=mock} so the UI can label it "Demo Weather Data".</p>
 */
@Service
public class MockWeatherProvider implements WeatherService {

    private final WeatherRiskService weatherRiskService;

    public MockWeatherProvider(WeatherRiskService weatherRiskService) {
        this.weatherRiskService = weatherRiskService;
    }

    @Override
    public WeatherDTO getWeather(String location, double latitude, double longitude) {
        long seed = (System.currentTimeMillis() / 30_000L)
                + location.toLowerCase().hashCode()
                + Double.hashCode(latitude) * 31L
                + Double.hashCode(longitude);
        Random random = new Random(seed);

        String[] conditions = {"Clear", "Partly Cloudy", "Cloudy", "Rainy", "Stormy"};
        String[] descriptions = {"clear sky", "few clouds", "broken clouds", "moderate rain", "heavy thunderstorm"};
        int index = random.nextInt(conditions.length);

        double temperature = Math.round((26.0 + random.nextDouble() * 8.0) * 10.0) / 10.0;
        double windSpeed = Math.round((6.0 + random.nextDouble() * 26.0) * 10.0) / 10.0;
        int humidity = 40 + random.nextInt(40);
        int visibility = 6000 + random.nextInt(9000);
        int cloudiness = random.nextInt(101);
        double rainProbability = rainProbabilityFor(index, random);
        int pressure = 1004 + random.nextInt(12);

        WeatherDTO dto = new WeatherDTO();
        dto.setLocation(location);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setTemperature(temperature);
        dto.setFeelsLike(Math.round((temperature + 1.2) * 10.0) / 10.0);
        dto.setHumidity(humidity);
        dto.setPressure(pressure);
        dto.setWindSpeed(windSpeed);
        dto.setWindDirection(Math.round(random.nextDouble() * 360.0));
        dto.setVisibility(visibility);
        dto.setCloudiness(cloudiness);
        dto.setWeatherCondition(conditions[index]);
        dto.setWeatherDescription(descriptions[index]);
        dto.setRainProbability(rainProbability);
        dto.setSunrise("06:12");
        dto.setSunset("18:43");
        dto.setTimestamp(System.currentTimeMillis());
        dto.setLastUpdated(Instant.now().toString());

        dto.setProvider("mock");
        dto.setMockData(true);
        dto.setFallback(true);

        weatherRiskService.assess(dto);
        return dto;
    }

    /** Rain probability consistent with the simulated condition. */
    private double rainProbabilityFor(int conditionIndex, Random random) {
        return switch (conditionIndex) {
            case 4 -> Math.round((80.0 + random.nextDouble() * 19.0) * 10.0) / 10.0;   // Stormy 80-98 %
            case 3 -> Math.round((65.0 + random.nextDouble() * 30.0) * 10.0) / 10.0;   // Rainy 65-94 %
            case 2 -> Math.round((30.0 + random.nextDouble() * 25.0) * 10.0) / 10.0;   // Cloudy 30-54 %
            case 1 -> Math.round((10.0 + random.nextDouble() * 20.0) * 10.0) / 10.0;   // Partly Cloudy 10-29 %
            default -> Math.round(random.nextDouble() * 15.0 * 10.0) / 10.0;           // Clear 0-14 %
        };
    }
}
