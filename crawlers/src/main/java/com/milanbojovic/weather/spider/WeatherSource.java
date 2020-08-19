package com.milanbojovic.weather.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public abstract class WeatherSource {

    private String url;
    private Document document;

    public WeatherSource(String url) {
        this.url = url;
        try {
            document = Jsoup.connect(this.url).get();
        } catch (IOException e) {
            //TODO add logback logging
            e.printStackTrace();
        }
    }

    protected Element getBody() {
        return document.body();
    }

    public abstract int getMinTemp();

    public abstract int getMaxTemp();

    public abstract int getRealFeel();

    public abstract int getHumidity();

    public abstract int getPressure();

    public abstract double getUvIndex();

    public abstract  double getWindSpeed();

    public abstract String getWindDirection();

    public abstract String getDescription();

    public abstract Image getImage();

}
