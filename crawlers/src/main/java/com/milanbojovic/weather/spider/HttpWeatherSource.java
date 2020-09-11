package com.milanbojovic.weather.spider;

import org.jsoup.nodes.Document;

public interface HttpWeatherSource {
    Document provideCurrentWeatherDocument(String city);

    Document provideWeeklyForecastDocument(String city);
}
