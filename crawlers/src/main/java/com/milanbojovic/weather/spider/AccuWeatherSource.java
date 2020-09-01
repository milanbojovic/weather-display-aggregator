package com.milanbojovic.weather.spider;

import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.data.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.Util;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccuWeatherSource extends AbstractWeatherSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccuWeatherSource.class);
    private final HttpClient httpClient;
    private Map<String, String> documents;

    public AccuWeatherSource(List<String> cities) {
        super("AccuWeather Provider");
        LOGGER.info("Creating AccuWeather Source");
        httpClient = HttpClient.newHttpClient();
        try {
            documents = cities.stream()
                    .map(String::toLowerCase)
                    .map(this::createRequestForCity)
                    .flatMap(Collection::stream)
                    .map(this::queryAccuWeatherApi)
                    .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
            persistAllWeatherDataToMap(cities);
        } catch (Exception e) {
            LOGGER.error("Error occurred while assembling weather data for AccuWeather Source");
            e.printStackTrace();
        }
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

    private ImmutablePair<String, String> queryAccuWeatherApi(HttpRequest request) {
        Optional<HttpResponse<String>> response = Optional.empty();
        try {
            LOGGER.debug(String.format("Executing API request: %s", request.toString()));
            response = Optional.of(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            LOGGER.error("Error occured while executing http request.", e);
        }
        return new ImmutablePair<>(request.uri().getPath(), getResponseString(response));
    }

    private String getResponseString(Optional<HttpResponse<String>> response) {
        return response.isPresent() ? response.get().body() : "Empty";
    }

    private Supplier<MissingFormatArgumentException> missingEnvVarException() {
        return () -> new MissingFormatArgumentException("Missing mandatory environment variable " +
                "which should be passed as parameter [" + ConstHelper.ACCU_WEATHER_API_KEY_ENV_VAR + "]!");
    }

    private String getCurrentWeatherFor(String city) {
        return documents.get("/currentconditions/v1/" + Util.accuWeatherLocationIdMap.get(city.toLowerCase()));
    }

    private String getFiveDayWeatherFor(String city) {
        return documents.get("/forecasts/v1/daily/5day/" + Util.accuWeatherLocationIdMap.get(city.toLowerCase()));
    }

    @Override
    protected Elements getWeeklyForecast(String city) {
        return null;
    }

    protected List<Map<String, Object>> getWeeklyForecastString(String city) {
        String fiveDayWeather = getFiveDayWeatherFor(city);
        return JsonPath.read(fiveDayWeather, "$.DailyForecasts");
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

    @Override
    protected List<DailyForecast> initializeDailyForecast(String city) {
        LOGGER.debug(String.format("Initializing weather data for %s.", weatherProvider));
        List<Map<String, Object>> weeklyForecastsStr = getWeeklyForecastString(city);
        return weeklyForecastsStr.stream()
                .map(this::mapToJson)
                .map(this::buildDailyForecastFor)
                .collect(Collectors.toList());
    }

    protected DailyForecast buildDailyForecastFor(String json) {
        return DailyForecast.builder()
                .minTemp(getForecastedMinTemp(json))
                .maxTemp(getForecastedMaxTemp(json))
                .windSpeed(getForecastedWindSpeed(json))
                .windDirection(getForecastedWindDirection(json))
                .uvIndex(getForecastedUvIndex(json))
                .description(getForecastedDescription(json))
                .date(getForecastedDate(json))
                .day(getForecastedDay(json))
                .imageUrl(getForecastedImageUrl(json))
                .build();
    }

    //CURRENT WEATHER DATA

    @Override
    protected double getCurrentTemp(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].Temperature.Metric.Value");
    }

    @Override
    public double getCurrentRealFeel(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].RealFeelTemperature.Metric.Value");
    }

    @Override
    public int getCurrentHumidity(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].RelativeHumidity");
    }

    @Override
    public double getCurrentPressure(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].Pressure.Metric.Value");
    }

    @Override
    public double getCurrentUvIndex(String city) {
        int uvIndex = JsonPath.read(getCurrentWeatherFor(city), "$[0].UVIndex");
        return Double.parseDouble(Integer.toString(uvIndex));
    }

    @Override
    public String getCurrentImageUrl(String city) {
        String imgId = JsonPath.read(getCurrentWeatherFor(city), "$[0].WeatherIcon").toString();
        return String.format(ConstHelper.ACCU_WEATHER_URL + ConstHelper.ACCU_WEATHER_API_IMAGES_LOCATION, imgId);
    }

    @Override
    public double getCurrentWindSpeed(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].Wind.Speed.Metric.Value");
    }

    @Override
    public String getCurrentWindDirection(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].Wind.Direction.English");
    }

    @Override
    public String getCurrentDescription(String city) {
        return JsonPath.read(getCurrentWeatherFor(city), "$[0].WeatherText");
    }

    @Override
    public String getCurrentDate(String city) {
        String dateStr = JsonPath.read(getCurrentWeatherFor(city), "$[0].LocalObservationDateTime");
        return toDate(dateStr);
    }

    private String toDate(String dateStr) {
        String[] dateArr = dateStr.split("T")[0].split("-");
        int year = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int day = Integer.parseInt(dateArr[2]);
        return year + "-" + month + "-" + day;
    }

    //FORECASTED WEATHER DATA

    @Override
    public double getForecastedMinTemp(String element) {
        return JsonPath.read(element, "$.Temperature.Minimum.Value");
    }

    @Override
    public double getForecastedMaxTemp(String element) {
        return JsonPath.read(element, "$.Temperature.Maximum.Value");
    }

    @Override
    public String getForecastedWindDirection(String element) {
        return JsonPath.read(element, "$.Day.Wind.Direction.English");
    }

    @Override
    public double getForecastedWindSpeed(String element) {
        return JsonPath.read(element, "$.Day.Wind.Speed.Value");
    }

    @Override
    public double getForecastedUvIndex(String element) {
        int uvIndex = JsonPath.read(element, "$.AirAndPollen[5].Value");
        return Double.parseDouble(Integer.toString(uvIndex));
    }

    @Override
    public String getForecastedDescription(String element) {
        return JsonPath.read(element, "$.Day.IconPhrase");
    }

    @Override
    public String getForecastedImageUrl(String element) {
        String imgId = JsonPath.read(element, "$.Day.IconPhrase");
        return String.format(ConstHelper.ACCU_WEATHER_URL + ConstHelper.ACCU_WEATHER_API_IMAGES_LOCATION, imgId);
    }

    @Override
    public double getForecastedMinTemp(Element element) {
        return 0;
    }

    @Override
    public double getForecastedMaxTemp(Element element) {
        return 0;
    }

    @Override
    public double getForecastedWindSpeed(Element element) {
        return 0;
    }

    @Override
    public String getForecastedWindDirection(Element element) {
        return null;
    }

    @Override
    public double getForecastedUvIndex(Element element) {
        return 0;
    }

    @Override
    public String getForecastedDescription(Element element) {
        return null;
    }

    @Override
    public String getForecastedDate(Element element) {
        return null;
    }

    @Override
    public String getForecastedImageUrl(Element element) {
        return null;
    }

    @Override
    public String getForecastedDay(Element element) {
        return null;
    }

    @Override
    public String getForecastedDate(String element) {
        String dateStr = JsonPath.read(element, "$.Date");
        return dateStr.split("T")[0];
    }

    @Override
    public String toString() {
        return "AccuWeatherSource{" +
                "weatherDataMap=" + weatherDataMap +
                '}';
    }
}
