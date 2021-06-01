package com.milanbojovic.weather.data.model.extraction.api.interfaces;

import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.extraction.DataParser;

public interface CurrentWeatherParser extends DataParser {

    default CurrentWeather createCurrentWeatherFrom(String city) {
        return CurrentWeather.builder()
                .currentTemp(getTemp(city))
                .realFeel(getRealFeel(city))
                .humidity(getHumidity(city))
                .pressure(getPressure(city))
                .windSpeed(getWindSpeed(city))
                .windDirection(getWindDirection(city))
                .uvIndex(getUvIndex(city))
                .imageUrl(getImageUrl(city))
                .description(getDescription(city))
                .date(getDate(city))
                .build();
    }

    double getTemp(String city);

    double getRealFeel(String city);

    int getHumidity(String city);

    double getPressure(String city);

    double getWindSpeed(String city);

    String getWindDirection(String city);

    double getUvIndex(String city);

    String getImageUrl(String city);

    String getDescription(String city);

    String getDate(String city);
}
