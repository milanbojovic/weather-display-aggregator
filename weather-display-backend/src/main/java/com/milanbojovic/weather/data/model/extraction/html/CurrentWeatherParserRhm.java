package com.milanbojovic.weather.data.model.extraction.html;


import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.extraction.html.interfaces.CurrentWeatherParser;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.CurrentWeatherColumnsEnum;
import com.milanbojovic.weather.util.Util;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Double.parseDouble;

public class CurrentWeatherParserRhm implements CurrentWeatherParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentWeatherParserRhm.class);
    CurrentWeather currentWeather;
    private final AppConfig appConfig;

    public CurrentWeatherParserRhm(Element htmlDocument, String dateHeader, AppConfig appConfig) {
        this.appConfig = appConfig;
        currentWeather = createCurrentWeatherFrom(htmlDocument, dateHeader);
    }

    CurrentWeather createCurrentWeatherFrom(Element doc, String dateHeader) {
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
                .date(getDate(dateHeader))
                .build();
    }

    public CurrentWeather extract() {
        return currentWeather;
    }

    @Override
    public double getTemp(Element cityElement) {
        var temperature = getColumnValue(cityElement, CurrentWeatherColumnsEnum.TEMPERATURE);
        return parseDouble(temperature);
    }

    @Override
    public double getRealFeel(Element cityElement) {
        var realFeel = getColumnValue(cityElement, CurrentWeatherColumnsEnum.REAL_FEEL);
        return parseDouble(realFeel);
    }

    @Override
    public int getHumidity(Element cityElement) {
        var humidity = getColumnValue(cityElement, CurrentWeatherColumnsEnum.HUMIDITY);
        return Integer.parseInt(humidity);
    }

    @Override
    public double getPressure(Element cityElement) {
        var pressure = getColumnValue(cityElement, CurrentWeatherColumnsEnum.PRESSURE);
        return parseDouble(pressure);

    }

    @Override
    public double getWindSpeed(Element cityElement) {
        String windSpeedStr = getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_SPEED);
        double windSpeed = 0;
        try{
            windSpeed = parseDouble(windSpeedStr);
        } catch (NumberFormatException e) {
            LOGGER.error(String.format("Error while converting wind speed value to double: %s", windSpeedStr));
        }
        return windSpeed;

    }

    @Override
    public String getWindDirection(Element cityElement) {
        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_DIRECTION);
    }

    @Override
    public double getUvIndex(Element cityElement) {
        return 0;
    }

    @Override
    public String getImageUrl(Element cityElement) {
        var imgUrl = cityElement
                .child(CurrentWeatherColumnsEnum.IMAGE.ordinal())
                .childNode(1)
                .attr("src");
        return appConfig.getRhmzUrl() + "/repository/" + imgUrl.split("repository")[1];
    }

    @Override
    public String getDescription(Element cityElement) {
        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.DESCRIPTION);
    }

    @Override
    public String getDate(Element doc) {
        return null;
    }

    public String getDate(String dateHeader) {
        var dateStr = dateHeader.split(" ")[6];
        var split = dateStr.split("\\.");
        var year = Integer.parseInt(split[2]);
        var month = Integer.parseInt(split[1]);
        var day = Integer.parseInt(split[0]);
        var date = Util.formatDate(year, month, day);
        return getDayFromDateString(date) + " - " + date;
    }

    private String getColumnValue(Element city, CurrentWeatherColumnsEnum column) {
        return city.child(column.ordinal()).text();
    }
}
