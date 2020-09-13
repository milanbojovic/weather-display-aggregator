package com.milanbojovic.weather.service.weather;

import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.service.persistance.MongoDao;
import com.milanbojovic.weather.service.weather.client.ApiClient;
import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.extraction.api.CurrentWeatherParserAcu;
import com.milanbojovic.weather.data.model.extraction.api.DailyForecastParserAcu;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.Util;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccuWeatherService implements WeatherProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccuWeatherService.class);
    private final AppConfig appConfig;
    private final MongoDao mongoDao;
    private final String providerName;
    private final Map<String, String> documents;

    @Autowired
    public AccuWeatherService(AppConfig appConfig, MongoDao mongoDao) {
        LOGGER.info("Creating AccuWeather Source");
        ApiClient connectionClient = new ApiClient();
        this.appConfig = appConfig;
        this.mongoDao = mongoDao;
        providerName = "ACCU";

        List<String> citiesList = appConfig.getCities();
        documents = citiesList.stream()
                .map(String::toLowerCase)
                .map(this::createRequestForCity)
                .flatMap(Collection::stream)
                .map(connectionClient::provideStringStringPair)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        persistWeatherDataToDb(mongoDao, citiesList, providerName);
    }

    @Override
    public CurrentWeather provideCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", providerName, city));
        String cityJson = documents.get(queryCityUrl(city, appConfig.getAccuWeatherCurrentWeather()));
        CurrentWeatherParserAcu currentWeatherExtractorAcu = new CurrentWeatherParserAcu(cityJson, appConfig);
        return currentWeatherExtractorAcu.extract();
    }

    private String queryCityUrl(String city, String pathPrefix) {
        return pathPrefix + Util.accuWeatherLocationIdMap.get(city.toLowerCase());
    }

    @Override
    public List<DailyForecast> provideWeeklyForecast(String city) {
        LOGGER.debug(String.format("Initializing forecasted weather forecast for %s.", providerName));
        String query = queryCityUrl(city, appConfig.getAccuWeatherWeeklyForecast());
        String weeklyForecastDocJson = documents.get(query);
        List<Map<String, Object>> weeklyForecastsStr = JsonPath.read(weeklyForecastDocJson, "$.DailyForecasts");

        return weeklyForecastsStr.stream()
                .map(this::mapToJson)
                .map(this::mapToDailyForecast)
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    private DailyForecast mapToDailyForecast(String dailyForecast) {
        DailyForecastParserAcu dailyForecastExtractorAcu = new DailyForecastParserAcu(dailyForecast, appConfig);
        DailyForecast extractedDailyForecast = dailyForecastExtractorAcu.extract();
        extractedDailyForecast.setProvider(providerName);
        return extractedDailyForecast;
    }


    private List<HttpRequest> createRequestForCity(String city) {
        return buildAccuWeatherUris(Util.accuWeatherLocationIdMap.get(city.toLowerCase()).toString())
                .stream()
                .map(uriRequest())
                .collect(Collectors.toList());
    }

    private Function<URI, HttpRequest> uriRequest() {
        return uri -> HttpRequest.newBuilder()
                .uri(uri)
                .build();
    }

    private List<URI> buildAccuWeatherUris(String locationId) {
        return Arrays.asList(buildUri("currentconditions", "v1", locationId),
                buildUri("forecasts", "v1", "daily", "5day", locationId));
    }

    private URI buildUri(String... path) {
        LOGGER.debug(MessageFormat.format("Building uris for {0}", providerName));
        try {
            return new URIBuilder(appConfig.getAccuWeatherUrl())
                    .setPathSegments(path)
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_API_KEY, appConfig.getAccuWeatherApiKey())
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_LANGUAGE, "sr-rs")
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_DETAILS_NEEDED, "true")
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_METRIC, "true")
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.error("Error unable to build URI. ", e);
        }
        return null;
    }

    private String mapToJson(Map<String, Object> map) {
        StringWriter out = new StringWriter();
        try {
            JSONValue.writeJSONString(map, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }
}
