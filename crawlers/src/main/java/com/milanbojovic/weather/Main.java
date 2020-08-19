package com.milanbojovic.weather;

import com.milanbojovic.weather.data.WeatherData;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main - started");

        final String w2umbrellaUrl = "https://www.weather2umbrella.com/vremenska-prognoza-beograd-srbija-sr/7-dana";
        Weather2UmbrellaSource w2umbrella = new Weather2UmbrellaSource(w2umbrellaUrl);

        WeatherData weatherData = new WeatherData();
        weatherData.setMinTemp(w2umbrella.getMinTemp());
        weatherData.setMaxTemp(w2umbrella.getMaxTemp());
        weatherData.setUvIndex(w2umbrella.getUvIndex());
        weatherData.setWindSpeed(w2umbrella.getWindSpeed());
        weatherData.setWindDirection(w2umbrella.getWindDirection());
        weatherData.setDescription(w2umbrella.getDescription());
        weatherData.setImage(w2umbrella.getImage());



        System.out.println("Minimalna temperatura za danas je: " + weatherData.getMinTemp() + "°C.");
        System.out.println("Maksimalna temperatura za danas je: " + weatherData.getMaxTemp() + "°C.");
        System.out.println("Danasnji uv index je: " + weatherData.getUvIndex() + ".");
        System.out.println("Brzina vetra je: " + weatherData.getWindSpeed() + "m/s.");
        System.out.println("Pravac vetra je: " + weatherData.getWindDirection() + ".");
        System.out.println(weatherData.getDescription() + ".");
        System.out.println(weatherData.getImage() + ".");
        System.out.println("Main - finished.");
    }
}
