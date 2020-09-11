package com.milanbojovic.weather.data.extraction.template.api;

import com.milanbojovic.weather.data.extraction.template.DataExtractor;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.Util;

public interface CurrentWeatherExtractor extends DataExtractor {

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

    @Override
    default String toDate(String dateStr) {
        String[] dateArr = dateStr.split("T")[0].split("-");
        int year = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int day = Integer.parseInt(dateArr[2]);
        return Util.formatDate(year, month, day);
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
