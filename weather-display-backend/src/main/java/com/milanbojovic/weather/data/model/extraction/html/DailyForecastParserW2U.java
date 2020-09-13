package com.milanbojovic.weather.data.model.extraction.html;

import com.milanbojovic.weather.data.model.extraction.html.interfaces.DailyForecastParser;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.Util;
import org.cyrlat.CyrillicLatinConverter;
import org.jsoup.nodes.Element;

import java.util.Calendar;

public class DailyForecastParserW2U implements DailyForecastParser {

    private final DailyForecast dailyForecast;

    public DailyForecastParserW2U(Element htmlDocument) {
        dailyForecast = createDailyForecastFor(htmlDocument);
    }

    public DailyForecast extract() {
        return dailyForecast;
    }

    @Override
    public double getMinTemp(Element doc) {
        Element dayMinTemp = doc.getElementsByClass("day_min").get(0);
        return parseTemperature(dayMinTemp);
    }

    @Override
    public double getMaxTemp(Element doc) {
        Element dayMinTemp = doc.getElementsByClass("day_max").get(0);
        return parseTemperature(dayMinTemp);
    }

    @Override
    public double getWindSpeed(Element doc) {
        Element dayWindSpeed = doc.getElementsByClass("day_wind_speed").get(0);
        return parseWindSpeed(dayWindSpeed);
    }

    @Override
    public String getWindDirection(Element doc) {
        Element dayWindDirection = doc.getElementsByClass("day_wind_dir").get(0);
        return dayWindDirection.text();
    }

    @Override
    public double getUvIndex(Element doc) {
        Element dayUv = doc.getElementsByClass("day_uv").get(0);
        return Double.parseDouble(dayUv.text());
    }

    @Override
    public String getDescription(Element doc) {
        Element dayDescription = doc.getElementsByClass("day_description").get(0);
        return CyrillicLatinConverter.latinToCyrillic(dayDescription.text());
    }

    @Override
    public String getDate(Element doc) {
        String dayLabel = doc.getElementsByClass("day_label").get(0).text();
        int day = Integer.parseInt(dayLabel.split(" ")[1].substring(0, 2));
        int month = Util.monthNumberMap.get(dayLabel.split(" ")[2].trim().toLowerCase());
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String dayString = dayLabel.split(" ")[0].trim();
        return CyrillicLatinConverter.latinToCyrillic(dayString) + " - " + Util.formatDate(year, month, day);
    }

    @Override
    public String getImageUrl(Element doc) {
        Element dayImage = doc.getElementsByClass("day_icon").get(0);
        return dayImage.child(0).attr("src");
    }
}