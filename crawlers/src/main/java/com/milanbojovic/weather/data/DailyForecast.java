package com.milanbojovic.weather.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyForecast {
    String provider;
    double minTemp;
    double maxTemp;
    double windSpeed;
    String windDirection;
    double uvIndex;
    String description;
    String date;
    String day;
    String imageUrl;
}
