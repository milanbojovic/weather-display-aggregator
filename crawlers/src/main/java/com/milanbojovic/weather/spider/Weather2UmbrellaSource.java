package com.milanbojovic.weather.spider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Weather2UmbrellaSource extends WeatherSource {
    static final String URL = "https://www.weather2umbrella.com";
    static final String CITY = "/vremenska-prognoza-beograd-srbija-sr";
    static final String SEVEN_DAY_FORECAST = "/7-dana";
    static final String CURRENT_WEATHER = "/trenutno";

    private final Map<String, Document> documents;

    public Weather2UmbrellaSource() {
        documents = createUriList().stream()
                .map(this::uriDocTuple)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        weatherData = initializeWeatherData();
    }

    private List<String> createUriList() {
        return Arrays.asList(
                URL + CITY + SEVEN_DAY_FORECAST,
                URL + CITY + CURRENT_WEATHER
        );
    }

    private ImmutablePair<String, Document> uriDocTuple(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImmutablePair<>("/" + getCityPath(url) + "/" + getResourcePath(url), document);
    }

    private String getResourcePath(String url) {
        return url.split("/")[url.split("/").length - 1];
    }

    private String getCityPath(String url) {
        return url.split("/")[url.split("/").length - 2];
    }

    private Document getCurrentWeather() {
        return documents.get(CITY + CURRENT_WEATHER);
    }

    private Document getSevenDayWeather() {
        return documents.get(CITY + SEVEN_DAY_FORECAST);
    }

    @Override
    public int getMinTemp() {
        Element today = getCurrentWeekDay();
        Element minTemperatureElem = today.getElementsByClass("day_min").get(0);
        return parseTemperature(minTemperatureElem);
    }

    private int parseTemperature(Element dayMinTemp) {
        String strTemp = Optional.ofNullable(dayMinTemp.text()).orElse("0°");
        return Integer.parseInt(strTemp.substring(0, strTemp.indexOf("°")));
    }

    private Element getCurrentWeekDay() {
        Element sevenDayTable = getSevenDayWeather().getElementById("seven_days").child(1).child(0).child(0);
        return sevenDayTable.getElementsByClass("day_wrap").get(0);
    }

    @Override
    public int getMaxTemp() {
        Element today = getCurrentWeekDay();
        Element dayMinTemp = today.getElementsByClass("day_max").get(0);
        return parseTemperature(dayMinTemp);
    }

    @Override
    public int getRealFeel() {
        Element realFeel = getCurrentWeather().getElementById("feels");
        return parseTemperature(realFeel);
    }

    @Override
    public int getHumidity() {
        Element humidity = getCurrentWeather().getElementById("humidity");
        return parseHumidity(humidity);
    }

    private int parseHumidity(Element humidity) {
        String strHumidity = Optional.ofNullable(humidity.text()).orElse("0%");
        return Integer.parseInt(strHumidity.substring(0, strHumidity.indexOf("%")));
    }

    @Override
    public int getPressure() {
        Element airPresure = getCurrentWeather().getElementById("presure");
        return parseAirPressure(airPresure);
    }

    private int parseAirPressure(Element airPressure) {
        String strPressure = Optional.ofNullable(airPressure.text()).orElse("0.0 mbar");
        return Integer.parseInt(strPressure.substring(0, strPressure.indexOf(" mbar")));
    }

    @Override
    public double getUvIndex() {
        Element today = getCurrentWeekDay();
        Element dayUv = today.getElementsByClass("day_uv").get(0);
        return Double.parseDouble(dayUv.text());
    }

    @Override
    public double getWindSpeed() {
        Element today = getCurrentWeekDay();
        Element dayWindSpeed = today.getElementsByClass("day_wind_speed").get(0);
        return parseWindSpeedTemperature(dayWindSpeed);
    }

    private double parseWindSpeedTemperature(Element windSpeed) {
        String strTemp = Optional.ofNullable(windSpeed.text()).orElse("0.0 m/s");
        return Double.parseDouble(strTemp.substring(0, strTemp.indexOf(" m/s")));
    }

    @Override
    public String getWindDirection() {
        Element today = getCurrentWeekDay();
        Element dayWindSpeed = today.getElementsByClass("day_wind_dir").get(0);
        return dayWindSpeed.text();
    }

    @Override
    public String getDescription() {
        Element today = getCurrentWeekDay();
        Element dayDescription = today.getElementsByClass("day_description").get(0);
        return dayDescription.text();
    }

    @Override
    public String getImageUrl() {
        Element today = getCurrentWeekDay();
        Element dayImage = today.getElementsByClass("day_icon").get(0);
        return dayImage.child(0).attr("src");
    }

    @Override
    public Date getDate() {
        String strDate = getCurrentWeather().getElementById("current_date").text();
        String[] split = strDate.split(" ");

        int year = Integer.parseInt(split[split.length -1]);
        int month = monthToNumber(split[split.length -3]);
        int day = Integer.parseInt(StringUtils.remove(split[split.length -2],"."));

        return new Date(year, month, day);
    }

    private int monthToNumber(String month) {
        switch (month.toLowerCase()) {
            case "jan":
                return 0;
            case "feb":
                return 1;
            case "mar":
                return 2;
            case "apr":
                return 3;
            case "maj":
                return 4;
            case "jun":
                return 5;
            case "jul":
                return 6;
            case "avg":
                return 7;
            case "sep":
                return 8;
            case "okt":
                return 9;
            case "nov":
                return 10;
            case "dec":
                return 11;
            default:
                return 0;
        }
    }
}
