package com.disaster.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider-agnostic current-weather payload.
 *
 * <p>Both {@code OpenWeatherProvider} and {@code MockWeatherProvider} produce
 * this exact shape, so the frontend never needs to know which provider served
 * the request. The {@code provider} / {@code mockData} / {@code fallback}
 * fields let the UI safely label the source without ever exposing credentials.
 *
 * <p>Risk fields ({@code riskLevel}, {@code riskType}, {@code riskMessage},
 * {@code riskFactors}, {@code riskReasons}, {@code recommendedActions}) are
 * filled by the rule-based {@code WeatherRiskService} and are explicitly
 * labelled as an assessment, never as an official government warning.</p>
 */
public class WeatherDTO {
    private String location;
    private double latitude;
    private double longitude;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private double windDirection;
    private int visibility;
    private int cloudiness;
    private String weatherCondition;
    private String weatherDescription;
    private double rainProbability;
    private String sunrise;
    private String sunset;
    private long timestamp;
    private String lastUpdated;
    private String alert;

    // Provider source / status flags
    private String provider;
    private boolean mockData;
    private boolean fallback;

    // Rule-based risk assessment
    private String riskLevel;
    private String riskType;
    private String riskMessage;
    private List<String> riskFactors = new ArrayList<>();
    private List<String> riskReasons = new ArrayList<>();
    private List<String> recommendedActions = new ArrayList<>();

    public WeatherDTO() {}

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public double getWindDirection() { return windDirection; }
    public void setWindDirection(double windDirection) { this.windDirection = windDirection; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public int getCloudiness() { return cloudiness; }
    public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }
    public double getRainProbability() { return rainProbability; }
    public void setRainProbability(double rainProbability) { this.rainProbability = rainProbability; }
    public String getSunrise() { return sunrise; }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }
    public String getSunset() { return sunset; }
    public void setSunset(String sunset) { this.sunset = sunset; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getAlert() { return alert; }
    public void setAlert(String alert) { this.alert = alert; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public boolean isMockData() { return mockData; }
    public void setMockData(boolean mockData) { this.mockData = mockData; }
    public boolean isFallback() { return fallback; }
    public void setFallback(boolean fallback) { this.fallback = fallback; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }
    public String getRiskMessage() { return riskMessage; }
    public void setRiskMessage(String riskMessage) { this.riskMessage = riskMessage; }
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors == null ? new ArrayList<>() : riskFactors;
    }
    public List<String> getRiskReasons() { return riskReasons; }
    public void setRiskReasons(List<String> riskReasons) {
        this.riskReasons = riskReasons == null ? new ArrayList<>() : riskReasons;
    }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions == null ? new ArrayList<>() : recommendedActions;
    }
}
