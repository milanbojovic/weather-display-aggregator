package com.milanbojovic.weather;

import com.milanbojovic.weather.http.Server;
import com.milanbojovic.weather.spider.AccuWeatherSource;
import com.milanbojovic.weather.spider.RhmzSource;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static final String CITY_BEOGRAD = "Beograd";
    public static final String CITY_NOVI_SAD = "Novi sad";

    public static void main(String[] args) throws IOException {
        LOGGER.info("Main - started");

        List<String> citiesList = Arrays.asList(Util.CITY_BEOGRAD, Util.CITY_NOVI_SAD);

//        AccuWeatherSource acu = new AccuWeatherSource(Arrays.asList(Util.CITY_BEOGRAD, Util.CITY_NOVI_SAD));
//        Weather2UmbrellaSource w2u = new Weather2UmbrellaSource();
//        RhmzSource rhmz = new RhmzSource(citiesList);
//
//        LOGGER.info(acu.toString());
//        LOGGER.info(w2u.toString());
//        LOGGER.info(rhmz.toString());
        Server server = new Server(Arrays.asList(Util.CITY_BEOGRAD, Util.CITY_NOVI_SAD, Util.CITY_KRAGUJEVAC));
        LOGGER.info("Main - finished");
    }
}
