package com.milanbojovic.weather.service;

import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.client.ApiClient;
import com.milanbojovic.weather.data.extraction.CurrentWeatherExtractorAcu;
import com.milanbojovic.weather.data.extraction.DailyForecastExtractorAcu;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.Util;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.MissingFormatArgumentException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccuWeatherService implements WeatherProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccuWeatherService.class);
    private final String weatherProvider;
    private final Map<String, String> documents;

    public AccuWeatherService(List<String> cities) {
        weatherProvider = "ACCU";
        LOGGER.info("Creating AccuWeather Source");
        ApiClient connectionClient = new ApiClient();

        documents = cities.stream()
                .map(String::toLowerCase)
                .map(this::createRequestForCity)
                .flatMap(Collection::stream)
                .map(connectionClient::provideStringStringPair)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public CurrentWeather provideCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", weatherProvider, city));
        String cityJson = documents.get(queryCityUrl(city, ConstHelper.ACCU_WEATHER_CURRENT_WEATHER));
        CurrentWeatherExtractorAcu currentWeatherExtractorAcu = new CurrentWeatherExtractorAcu(cityJson);
        return currentWeatherExtractorAcu.extract();
    }

    private String queryCityUrl(String city, String pathPrefix) {
        return pathPrefix + Util.accuWeatherLocationIdMap.get(city.toLowerCase());
    }

    @Override
    public List<DailyForecast> provideWeeklyForecast(String city) {
        LOGGER.debug(String.format("Initializing forecasted weather forecast for %s.", weatherProvider));
        String query = queryCityUrl(city, ConstHelper.ACCU_WEATHER_FIVE_DAY_FORECAST);
        String weeklyForecastDocJson = documents.get(query);
        List<Map<String, Object>> weeklyForecastsStr = JsonPath.read(weeklyForecastDocJson, "$.DailyForecasts");

        return weeklyForecastsStr.stream()
                .map(this::mapToJson)
                .map(this::mapToDailyForecast)
                .collect(Collectors.toList());
    }

    private DailyForecast mapToDailyForecast(String dailyForecast) {
        DailyForecastExtractorAcu dailyForecastExtractorAcu = new DailyForecastExtractorAcu(dailyForecast);
        DailyForecast extractedDailyForecast = dailyForecastExtractorAcu.extract();
        extractedDailyForecast.setProvider(weatherProvider);
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
        Optional<String> apiKey = Optional.ofNullable(System.getenv(ConstHelper.ACCU_WEATHER_API_KEY_ENV_VAR));
        LOGGER.debug(MessageFormat.format("Building uris for {0}", weatherProvider));
        try {
            return new URIBuilder(ConstHelper.ACCU_WEATHER_URL)
                    .setPathSegments(path)
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_API_KEY, apiKey.orElseThrow(missingEnvVarException()))
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_LANGUAGE, "sr-rs")
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_DETAILS_NEEDED, "true")
                    .addParameter(ConstHelper.ACCU_WEATHER_QUERY_PARAM_METRIC, "true")
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.error("Error unable to build URI. ", e);
        }
        return null;
    }

    private Supplier<MissingFormatArgumentException> missingEnvVarException() {
        return () -> new MissingFormatArgumentException("Missing mandatory environment variable " +
                "which should be passed as parameter [" + ConstHelper.ACCU_WEATHER_API_KEY_ENV_VAR + "]!");
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
