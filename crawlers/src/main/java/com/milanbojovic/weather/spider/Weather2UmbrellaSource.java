package com.milanbojovic.weather.spider;

import com.sun.istack.internal.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class Weather2UmbrellaSource extends WeatherSource {

    public Weather2UmbrellaSource(String url) {
        super(url);
    }

    @Override
    public int getMinTemp() {
        Element today = getCurrentDay();
        Element minTemperatureElem = today.getElementsByClass("day_min").get(0);
        return parseTemperature(minTemperatureElem);
    }

    private int parseTemperature(@NotNull Element dayMinTemp) {
        String strTemp = Optional.ofNullable(dayMinTemp.text()).orElse("0°");
        return Integer.parseInt(strTemp.substring(0, strTemp.indexOf("°")));
    }

    private Element getCurrentDay() {
        Element seven_days_table = getBody().getElementById("seven_days").child(1).child(0).child(0);
        Elements days = seven_days_table.getElementsByClass("day_wrap");
        Element today = days.get(0);
        return today;
    }

    @Override
    public int getMaxTemp() {
        Element today = getCurrentDay();
        Element dayMinTemp = today.getElementsByClass("day_max").get(0);
        return parseTemperature(dayMinTemp);
    }

    @Override
    public int getRealFeel() {
        return 0;
    }

    @Override
    public int getHumidity() {
        return 0;
    }

    @Override
    public int getPressure() {
        return 0;
    }

    @Override
    public double getUvIndex() {
        Element today = getCurrentDay();
        Element dayUv = today.getElementsByClass("day_uv").get(0);
        return Double.parseDouble(dayUv.text());
    }

    @Override
    public double getWindSpeed() {
        Element today = getCurrentDay();
        Element dayWindSpeed = today.getElementsByClass("day_wind_speed").get(0);
        return parseWindSpeedTemperature(dayWindSpeed);
    }

    private double parseWindSpeedTemperature(@NotNull Element windSpeed) {
        String strTemp = Optional.ofNullable(windSpeed.text()).orElse("0.0 m/s");
        return Double.parseDouble(strTemp.substring(0, strTemp.indexOf(" m/s")));
    }

    @Override
    public String getWindDirection() {
        Element today = getCurrentDay();
        Element dayWindSpeed = today.getElementsByClass("day_wind_dir").get(0);
        return dayWindSpeed.text();
    }

    @Override
    public String getDescription() {
        Element today = getCurrentDay();
        Element dayDescription = today.getElementsByClass("day_description").get(0);
        return dayDescription.text();
    }

    //TODO Fix me please :) boom not working
    @Override
    public Image getImage() {
        Element today = getCurrentDay();
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
}
