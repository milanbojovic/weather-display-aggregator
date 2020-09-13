package com.milanbojovic.weather.service.weather;

import com.milanbojovic.weather.service.persistance.MongoDao;
import com.milanbojovic.weather.service.weather.client.HtmlClient;
import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.extraction.html.CurrentWeatherParserW2U;
import com.milanbojovic.weather.data.model.extraction.html.DailyForecastParserW2U;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Weather2UmbrellaService implements WeatherProvider {
    private final AppConfig appConfig;
    private final MongoDao mongoDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(Weather2UmbrellaService.class);
    private final String providerName;
    private final Map<String, Document> documents;

    @Autowired
    public Weather2UmbrellaService(AppConfig appConfig, MongoDao mongoDao) {
        this.appConfig = appConfig;
        this.mongoDao = mongoDao;
        providerName = "W2UM";
        LOGGER.info("Creating Weather2Umbrella Source");
        HtmlClient connectionClient = new HtmlClient();
        List<String> citiesList = appConfig.getCities();

        documents = citiesList.stream()
                .map(String::toLowerCase)
                .map(city -> city.replace(" ", "-"))
                .map(this::buildWeather2UmbrellaUris)
                .flatMap(Collection::stream)
                .map(connectionClient::provideStringDocumentPair)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        persistWeatherDataToDb(mongoDao, citiesList, providerName);
    }

    private List<String> buildWeather2UmbrellaUris(String city) {
        return Arrays.asList(
                appConfig.getW2uUrl() + String.format(ConstHelper.W2U_CITY, city) + appConfig.getW2uWeeklyForecast(),
                appConfig.getW2uUrl() + String.format(ConstHelper.W2U_CITY, city) + appConfig.getW2uCurrentWeather()
        );
    }

    @Override
    public CurrentWeather provideCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", providerName, city));
        String queryCityUrl = queryCityUrl(city, appConfig.getW2uCurrentWeather());
        Document cityDoc = documents.get(queryCityUrl);
        CurrentWeatherParserW2U currentWeatherExtractorW2u = new CurrentWeatherParserW2U(cityDoc);
        return currentWeatherExtractorW2u.extract();
    }

    @Override
    public List<DailyForecast> provideWeeklyForecast(String city) {
        LOGGER.debug(String.format("Initializing weather forecast for %s.", providerName));
        String queryCityUrl = queryCityUrl(city, appConfig.getW2uWeeklyForecast());
        Document weeklyForecastDocument = documents.get(queryCityUrl);
        return weeklyForecastDocument.getElementsByClass("day_wrap")
                .stream()
                .limit(5)
                .map(this::maptoDailyForecast)
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    private DailyForecast maptoDailyForecast(Element dailyForecast) {
        DailyForecastParserW2U dailyForecastExtractorW2u = new DailyForecastParserW2U(dailyForecast);
        DailyForecast extractedDailyForecast = dailyForecastExtractorW2u.extract();
        extractedDailyForecast.setProvider(providerName);
        return dailyForecastExtractorW2u.extract();
    }

    private String queryCityUrl(String city, String w2uCurrentWeather) {
        return appConfig.getW2uUrl() + String.format(ConstHelper.W2U_CITY, city.replace(" ", "-")) + w2uCurrentWeather;
    }
}
