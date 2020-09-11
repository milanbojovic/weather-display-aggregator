package com.milanbojovic.weather.data.extraction.template.html;

import com.milanbojovic.weather.data.extraction.template.DataExtractor;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.Util;
import org.jsoup.nodes.Element;


public interface CurrentWeatherExtractor extends DataExtractor {

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

    default String toDate(String dateStr) {
        String[] dateArr = dateStr.split("T")[0].split("-");
        int year = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int day = Integer.parseInt(dateArr[2]);
        return Util.formatDate(year, month, day);
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
