package com.milanbojovic.weather.data.extraction.template.api;

import com.milanbojovic.weather.data.extraction.template.DataExtractor;

public interface DailyForecastExtractor extends DataExtractor {
    double getMinTemp(String city);

    double getMaxTemp(String city);

    double getWindSpeed(String city);

    String getWindDirection(String city);

    double getUvIndex(String city);

    String getDescription(String city);

    String getDate(String city);

    String getImageUrl(String city);

}
