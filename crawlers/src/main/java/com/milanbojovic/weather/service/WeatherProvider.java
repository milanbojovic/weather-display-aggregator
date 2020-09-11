package com.milanbojovic.weather.service;

import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.data.model.WeatherData;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface WeatherProvider {
    CurrentWeather provideCurrentWeather(String city);

    List<DailyForecast> provideWeeklyForecast(String city);

    default WeatherData provideWeather(String city) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(StringUtils.capitalize(city));
        weatherData.setCurrentWeather(provideCurrentWeather(city));
        weatherData.setWeeklyForecast(provideWeeklyForecast(city));
        return weatherData;
    }
}
