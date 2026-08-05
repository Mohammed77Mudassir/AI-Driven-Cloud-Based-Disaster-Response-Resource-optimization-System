package com.disaster.service;

import com.disaster.dto.WeatherDTO;

public interface WeatherService {
    WeatherDTO getWeather(String location, double latitude, double longitude);
}
