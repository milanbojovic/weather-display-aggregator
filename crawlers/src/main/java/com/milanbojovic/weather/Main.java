package com.milanbojovic.weather;

import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main - started");

        Weather2UmbrellaSource w2umbrella = new Weather2UmbrellaSource();

        System.out.println(w2umbrella.getWeatherData());

        System.out.println("Main - finished");
    }
}
