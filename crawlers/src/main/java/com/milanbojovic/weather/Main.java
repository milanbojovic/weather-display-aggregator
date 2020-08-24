package com.milanbojovic.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milanbojovic.weather.spider.AccuWeatherSource;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        System.out.println("Main - started");

        Weather2UmbrellaSource w2umbrella = new Weather2UmbrellaSource();
//        AccuWeatherSource accuWeather = new AccuWeatherSource();



        ObjectMapper mapper = new ObjectMapper();
        //Converting the Object to JSONString
        String jsonString = mapper.writeValueAsString(w2umbrella.getWeatherData());

//        System.out.println(w2umbrella.getWeatherData());
//        System.out.println(accuWeather.getWeatherData());

        System.out.println("Main - finished");
    }
}
