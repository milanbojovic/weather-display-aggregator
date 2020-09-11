package com.milanbojovic.weather.data.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class WeatherData {
    private String city;
    private CurrentWeather currentWeather;
    private List<DailyForecast> weeklyForecast;
}
