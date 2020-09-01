package com.milanbojovic.weather;

import com.milanbojovic.weather.spider.AccuWeatherSource;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        System.out.println("Main - started");

        AccuWeatherSource acu = new AccuWeatherSource(Arrays.asList("Beograd"));
//        Weather2UmbrellaSource w2u = new Weather2UmbrellaSource(Arrays.asList("Beograd"));
        LOGGER.info(acu.toString());



//        Server server = new Server();
        System.out.println("Main - finished");
    }
}
