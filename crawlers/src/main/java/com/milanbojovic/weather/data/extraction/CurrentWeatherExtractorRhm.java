package com.milanbojovic.weather.data.extraction;


import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.data.extraction.template.html.CurrentWeatherExtractor;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.CurrentWeatherColumnsEnum;
import com.milanbojovic.weather.util.Util;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Double.parseDouble;

public class CurrentWeatherExtractorRhm implements CurrentWeatherExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentWeatherExtractorRhm.class);
    CurrentWeather currentWeather;

    public CurrentWeatherExtractorRhm(Element htmlDocument, String dateHeader) {
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
        String temperature = getColumnValue(cityElement, CurrentWeatherColumnsEnum.TEMPERATURE);
        return parseDouble(temperature);
    }

    @Override
    public double getRealFeel(Element cityElement) {
        String realFeel = getColumnValue(cityElement, CurrentWeatherColumnsEnum.REAL_FEEL);
        return parseDouble(realFeel);
    }

    @Override
    public int getHumidity(Element cityElement) {
        String humidity = getColumnValue(cityElement, CurrentWeatherColumnsEnum.HUMIDITY);
        return Integer.parseInt(humidity);
    }

    @Override
    public double getPressure(Element cityElement) {
        String pressure = getColumnValue(cityElement, CurrentWeatherColumnsEnum.PRESSURE);
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
//        Element uvElem = getUvIndexWeatherFor(city);
//        return uvElem == null ? 0 : parseDouble(uvElem.child(1).text());
        return 0;
    }

//    private Element getUvIndexWeatherFor(String city) {
//        LOGGER.debug(format("Fetching UV index data for %s", city));
//        Document uvIndexDoc = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX);
//        Elements uvIndexTable = getUvIndexForAllCities(uvIndexDoc);
//        return findCity(uvIndexTable, city);
//    }

    @Override
    public String getImageUrl(Element cityElement) {
        String imgUrl = cityElement
                .child(CurrentWeatherColumnsEnum.IMAGE.ordinal())
                .childNode(1)
                .attr("src");
        return ConstHelper.RHMZ_URL + "/repository/" + imgUrl.split("repository")[1];

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
        String dateStr = dateHeader.split(" ")[6];
        String[] split = dateStr.split("\\.");
        int year = Integer.parseInt(split[2]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[0]);
        return getDayFromDateString(Util.formatDate(year, month, day)) + " - " + Util.formatDate(year, month, day);
    }

    /*
    @Override
    public String getCurrentDate(String city) {
        String heading = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER)
                .getElementById("sadrzaj")
                .getElementsByTag("h1").get(0)
                .text();
        String dateStr = heading.split(" ")[6];
        String[] split = dateStr.split("\\.");
        int year = Integer.parseInt(split[2]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[0]);
        return Util.formatDate(year, month, day);
    }
    * */


    private String getColumnValue(Element city, CurrentWeatherColumnsEnum column) {
        return city.child(column.ordinal()).text();
    }
}
