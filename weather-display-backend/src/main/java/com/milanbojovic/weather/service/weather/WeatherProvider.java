package com.milanbojovic.weather.service.weather;

import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.data.model.WeatherData;
import com.milanbojovic.weather.service.persistance.MongoDao;
import com.mongodb.MongoClient;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public interface WeatherProvider {
    CurrentWeather provideCurrentWeather(String city);

    List<DailyForecast> provideWeeklyForecast(String city);

    String getProviderName();

    default WeatherData provideWeather(String city) {
        city = city.toLowerCase();
        return WeatherData.builder()
                .city(StringUtils.capitalize(city))
                .currentWeather(provideCurrentWeather(city))
                .weeklyForecast(provideWeeklyForecast(city))
                .build();
    }

    default WeatherData fetchPersistedWeatherData(MongoDao mongoDao, String city, String collection) {
        return mongoDao.readWeatherData(city, collection);
    }

    default void persistWeatherDataToDb(MongoDao mongoDao, List<String> citiesList, String provider) {
        List<WeatherData> weatherDataAllCities = citiesList.stream()
                .map(this::provideWeather)
                .collect(Collectors.toList());
        mongoDao.writeWeatherData(weatherDataAllCities, provider);
    }
}
