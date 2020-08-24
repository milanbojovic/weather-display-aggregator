package com.milanbojovic.weather.data;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class WeatherData {
    int minTemp;
    int maxTemp;
    int realFeel;
    int humidity;
    int pressure;
    double uvIndex;
    double windSpeed;
    String windDirection;
    String description;
    String imageUrl;
    Date date;
}

