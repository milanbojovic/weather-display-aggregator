package com.milanbojovic.weather.data.model.extraction.html;


import com.milanbojovic.weather.data.model.extraction.html.interfaces.CurrentWeatherParser;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.cyrlat.CyrillicLatinConverter;
import org.jsoup.nodes.Element;

public class CurrentWeatherParserW2U implements CurrentWeatherParser {
    CurrentWeather currentWeather;

    public CurrentWeatherParserW2U(Element htmlDocument) {
        currentWeather = createCurrentWeatherFrom(htmlDocument);
    }

    public CurrentWeather extract() {
        return currentWeather;
    }

    @Override
    public double getTemp(Element doc) {
        Element currTempStr = doc.getElementsByClass("current_temperature_data").get(0);
        return parseTemperature(currTempStr);
    }

    @Override
    public double getRealFeel(Element doc) {
        Element currRealFeel = doc.getElementById("feels");
        return parseTemperature(currRealFeel);
    }

    @Override
    public int getHumidity(Element doc) {
        Element currHumidity = doc.getElementById("humidity");
        return parseHumidity(currHumidity);
    }

    @Override
    public double getPressure(Element doc) {
        Element airPresure = doc.getElementById("presure");
        return parseAirPressure(airPresure);
    }

    @Override
    public double getWindSpeed(Element doc) {
        Element currWindSpeed = doc.getElementById("wind");
        return parseWindSpeed(currWindSpeed);
    }

    @Override
    public String getWindDirection(Element doc) {
        Element currWindDirection = doc.getElementById("wind");
        return currWindDirection.text().split(" ")[3];
    }

    @Override
    public double getUvIndex(Element doc) {
        Element dayUv = doc.getElementById("uv");
        String uvText = dayUv.text().equals("-")? "0" : dayUv.text();
        return Double.parseDouble(uvText);
    }

    @Override
    public String getImageUrl(Element doc) {
        Element dayImage = doc.getElementsByClass("current_wether_icon").first()
                .getElementsByTag("img").first();
        return dayImage.attr("src");
    }

    @Override
    public String getDescription(Element doc) {
        Element dayDescription = doc.getElementsByClass("weather_icon_decription").first();
        return CyrillicLatinConverter.latinToCyrillic(dayDescription.text());
    }

    @Override
    public String getDate(Element doc) {
        String strDate = doc.getElementById("current_date").text();
        String[] split = strDate.split(" ");

        int year = Integer.parseInt(split[split.length -1]);
        int month = Util.monthNumberMap.get(split[split.length - 3].toLowerCase());
        int day = Integer.parseInt(StringUtils.remove(split[split.length -2],"."));
        String date = Util.formatDate(year, month, day);
        return getDayFromDateString(date) + " - " + date;
    }
}
