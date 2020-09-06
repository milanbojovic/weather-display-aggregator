package com.milanbojovic.weather;

import com.milanbojovic.weather.http.Server;
import com.milanbojovic.weather.spider.RhmzSource;
import com.milanbojovic.weather.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("Main - started");

        List<String> citiesList = Arrays.asList(
                Util.CITY_BEOGRAD,
                Util.CITY_NOVI_SAD,
                Util.CITY_KRAGUJEVAC,
                Util.CITY_NIS,
                Util.CITY_ZLATIBOR
        );

        Server server = new Server(citiesList);
        LOGGER.info("Main - finished");
    }
}
