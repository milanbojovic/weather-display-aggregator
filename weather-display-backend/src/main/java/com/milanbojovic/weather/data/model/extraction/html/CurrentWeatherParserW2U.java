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
        var currTempStr = doc.getElementsByClass("current_temperature_data").get(0);
        return parseTemperature(currTempStr);
    }

    @Override
    public double getRealFeel(Element doc) {
        var currRealFeel = doc.getElementById("feels");
        return parseTemperature(currRealFeel);
    }

    @Override
    public int getHumidity(Element doc) {
        var currHumidity = doc.getElementById("humidity");
        return parseHumidity(currHumidity);
    }

    @Override
    public double getPressure(Element doc) {
        var airPresure = doc.getElementById("presure");
        return parseAirPressure(airPresure);
    }

    @Override
    public double getWindSpeed(Element doc) {
        var currWindSpeed = doc.getElementById("wind");
        return parseWindSpeed(currWindSpeed);
    }

    @Override
    public String getWindDirection(Element doc) {
        var currWindDirection = doc.getElementById("wind");
        return currWindDirection.text().split(" ")[3];
    }

    @Override
    public double getUvIndex(Element doc) {
        var dayUv = doc.getElementById("uv");
        var uvText = dayUv.text().equals("-")? "0" : dayUv.text();
        return Double.parseDouble(uvText);
    }

    @Override
    public String getImageUrl(Element doc) {
        var dayImage = doc.getElementsByClass("current_wether_icon").first()
                .getElementsByTag("img").first();
        return dayImage.attr("src");
    }

    @Override
    public String getDescription(Element doc) {
        var dayDescription = doc.getElementsByClass("weather_icon_decription").first();
        return CyrillicLatinConverter.latinToCyrillic(dayDescription.text());
    }

    @Override
    public String getDate(Element doc) {
        var strDate = doc.getElementById("current_date").text();
        var split = strDate.split(" ");

        var year = Integer.parseInt(split[split.length -1]);
        var month = Util.monthNumberMap.get(split[split.length - 3].toLowerCase());
        var day = Integer.parseInt(StringUtils.remove(split[split.length -2],"."));
        var date = Util.formatDate(year, month, day);
        return getDayFromDateString(date) + " - " + date;
    }
}
