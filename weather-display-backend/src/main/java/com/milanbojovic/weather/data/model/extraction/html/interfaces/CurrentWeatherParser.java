package com.milanbojovic.weather.data.model.extraction.html.interfaces;

import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.extraction.DataParser;
import org.jsoup.nodes.Element;


public interface CurrentWeatherParser extends DataParser {

    default CurrentWeather createCurrentWeatherFrom(Element doc) {
        return CurrentWeather.builder()
                .currentTemp(getTemp(doc))
                .realFeel(getRealFeel(doc))
                .humidity(getHumidity(doc))
                .pressure(getPressure(doc))
                .windSpeed(getWindSpeed(doc))
                .windDirection(getWindDirection(doc))
                .uvIndex(getUvIndex(doc))
                .imageUrl(getImageUrl(doc))
                .description(getDescription(doc))
                .date(getDate(doc))
                .build();
    }

    double getTemp(Element doc);

    double getRealFeel(Element doc);

    int getHumidity(Element doc);

    double getPressure(Element doc);

    double getWindSpeed(Element doc);

    String getWindDirection(Element doc);

    double getUvIndex(Element doc);

    String getImageUrl(Element doc);

    String getDescription(Element doc);

    String getDate(Element doc);
}
