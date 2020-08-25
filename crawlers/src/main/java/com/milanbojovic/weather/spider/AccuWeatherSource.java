package com.milanbojovic.weather.spider;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccuWeatherSource extends WeatherSource {
    static final String API_KEY_ENV_VAR = "ACCU_WEATHER_API_KEY";
    static final String URI = "http://dataservice.accuweather.com";

    static final String LOCATION_ID = "298198";

    static final String QUERY_PARAM_API_KEY = "apikey";
    static final String QUERY_PARAM_DETAILS_NEEDED = "details";
    static final String QUERY_PARAM_METRIC = "metric";
    static final String QUERY_PARAM_LANGUAGE = "language";

    private HttpClient httpClient;
    private Map<String, String> documents;

    public AccuWeatherSource() {
        httpClient = HttpClient.newHttpClient();

        try {
            documents = buildUris().stream()
                    .map(uriToRequest())
                    .map(this::queryAccuWeatherApi)
                    .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
            weatherData = initializeWeatherData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<URI> buildUris() throws MalformedURLException, URISyntaxException {
        return Arrays.asList(buildUri(URI, "currentconditions", "v1", LOCATION_ID),
                buildUri(URI, "forecasts", "v1", "daily", "1day", LOCATION_ID));
    }

    private Function<URI, HttpRequest> uriToRequest() {
        return uri -> HttpRequest.newBuilder()
                .uri(uri)
                .build();
    }

    private URI buildUri(String uri, String ... path) throws MalformedURLException, URISyntaxException {
        Optional<String> apiKey = Optional.ofNullable(System.getenv(API_KEY_ENV_VAR));
        return new URIBuilder(uri)
                .setPathSegments(path)
                .addParameter(QUERY_PARAM_API_KEY, apiKey.orElseThrow(missingEnvVarException()))
                .addParameter(QUERY_PARAM_LANGUAGE, "sr-rs")
                .addParameter(QUERY_PARAM_DETAILS_NEEDED, "true")
                .addParameter(QUERY_PARAM_METRIC, "true")
                .build();
    }

    private ImmutablePair<String, String> queryAccuWeatherApi(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ImmutablePair<>(request.uri().getPath(),response.body());
    }
    private Supplier<MissingFormatArgumentException> missingEnvVarException() {
        return () -> new MissingFormatArgumentException("Missing mandatory environment variable " +
                "which should be passed as parameter [" + API_KEY_ENV_VAR + "]!");
    }

    private String getCurrentWeather() {
        return documents.get("/currentconditions/v1/298198");
    }

    private String get7DayWeather() {
        return documents.get("/forecasts/v1/daily/1day/298198");
    }
    
    @Override
    public int getMinTemp() {
        Double minTemp = JsonPath.read(get7DayWeather(), "$.DailyForecasts.[0].Temperature.Minimum.Value");
        return (int)Math.round(minTemp);
    }
    

    @Override
    public int getMaxTemp() {
        Double minTemp = JsonPath.read(get7DayWeather(), "$.DailyForecasts.[0].Temperature.Maximum.Value");
        return (int)Math.round(minTemp);
    }

    @Override
    public int getRealFeel() {
        Double realFeel = JsonPath.read(getCurrentWeather(), "$[0].RealFeelTemperature.Metric.Value");
        return (int)Math.round(realFeel);
    }

    @Override
    public int getHumidity() {
        return JsonPath.read(getCurrentWeather(), "$[0].RelativeHumidity");
    }

    @Override
    public int getPressure() {
        Double pressure = JsonPath.read(getCurrentWeather(), "$[0].Pressure.Metric.Value");
        return (int)Math.round(pressure);
    }

    @Override
    public double getUvIndex() {
        int uvIndex = JsonPath.read(getCurrentWeather(), "$[0].UVIndex");
        return new Double(uvIndex);
    }

    @Override
    public double getWindSpeed() {
        return JsonPath.read(getCurrentWeather(), "$[0].Wind.Speed.Metric.Value");
    }

    @Override
    public String getWindDirection() {
        return JsonPath.read(getCurrentWeather(), "$[0].Wind.Direction.English");
    }

    @Override
    public String getDescription() {
        return JsonPath.read(get7DayWeather(), "$.Headline.Text");
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public Date getDate() {
        String dateStr = JsonPath.read(getCurrentWeather(), "$[0].LocalObservationDateTime");
        String[] dateArr = dateStr.split("T")[0].split("-");
        return new Date(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
    }
}
