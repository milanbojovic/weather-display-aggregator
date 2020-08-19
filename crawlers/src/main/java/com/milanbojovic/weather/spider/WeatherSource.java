package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.data.WeatherData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class WeatherSource {
    protected final Map<String, Document> documents;
    private final WeatherData weatherData;

    public WeatherData getWeatherData() {
        return weatherData;
    }

    public WeatherSource(List<String> urls) {
        documents = urls.stream()
                .map(this::uriDocTuple)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));

        weatherData = new WeatherData();
        weatherData.setMinTemp(this.getMinTemp());
        weatherData.setMaxTemp(this.getMaxTemp());
        weatherData.setUvIndex(this.getUvIndex());
        weatherData.setWindSpeed(this.getWindSpeed());
        weatherData.setWindDirection(this.getWindDirection());
        weatherData.setDescription(this.getDescription());
        weatherData.setImage(this.getImage());

        weatherData.setRealFeel(this.getRealFeel());
        weatherData.setHumidity(this.getHumidity());
        weatherData.setPressure(this.getPressure());
        weatherData.setDescription(this.getDescription());
        weatherData.setDate(this.getDate());
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

    public abstract Document getCurrentWeather();

    public abstract Document get7DayWeather();

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

    protected abstract Date getDate();
}
