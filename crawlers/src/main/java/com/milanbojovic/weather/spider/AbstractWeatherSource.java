package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.data.CurrentWeather;
import com.milanbojovic.weather.data.DailyForecast;
import com.milanbojovic.weather.data.WeatherData;
import com.milanbojovic.weather.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cyrlat.CyrillicLatinConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class AbstractWeatherSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWeatherSource.class);
    protected Map<String, WeatherData> weatherDataMap;
    protected final String weatherProvider;

    public AbstractWeatherSource(String provider) {
        weatherProvider = provider;
        weatherDataMap = new HashMap<>();
    }

    public String getWeatherProvider() {
        return weatherProvider;
    }

    public Map<String, WeatherData> getWeatherDataMap() {
        return weatherDataMap;
    }

    protected void persistAllWeatherDataToMap(List<String> cities) {
        cities.stream()
                .map(this::assembleWeatherDataForCity)
                .forEach(weatherData -> weatherDataMap.put(weatherData.getCity(), weatherData));
    }

    protected WeatherData assembleWeatherDataForCity(String city) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(StringUtils.capitalize(city));
        weatherData.setCurrentWeather(initializeCurrentWeather(city));
        weatherData.setWeeklyForecast(initializeDailyForecast(city));
        return weatherData;
    }

    protected CurrentWeather initializeCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", weatherProvider, city));
        return buildCurrentWeatherForCity(city);
    }

    protected List<DailyForecast> initializeDailyForecast(String city) {
        city = city.toLowerCase();
        LOGGER.debug(String.format("Initializing weather data for %s.", weatherProvider));
        return getWeeklyForecast(city)
                .stream()
                .map(this::buildDailyForecastFor)
                .collect(Collectors.toList());
    }

    private CurrentWeather buildCurrentWeatherForCity(String city) {
        city = city.toLowerCase();
        return CurrentWeather.builder()
                .currentTemp(getCurrentTemp(city))
                .realFeel(getCurrentRealFeel(city))
                .humidity(getCurrentHumidity(city))
                .pressure(getCurrentPressure(city))
                .windSpeed(getCurrentWindSpeed(city))
                .windDirection(getCurrentWindDirection(city))
                .uvIndex(getCurrentUvIndex(city))
                .imageUrl(getCurrentImageUrl(city))
                .description(getCurrentDescription(city))
                .date(getCurrentDate(city))
                .day(getCurrentDay(city))
                .build();
    }

    protected DailyForecast buildDailyForecastFor(Element element) {
        return DailyForecast.builder()
                .provider(weatherProvider)
                .minTemp(getForecastedMinTemp(element))
                .maxTemp(getForecastedMaxTemp(element))
                .windSpeed(getForecastedWindSpeed(element))
                .windDirection(getForecastedWindDirection(element))
                .uvIndex(getForecastedUvIndex(element))
                .description(getForecastedDescription(element))
                .date(getForecastedDate(element))
                .day(getForecastedDay(element))
                .imageUrl(getForecastedImageUrl(element))
                .build();
    }

    protected ImmutablePair<String, Document> requestUriToResponseDocTuple(String url, UnaryOperator<String> callback) {
        Document document = null;
        try {
            LOGGER.debug(String.format("Creating Jsoup connection to: %s", url));
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOGGER.error("Error while executing Jsoup connection", e);
        }
        return new ImmutablePair<>(callback.apply(url), document);
    }

    protected String getDayFromDateString(String dateStr) {
        String[] dateSplit = dateStr.split("\\.");
        int day = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int year = Integer.parseInt(dateSplit[2]);

        String stringDate = LocalDate.of(year, month, day)
                .getDayOfWeek()
                .toString()
                .toLowerCase();
        return Util.translateDayToRsCyrilic.get(stringDate);
    }

    protected abstract Elements getWeeklyForecast(String city);

    protected abstract double getCurrentTemp(String city);

    public abstract double getCurrentRealFeel(String city);

    public abstract int getCurrentHumidity(String city);

    public abstract double getCurrentPressure(String city);

    public abstract  double getCurrentWindSpeed(String city);

    public abstract String getCurrentWindDirection(String city);

    public abstract double getCurrentUvIndex(String city);

    public abstract String getCurrentImageUrl(String city);

    public abstract String getCurrentDescription(String city);

    public abstract String getCurrentDate(String city);

    protected String getCurrentDay(String city) {
        return getDayFromDateString(getCurrentDate(city));
    }

    public abstract double getForecastedMinTemp(String json);
    public abstract double getForecastedMaxTemp(String json);
    public abstract  double getForecastedWindSpeed(String json);
    public abstract String getForecastedWindDirection(String json);
    public abstract double getForecastedUvIndex(String json);
    public abstract String getForecastedDescription(String json);
    public abstract String getForecastedDate(String json);
    public abstract String getForecastedImageUrl(String json);

    public abstract double getForecastedMinTemp(Element element);
    public abstract double getForecastedMaxTemp(Element element);
    public abstract  double getForecastedWindSpeed(Element element);
    public abstract String getForecastedWindDirection(Element element);
    public abstract double getForecastedUvIndex(Element element);
    public abstract String getForecastedDescription(Element element);
    public abstract String getForecastedDate(Element element);
    public abstract String getForecastedImageUrl(Element element);
    public abstract String getForecastedDay(Element element);


    protected String getForecastedDay(String city) {
        return getDayFromDateString(getForecastedDate(city));
    }
}
