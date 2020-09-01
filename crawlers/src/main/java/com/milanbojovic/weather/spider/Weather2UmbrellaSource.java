package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cyrlat.CyrillicLatinConverter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Weather2UmbrellaSource extends AbstractWeatherSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(Weather2UmbrellaSource.class);
    private final Map<String, Document> documents;

    public Weather2UmbrellaSource(List<String> cities) {
        super("Weather2Umbrella Provider");
        LOGGER.info("Creating Weather2Umbrella Source");

        documents = cities.stream()
                .map(String::toLowerCase)
                .map(city -> city.replace(" ", "-"))
                .map(this::buildWeather2UmbrellaUris)
                .flatMap(Collection::stream)
                .map(url -> this.requestUriToResponseDocTuple(url, this::getMapKey))
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        persistAllWeatherDataToMap(cities);
    }

    private List<String> buildWeather2UmbrellaUris(String city) {
        return Arrays.asList(
                ConstHelper.W2U_URL + String.format(ConstHelper.W2U_CITY, city) + ConstHelper.W2U_SEVEN_DAY_FORECAST,
                ConstHelper.W2U_URL + String.format(ConstHelper.W2U_CITY, city) + ConstHelper.W2U_CURRENT_WEATHER
        );
    }

    private String getMapKey(String url) {
        return "/" + getCityPath(url) + "/" + getResourcePath(url);
    }

    private String getResourcePath(String url) {
        return url.split("/")[url.split("/").length - 1];
    }

    private String getCityPath(String url) {
        return url.split("/")[url.split("/").length - 2];
    }

    private Document getCurrentWeatherDocFor(String city) {
        return documents.get(String.format(ConstHelper.W2U_CITY, city.replace(" ", "-")) + ConstHelper.W2U_CURRENT_WEATHER);
    }

    private Document getSevenDayWeatherDocFor(String city) {
        return documents.get(String.format(ConstHelper.W2U_CITY, city.replace(" ", "-")) + ConstHelper.W2U_SEVEN_DAY_FORECAST);
    }

    @Override
    protected Elements getWeeklyForecast(String city) {
        Document sevenDayWeatherDoc = getSevenDayWeatherDocFor(city);
        return sevenDayWeatherDoc.getElementsByClass("day_wrap")
                .stream()
                .limit(5)
                .collect(Collectors.toCollection(Elements::new));
    }

    @Override
    protected double getCurrentTemp(String city) {
        Element currTempStr = getCurrentWeatherDocFor(city).getElementsByClass("current_temperature_data").get(0);
        return parseTemperature(currTempStr);
    }

    @Override
    public double getCurrentRealFeel(String city) {
        Element currRealFeel = getCurrentWeatherDocFor(city).getElementById("feels");
        return parseTemperature(currRealFeel);
    }

    @Override
    public int getCurrentHumidity(String city) {
        Element currHumidity = getCurrentWeatherDocFor(city).getElementById("humidity");
        return parseHumidity(currHumidity);
    }

    private int parseHumidity(Element humidity) {
        String strHumidity = Optional.ofNullable(humidity.text()).orElse("0%");
        return Integer.parseInt(strHumidity.substring(0, strHumidity.indexOf("%")));
    }

    @Override
    public double getCurrentPressure(String city) {
        Element airPresure = getCurrentWeatherDocFor(city).getElementById("presure");
        return parseAirPressure(airPresure);
    }

    private int parseAirPressure(Element airPressure) {
        String strPressure = Optional.ofNullable(airPressure.text()).orElse("0.0 mbar");
        return Integer.parseInt(strPressure.substring(0, strPressure.indexOf(" mbar")));
    }

    @Override
    public double getCurrentUvIndex(String city) {
        Element dayUv = getCurrentWeatherDocFor(city).getElementById("uv");
        String uvText = dayUv.text().equals("-")? "0" : dayUv.text();
        return Double.parseDouble(uvText);
    }

    @Override
    public double getCurrentWindSpeed(String city) {
        Element currWindSpeed = getCurrentWeatherDocFor(city).getElementById("wind");
        return parseWindSpeed(currWindSpeed);
    }

    private double parseWindSpeed(Element windSpeed) {
        String windSpeedStr = windSpeed.text().equals("-")? "0.0 m/s" : windSpeed.text();
        return Double.parseDouble(windSpeedStr.split(" ")[0]);
    }

    @Override
    public String getCurrentWindDirection(String city) {
        Element currWindDirection = getCurrentWeatherDocFor(city).getElementById("wind");
        return currWindDirection.text().split(" ")[3];
    }

    @Override
    public String getCurrentDescription(String city) {
        Element dayDescription = getCurrentWeatherDocFor(city).getElementsByClass("weather_icon_decription").first();
        return dayDescription.text();
    }

    @Override
    public String getCurrentImageUrl(String city) {
        Element dayImage = getCurrentWeatherDocFor(city).getElementsByClass("current_wether_icon").first()
                .getElementsByTag("img").first();
        return dayImage.attr("src");
    }

    @Override
    public String getCurrentDate(String city) {
        String strDate = getCurrentWeatherDocFor(city).getElementById("current_date").text();
        String[] split = strDate.split(" ");

        int year = Integer.parseInt(split[split.length -1]);
        int month = Util.monthNumberMap.get(split[split.length - 3].toLowerCase());
        int day = Integer.parseInt(StringUtils.remove(split[split.length -2],"."));
        return year + "-" + month + "-" + day;
    }

    private double parseTemperature(Element dayMinTemp) {
        String strTemp = Optional.ofNullable(dayMinTemp.text()).orElse("0°");
        return Double.parseDouble(strTemp.substring(0, strTemp.indexOf("°")));
    }

    @Override
    public double getForecastedMinTemp(Element element) {
        Element dayMinTemp = element.getElementsByClass("day_min").get(0);
        return parseTemperature(dayMinTemp);
    }

    @Override
    public double getForecastedMaxTemp(Element element) {
        Element dayMinTemp = element.getElementsByClass("day_max").get(0);
        return parseTemperature(dayMinTemp);
    }


    @Override
    public double getForecastedWindSpeed(Element element) {
        Element dayWindSpeed = element.getElementsByClass("day_wind_speed").get(0);
        return parseWindSpeed(dayWindSpeed);
    }


    @Override
    public String getForecastedWindDirection(Element element) {
        Element dayWindDirection = element.getElementsByClass("day_wind_dir").get(0);
        return dayWindDirection.text();
    }

    @Override
    public double getForecastedUvIndex(Element element) {
        Element dayUv = element.getElementsByClass("day_uv").get(0);
        return Double.parseDouble(dayUv.text());
    }

    @Override
    public String getForecastedDescription(Element element) {
        Element dayDescription = element.getElementsByClass("day_description").get(0);
        return CyrillicLatinConverter.latinToCyrillic(dayDescription.text());
    }

    @Override
    public String getForecastedDate(Element element) {
        String dayLabel = element.getElementsByClass("day_label").get(0).text();
        int day = Integer.parseInt(dayLabel.split(" ")[1].substring(0, 2));
        int month = Util.monthNumberMap.get(dayLabel.split(" ")[2].trim().toLowerCase());
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return year + "-" + month + "-" + day;
    }

    @Override
    public String getForecastedImageUrl(Element element) {
        Element dayImage = element.getElementsByClass("day_icon").get(0);
        return dayImage.child(0).attr("src");
    }

    @Override
    public String getForecastedDay(Element element) {
        String dayLabel = element.getElementsByClass("day_label").get(0).text();
        return dayLabel.split(" ")[0].trim();
    }

    //NULLS NOT OVERRIDED NOT NEEDED

    @Override
    public double getForecastedMaxTemp(String city) {
        return 0;
    }


    @Override
    public double getForecastedMinTemp(String city) {
        return 0;
    }


    @Override
    public double getForecastedWindSpeed(String city) {
        return 0;
    }

    @Override
    public String getForecastedWindDirection(String city) {
        return null;
    }

    @Override
    public double getForecastedUvIndex(String city) {
        return 0;
    }

    @Override
    public String getForecastedDescription(String city) {
        return null;
    }

    @Override
    public String getForecastedDate(String city) {
        return null;
    }

    @Override
    public String getForecastedImageUrl(String city) {
        return null;
    }

    @Override
    public String toString() {
        return "Weather2UmbrellaSource{" +
                "weatherDataMap=" + weatherDataMap +
                '}';
    }
}
