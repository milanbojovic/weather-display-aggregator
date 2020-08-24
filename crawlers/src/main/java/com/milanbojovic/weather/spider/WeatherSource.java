package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.data.WeatherData;
import org.jsoup.nodes.Document;

import java.util.Date;

public abstract class WeatherSource {
    protected WeatherData weatherData = null;

    public WeatherSource() {
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }

    protected WeatherData initializeWeatherData() {
        return WeatherData.builder()
                .minTemp(getMinTemp())
                .maxTemp(getMaxTemp())
                .uvIndex(getUvIndex())
                .windSpeed(getWindSpeed())
                .windDirection(getWindDirection())
                .description(getDescription())
                .imageUrl(getImageUrl())
                .realFeel(getRealFeel())
                .humidity(getHumidity())
                .pressure(getPressure())
                .description(getDescription())
                .date(getDate())
                .build();
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

    public abstract String getImageUrl();

    public abstract Date getDate();
}
