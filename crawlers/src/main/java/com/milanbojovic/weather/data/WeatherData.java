package com.milanbojovic.weather.data;

import lombok.Data;

import java.awt.*;
import java.util.Date;

@Data
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
    Image image;
    Date date;
}

