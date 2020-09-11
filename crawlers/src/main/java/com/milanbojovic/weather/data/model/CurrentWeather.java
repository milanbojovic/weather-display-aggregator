package com.milanbojovic.weather.data.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentWeather {
    double currentTemp;
    double realFeel;
    int humidity;
    double pressure;
    double uvIndex;
    double windSpeed;
    String windDirection;
    String description;
    String imageUrl;
    String date;
}

