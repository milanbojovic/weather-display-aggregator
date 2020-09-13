package com.milanbojovic.weather;

import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.service.weather.AccuWeatherService;
import com.milanbojovic.weather.service.persistance.MongoDao;
import com.milanbojovic.weather.service.weather.RhmzService;
import com.milanbojovic.weather.service.weather.Weather2UmbrellaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class WeatherApp {
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RhmzService rhmzService;

    @Autowired
    private AccuWeatherService accuWeatherService;

    @Autowired
    private Weather2UmbrellaService weather2UmbrellaService;

    @Autowired
    private MongoDao mongoDao;

    @RequestMapping("/config")
    AppConfig config() {
        return appConfig;
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherApp.class, args);
    }
}

