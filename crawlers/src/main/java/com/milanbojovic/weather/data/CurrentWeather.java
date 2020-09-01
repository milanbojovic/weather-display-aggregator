package com.milanbojovic.weather.data;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

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
    String day;
}

