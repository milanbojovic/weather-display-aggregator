package com.milanbojovic.weather.data.model.extraction.html.interfaces;

import com.milanbojovic.weather.data.model.extraction.DataParser;
import com.milanbojovic.weather.data.model.DailyForecast;
import org.jsoup.nodes.Element;

public interface DailyForecastParser extends DataParser {

    default DailyForecast createDailyForecastFor(Element htmlDocument) {
        return DailyForecast.builder()
                .minTemp(getMinTemp(htmlDocument))
                .maxTemp(getMaxTemp(htmlDocument))
                .windSpeed(getWindSpeed(htmlDocument))
                .windDirection(getWindDirection(htmlDocument))
                .uvIndex(getUvIndex(htmlDocument))
                .description(getDescription(htmlDocument))
                .date(getDate(htmlDocument))
                .imageUrl(getImageUrl(htmlDocument))
                .build();
    }

    double getMinTemp(Element doc);

    double getMaxTemp(Element doc);

    double getWindSpeed(Element doc);

    String getWindDirection(Element doc);

    double getUvIndex(Element doc);

    String getDescription(Element doc);

    String getDate(Element doc);

    String getImageUrl(Element doc);

}
