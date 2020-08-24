package com.milanbojovic.weather.spider;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
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
        return "[\n" +
                "  {\n" +
                "    \"LocalObservationDateTime\": \"2020-08-22T22:53:00+02:00\",\n" +
                "    \"EpochTime\": 1598129580,\n" +
                "    \"WeatherText\": \"Clear\",\n" +
                "    \"WeatherIcon\": 33,\n" +
                "    \"HasPrecipitation\": false,\n" +
                "    \"PrecipitationType\": null,\n" +
                "    \"IsDayTime\": false,\n" +
                "    \"Temperature\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 23.9,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 75,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"RealFeelTemperature\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 23.2,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 74,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"RealFeelTemperatureShade\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 23.2,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 74,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"RelativeHumidity\": 64,\n" +
                "    \"IndoorRelativeHumidity\": 64,\n" +
                "    \"DewPoint\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 16.8,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 62,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"Wind\": {\n" +
                "      \"Direction\": {\n" +
                "        \"Degrees\": 135,\n" +
                "        \"Localized\": \"SE\",\n" +
                "        \"English\": \"SE\"\n" +
                "      },\n" +
                "      \"Speed\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 7,\n" +
                "          \"Unit\": \"km/h\",\n" +
                "          \"UnitType\": 7\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 4.4,\n" +
                "          \"Unit\": \"mi/h\",\n" +
                "          \"UnitType\": 9\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"WindGust\": {\n" +
                "      \"Speed\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 12.8,\n" +
                "          \"Unit\": \"km/h\",\n" +
                "          \"UnitType\": 7\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 8,\n" +
                "          \"Unit\": \"mi/h\",\n" +
                "          \"UnitType\": 9\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"UVIndex\": 0,\n" +
                "    \"UVIndexText\": \"Low\",\n" +
                "    \"Visibility\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 16.1,\n" +
                "        \"Unit\": \"km\",\n" +
                "        \"UnitType\": 6\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 10,\n" +
                "        \"Unit\": \"mi\",\n" +
                "        \"UnitType\": 2\n" +
                "      }\n" +
                "    },\n" +
                "    \"ObstructionsToVisibility\": \"\",\n" +
                "    \"CloudCover\": 10,\n" +
                "    \"Ceiling\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 8534,\n" +
                "        \"Unit\": \"m\",\n" +
                "        \"UnitType\": 5\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 28000,\n" +
                "        \"Unit\": \"ft\",\n" +
                "        \"UnitType\": 0\n" +
                "      }\n" +
                "    },\n" +
                "    \"Pressure\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 1013,\n" +
                "        \"Unit\": \"mb\",\n" +
                "        \"UnitType\": 14\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 29.91,\n" +
                "        \"Unit\": \"inHg\",\n" +
                "        \"UnitType\": 12\n" +
                "      }\n" +
                "    },\n" +
                "    \"PressureTendency\": {\n" +
                "      \"LocalizedText\": \"Steady\",\n" +
                "      \"Code\": \"S\"\n" +
                "    },\n" +
                "    \"Past24HourTemperatureDeparture\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 1.1,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 2,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"ApparentTemperature\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 25,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 77,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"WindChillTemperature\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 23.9,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 75,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"WetBulbTemperature\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 19.4,\n" +
                "        \"Unit\": \"C\",\n" +
                "        \"UnitType\": 17\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 67,\n" +
                "        \"Unit\": \"F\",\n" +
                "        \"UnitType\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    \"Precip1hr\": {\n" +
                "      \"Metric\": {\n" +
                "        \"Value\": 0,\n" +
                "        \"Unit\": \"mm\",\n" +
                "        \"UnitType\": 3\n" +
                "      },\n" +
                "      \"Imperial\": {\n" +
                "        \"Value\": 0,\n" +
                "        \"Unit\": \"in\",\n" +
                "        \"UnitType\": 1\n" +
                "      }\n" +
                "    },\n" +
                "    \"PrecipitationSummary\": {\n" +
                "      \"Precipitation\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"PastHour\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past3Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past6Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past9Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past12Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past18Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past24Hours\": {\n" +
                "        \"Metric\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Imperial\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"in\",\n" +
                "          \"UnitType\": 1\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"TemperatureSummary\": {\n" +
                "      \"Past6HourRange\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 23.9,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 75,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 31.8,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 89,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past12HourRange\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 23.9,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 75,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 32.2,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 90,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"Past24HourRange\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 17.8,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 64,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Metric\": {\n" +
                "            \"Value\": 32.2,\n" +
                "            \"Unit\": \"C\",\n" +
                "            \"UnitType\": 17\n" +
                "          },\n" +
                "          \"Imperial\": {\n" +
                "            \"Value\": 90,\n" +
                "            \"Unit\": \"F\",\n" +
                "            \"UnitType\": 18\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"MobileLink\": \"http://m.accuweather.com/en/rs/belgrade/298198/current-weather/298198?lang=en-us\",\n" +
                "    \"Link\": \"http://www.accuweather.com/en/rs/belgrade/298198/current-weather/298198?lang=en-us\"\n" +
                "  }\n" +
                "]";
