package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.data.model.WeatherData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OldWeatherSource {
    static final Logger LOGGER = LoggerFactory.getLogger(OldWeatherSource.class);
    Map<String, WeatherData> weatherDataMap = new HashMap<>();

    default void persistAllWeatherDataToMap(List<String> cities) {
        cities.stream()
                .map(this::assembleWeatherDataForCity)
                .forEach(weatherData -> weatherDataMap.put(weatherData.getCity(), weatherData));
    }

    default WeatherData assembleWeatherDataForCity(String city) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(StringUtils.capitalize(city));
        weatherData.setCurrentWeather(initializeCurrentWeather(city));
        weatherData.setWeeklyForecast(initializeDailyForecast(city));
        return weatherData;
    }

    CurrentWeather initializeCurrentWeather(String city);

    List<DailyForecast> initializeDailyForecast(String city);

//    protected CurrentWeather initializeCurrentWeather(String city) {
//        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", weatherProvider, city));
//        return buildCurrentWeatherForCity(city);

//    }
//    protected List<DailyForecast> initializeDailyForecast(String city) {
//        city = city.toLowerCase();
//        LOGGER.debug(String.format("Initializing weather data for %s.", weatherProvider));
//        return getWeeklyForecast(city)
//                .stream()
//                .map(this::buildDailyForecastFor)
//                .collect(Collectors.toList());
//    }

//    protected String getDayFromDateString(String dateStr) {
//        String[] dateSplit = dateStr.split("\\.");
//        int day = Integer.parseInt(dateSplit[0]);
//        int month = Integer.parseInt(dateSplit[1]);
//        int year = Integer.parseInt(dateSplit[2]);
//
//        String stringDate = LocalDate.of(year, month, day)
//                .getDayOfWeek()
//                .toString()
//                .toLowerCase();
//        return Util.translateDayToRsCyrilic.get(stringDate);
//    }
//
//    protected String getForecastedDay(String city) {
//        return getDayFromDateString(getForecastedDate(city));
//    }
}
