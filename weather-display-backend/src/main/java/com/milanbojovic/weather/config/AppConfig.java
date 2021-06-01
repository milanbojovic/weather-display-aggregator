package com.milanbojovic.weather.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@ConfigurationProperties("app")
@Getter
@Setter
public class AppConfig {
    //HTTP SERVER
    private String serverUrl = "localhost";
    private String serverPort = "8080";
    private String serverRouteRhmz = "rhmz";
    private String serverRouteW2u = "w2u";
    private String serverRouteAccu = "accu";

    //ACCU WEATHER
    private String accuWeatherWeeklyForecast = "/forecasts/v1/daily/5day/";
    private String accuWeatherUrl = "http://dataservice.accuweather.com";
    private String accuWeatherImageFetchUrl = "http://developer.accuweather.com";
    private String accuWeatherCurrentWeather = "/currentconditions/v1/";

    //RHMZ
    private String rhmzUrl = "http://hidmet.gov.rs";
    private String rhmzCurrentWeather = "/osmotreni/index.php";
    private String rhmzWeeklyForecast = "/prognoza/stanica.php?mp_id=%s";
    private String rhmzContentNotFoundImage = "https://icon-library.net/images/error-icon-png/error-icon-png-4.jpg";

    //WEATHER TO UMBRELLA
    private String w2uUrl = "https://www.weather2umbrella.com";
    private String w2uCurrentWeather = "/trenutno";
    private String w2uWeeklyForecast = "/7-dana";

    //DATABASE
    private String mongoConnectionUrl = "mongodb://localhost:27017";

    //OTHER
    private List<Integer> citiesLocationIds = Collections.singletonList(298198);
    private String accuWeatherApiKey = "no default key";
    private List<String> cities = Collections.singletonList("Beograd");
}
