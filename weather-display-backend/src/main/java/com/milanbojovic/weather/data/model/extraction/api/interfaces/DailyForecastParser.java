package com.milanbojovic.weather.data.model.extraction.api.interfaces;

import com.milanbojovic.weather.data.model.extraction.DataParser;

public interface DailyForecastParser extends DataParser {
    double getMinTemp(String city);

    double getMaxTemp(String city);

    double getWindSpeed(String city);

    String getWindDirection(String city);

    double getUvIndex(String city);

    String getDescription(String city);

    String getDate(String city);

    String getImageUrl(String city);

}
