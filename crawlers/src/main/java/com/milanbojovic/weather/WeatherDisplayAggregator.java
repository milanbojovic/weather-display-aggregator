package com.milanbojovic.weather;

import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.MongoDao;
import com.milanbojovic.weather.service.AccuWeatherService;
import com.milanbojovic.weather.service.RhmzService;
import com.milanbojovic.weather.service.Weather2UmbrellaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class WeatherDisplayAggregator {
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
        SpringApplication.run(WeatherDisplayAggregator.class, args);
    }
}

