package com.milanbojovic.weather.spider;

import com.sun.istack.internal.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Weather2UmbrellaSource extends WeatherSource {
    static final String URL = "https://www.weather2umbrella.com";
    static final String CITY = "/vremenska-prognoza-beograd-srbija-sr";
    static final String SEVEN_DAY_FORECAST = "/7-dana";
    static final String CURRENT_WEATHER = "/trenutno";

    public static List<String> createList() {
        ArrayList<String> weather2UmbrellaUris = new ArrayList<>();
        weather2UmbrellaUris.add(URL + CITY + SEVEN_DAY_FORECAST);
        weather2UmbrellaUris.add(URL + CITY + CURRENT_WEATHER);
        return weather2UmbrellaUris;
    }

    public Weather2UmbrellaSource() {
        super(Weather2UmbrellaSource.createList());
    }

    @Override
    public Document getCurrentWeather() {
        return documents.get(CITY + CURRENT_WEATHER);
    }

    @Override
    public Document get7DayWeather() {
        return documents.get(CITY + SEVEN_DAY_FORECAST);
    }

    @Override
    public int getMinTemp() {
        Element today = getCurrentWeekDay();
        Element minTemperatureElem = today.getElementsByClass("day_min").get(0);
        return parseTemperature(minTemperatureElem);
    }

    private int parseTemperature(@NotNull Element dayMinTemp) {
        String strTemp = Optional.ofNullable(dayMinTemp.text()).orElse("0°");
        return Integer.parseInt(strTemp.substring(0, strTemp.indexOf("°")));
    }

    private Element getCurrentWeekDay() {
        Element sevenDayTable = get7DayWeather().getElementById("seven_days").child(1).child(0).child(0);
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

    private int parseHumidity(@NotNull Element humidity) {
        String strHumidity = Optional.ofNullable(humidity.text()).orElse("0%");
        return Integer.parseInt(strHumidity.substring(0, strHumidity.indexOf("%")));
    }

    @Override
    public int getPressure() {
        Element airPresure = getCurrentWeather().getElementById("presure");
        return parseAirPressure(airPresure);
    }

    private int parseAirPressure(@NotNull Element airPressure) {
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

    private double parseWindSpeedTemperature(@NotNull Element windSpeed) {
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

    //TODO Fix me please :) boom not working
    @Override
    public Image getImage() {
        Element today = getCurrentWeekDay();
        Element dayImage = today.getElementsByClass("day_icon").get(0);
        String imageUrl = dayImage.child(0).attr("src");

        Image image = null;
        try {
            URL url = new URL(imageUrl);
            image = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected Date getDate() {
        //TODO Serbian date to Java date !
        //String current_date = getCurrentWeather().getElementById("current_date").text();

        return Date.from(Instant.now());
    }
}