//        return documents.get("/currentconditions/v1/298198");
    }

    private String get7DayWeather() {
        return "{\n" +
                "  \"Headline\": {\n" +
                "    \"EffectiveDate\": \"2020-08-24T08:00:00+02:00\",\n" +
                "    \"EffectiveEpochDate\": 1598248800,\n" +
                "    \"Severity\": 1,\n" +
                "    \"Text\": \"Oluje sa grmljavinom, povremeno jake, ponedeljak\",\n" +
                "    \"Category\": \"thunderstorm\",\n" +
                "    \"EndDate\": \"2020-08-24T20:00:00+02:00\",\n" +
                "    \"EndEpochDate\": 1598292000,\n" +
                "    \"MobileLink\": \"http://m.accuweather.com/sr/rs/belgrade/298198/extended-weather-forecast/298198?unit=c\",\n" +
                "    \"Link\": \"http://www.accuweather.com/sr/rs/belgrade/298198/daily-weather-forecast/298198?unit=c\"\n" +
                "  },\n" +
                "  \"DailyForecasts\": [\n" +
                "    {\n" +
                "      \"Date\": \"2020-08-23T07:00:00+02:00\",\n" +
                "      \"EpochDate\": 1598158800,\n" +
                "      \"Sun\": {\n" +
                "        \"Rise\": \"2020-08-23T05:50:00+02:00\",\n" +
                "        \"EpochRise\": 1598154600,\n" +
                "        \"Set\": \"2020-08-23T19:30:00+02:00\",\n" +
                "        \"EpochSet\": 1598203800\n" +
                "      },\n" +
                "      \"Moon\": {\n" +
                "        \"Rise\": \"2020-08-23T10:54:00+02:00\",\n" +
                "        \"EpochRise\": 1598172840,\n" +
                "        \"Set\": \"2020-08-23T22:07:00+02:00\",\n" +
                "        \"EpochSet\": 1598213220,\n" +
                "        \"Phase\": \"WaxingCrescent\",\n" +
                "        \"Age\": 4\n" +
                "      },\n" +
                "      \"Temperature\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Value\": 18.9,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Value\": 32.2,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        }\n" +
                "      },\n" +
                "      \"RealFeelTemperature\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Value\": 18.3,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Value\": 35,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        }\n" +
                "      },\n" +
                "      \"RealFeelTemperatureShade\": {\n" +
                "        \"Minimum\": {\n" +
                "          \"Value\": 18.3,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        },\n" +
                "        \"Maximum\": {\n" +
                "          \"Value\": 31.7,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        }\n" +
                "      },\n" +
                "      \"HoursOfSun\": 5.1,\n" +
                "      \"DegreeDaySummary\": {\n" +
                "        \"Heating\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        },\n" +
                "        \"Cooling\": {\n" +
                "          \"Value\": 8,\n" +
                "          \"Unit\": \"C\",\n" +
                "          \"UnitType\": 17\n" +
                "        }\n" +
                "      },\n" +
                "      \"AirAndPollen\": [\n" +
                "        {\n" +
                "          \"Name\": \"AirQuality\",\n" +
                "          \"Value\": 54,\n" +
                "          \"Category\": \"Umereno\",\n" +
                "          \"CategoryValue\": 2,\n" +
                "          \"Type\": \"Sumpor-dioksid\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"Grass\",\n" +
                "          \"Value\": 0,\n" +
                "          \"Category\": \"Nisko\",\n" +
                "          \"CategoryValue\": 1\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"Mold\",\n" +
                "          \"Value\": 0,\n" +
                "          \"Category\": \"Nisko\",\n" +
                "          \"CategoryValue\": 1\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"Tree\",\n" +
                "          \"Value\": 0,\n" +
                "          \"Category\": \"Nisko\",\n" +
                "          \"CategoryValue\": 1\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"Ragweed\",\n" +
                "          \"Value\": 0,\n" +
                "          \"Category\": \"Nisko\",\n" +
                "          \"CategoryValue\": 1\n" +
                "        },\n" +
                "        {\n" +
                "          \"Name\": \"UVIndex\",\n" +
                "          \"Value\": 7,\n" +
                "          \"Category\": \"Visoko\",\n" +
                "          \"CategoryValue\": 3\n" +
                "        }\n" +
                "      ],\n" +
                "      \"Day\": {\n" +
                "        \"Icon\": 17,\n" +
                "        \"IconPhrase\": \"Mestimično sunčano sa olujama s grmljavinom\",\n" +
                "        \"HasPrecipitation\": true,\n" +
                "        \"PrecipitationType\": \"Rain\",\n" +
                "        \"PrecipitationIntensity\": \"Light\",\n" +
                "        \"ShortPhrase\": \"Nekoliko pljuskova i oluja sa grmljavinom\",\n" +
                "        \"LongPhrase\": \"Nekoliko pljuskova i oluja sa grmljavinom\",\n" +
                "        \"PrecipitationProbability\": 60,\n" +
                "        \"ThunderstormProbability\": 40,\n" +
                "        \"RainProbability\": 60,\n" +
                "        \"SnowProbability\": 0,\n" +
                "        \"IceProbability\": 0,\n" +
                "        \"Wind\": {\n" +
                "          \"Speed\": {\n" +
                "            \"Value\": 8,\n" +
                "            \"Unit\": \"km/h\",\n" +
                "            \"UnitType\": 7\n" +
                "          },\n" +
                "          \"Direction\": {\n" +
                "            \"Degrees\": 312,\n" +
                "            \"Localized\": \"SZ\",\n" +
                "            \"English\": \"NW\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"WindGust\": {\n" +
                "          \"Speed\": {\n" +
                "            \"Value\": 16.1,\n" +
                "            \"Unit\": \"km/h\",\n" +
                "            \"UnitType\": 7\n" +
                "          },\n" +
                "          \"Direction\": {\n" +
                "            \"Degrees\": 312,\n" +
                "            \"Localized\": \"SZ\",\n" +
                "            \"English\": \"NW\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"TotalLiquid\": {\n" +
                "          \"Value\": 3.8,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Rain\": {\n" +
                "          \"Value\": 3.8,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Snow\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"cm\",\n" +
                "          \"UnitType\": 4\n" +
                "        },\n" +
                "        \"Ice\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"HoursOfPrecipitation\": 1.5,\n" +
                "        \"HoursOfRain\": 1.5,\n" +
                "        \"HoursOfSnow\": 0,\n" +
                "        \"HoursOfIce\": 0,\n" +
                "        \"CloudCover\": 37\n" +
                "      },\n" +
                "      \"Night\": {\n" +
                "        \"Icon\": 41,\n" +
                "        \"IconPhrase\": \"Mestimično oblačno sa olujama sa grmljavinom\",\n" +
                "        \"HasPrecipitation\": true,\n" +
                "        \"PrecipitationType\": \"Rain\",\n" +
                "        \"PrecipitationIntensity\": \"Moderate\",\n" +
                "        \"ShortPhrase\": \"Nekoliko pljuskova i oluja sa grmljavinom\",\n" +
                "        \"LongPhrase\": \"Nekoliko pljuskova i oluja sa grmljavinom\",\n" +
                "        \"PrecipitationProbability\": 60,\n" +
                "        \"ThunderstormProbability\": 40,\n" +
                "        \"RainProbability\": 60,\n" +
                "        \"SnowProbability\": 0,\n" +
                "        \"IceProbability\": 0,\n" +
                "        \"Wind\": {\n" +
                "          \"Speed\": {\n" +
                "            \"Value\": 8,\n" +
                "            \"Unit\": \"km/h\",\n" +
                "            \"UnitType\": 7\n" +
                "          },\n" +
                "          \"Direction\": {\n" +
                "            \"Degrees\": 315,\n" +
                "            \"Localized\": \"SZ\",\n" +
                "            \"English\": \"NW\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"WindGust\": {\n" +
                "          \"Speed\": {\n" +
                "            \"Value\": 12.9,\n" +
                "            \"Unit\": \"km/h\",\n" +
                "            \"UnitType\": 7\n" +
                "          },\n" +
                "          \"Direction\": {\n" +
                "            \"Degrees\": 315,\n" +
                "            \"Localized\": \"SZ\",\n" +
                "            \"English\": \"NW\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"TotalLiquid\": {\n" +
                "          \"Value\": 2.5,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Rain\": {\n" +
                "          \"Value\": 2.5,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"Snow\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"cm\",\n" +
                "          \"UnitType\": 4\n" +
                "        },\n" +
                "        \"Ice\": {\n" +
                "          \"Value\": 0,\n" +
                "          \"Unit\": \"mm\",\n" +
                "          \"UnitType\": 3\n" +
                "        },\n" +
                "        \"HoursOfPrecipitation\": 1,\n" +
                "        \"HoursOfRain\": 1,\n" +
                "        \"HoursOfSnow\": 0,\n" +
                "        \"HoursOfIce\": 0,\n" +
                "        \"CloudCover\": 53\n" +
                "      },\n" +
                "      \"Sources\": [\n" +
                "        \"AccuWeather\"\n" +
                "      ],\n" +
                "      \"MobileLink\": \"http://m.accuweather.com/sr/rs/belgrade/298198/daily-weather-forecast/298198?day=1&unit=c\",\n" +
                "      \"Link\": \"http://www.accuweather.com/sr/rs/belgrade/298198/daily-weather-forecast/298198?day=1&unit=c\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
//        return documents.get("/forecasts/v1/daily/1day/298198");
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
        return (int)JsonPath.read(getCurrentWeather(), "$[0].Pressure.Metric.Value");
    }

    @Override
    public double getUvIndex() {
        return JsonPath.read(getCurrentWeather(), "$[0].UVIndex");
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
