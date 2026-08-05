package com.disaster.dto;

public class WeatherDTO {
    private String location;
    private double temperature;
    private int humidity;
    private double rainProbability;
    private double windSpeed;
    private int visibility;
    private String weatherCondition;
    private String alert;
    private boolean mockData;

    public WeatherDTO() {}
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public double getRainProbability() { return rainProbability; }
    public void setRainProbability(double rainProbability) { this.rainProbability = rainProbability; }
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    public String getAlert() { return alert; }
    public void setAlert(String alert) { this.alert = alert; }
    public boolean isMockData() { return mockData; }
    public void setMockData(boolean mockData) { this.mockData = mockData; }
}
