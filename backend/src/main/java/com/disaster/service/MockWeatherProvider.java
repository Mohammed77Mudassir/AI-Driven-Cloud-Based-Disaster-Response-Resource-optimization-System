package com.disaster.service;

import com.disaster.dto.WeatherDTO;
import org.springframework.stereotype.Service;

@Service
public class MockWeatherProvider implements WeatherService {
    @Override
    public WeatherDTO getWeather(String location, double latitude, double longitude) {
        WeatherDTO dto = new WeatherDTO();
        dto.setLocation(location);
        dto.setTemperature(28.5 + (Math.random() * 10 - 5));
        dto.setHumidity(45 + (int)(Math.random() * 40));
        dto.setRainProbability(Math.round(Math.random() * 100 * 10.0) / 10.0);
        dto.setWindSpeed(12.5 + (Math.random() * 20));
        dto.setVisibility(8000 + (int)(Math.random() * 7000));
        
        String[] conditions = {"Clear", "Partly Cloudy", "Cloudy", "Rainy", "Stormy"};
        String[] alerts = {"", "", "", "Heat advisory in effect", "Heavy rainfall warning", "Strong wind alert"};
        dto.setWeatherCondition(conditions[(int)(Math.random() * conditions.length)]);
        dto.setAlert(Math.random() > 0.7 ? alerts[(int)(Math.random() * alerts.length)] : "");
        dto.setMockData(true);
        
        return dto;
    }
}
