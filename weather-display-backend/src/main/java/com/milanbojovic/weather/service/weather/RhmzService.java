package com.milanbojovic.weather.service.weather;

import com.milanbojovic.weather.service.persistance.MongoDao;
import com.milanbojovic.weather.service.weather.client.HtmlClient;
import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.extraction.html.CurrentWeatherParserRhm;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.CurrentWeatherColumnsEnum;
import com.milanbojovic.weather.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cyrlat.CyrillicLatinConverter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;

@Service
public class RhmzService implements WeatherProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RhmzService.class);
    public static final String MSG_FORMAT = "{0}{1}";
    private final AppConfig appConfig;
    public final String prividerName;
    private final Map<String, Document> documents;

    @Autowired
    public RhmzService(AppConfig appConfig, MongoDao mongoDao) {
        LOGGER.info("Creating Rhmz Source");
        this.appConfig = appConfig;
        prividerName = "RHMZ";
        var connectionClient = new HtmlClient();
        var citiesList = appConfig.getCities();

        documents = createUriList(citiesList).stream()
                .map(connectionClient::provideStringDocumentPair)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        persistWeatherDataToDb(mongoDao, citiesList, prividerName);
    }

    private List<String> createUriList(List<String> cities) {
        List<String> dailyForecastUris = cities.stream()
                .map(String::toLowerCase)
                .map(Util.rhmzLocationIdMap::get)
                .map(x -> format(MessageFormat.format("{0}{1}{2}",
                        appConfig.getRhmzUrl(),
                        ConstHelper.RHMZ_URI_PATH,
                        appConfig.getRhmzWeeklyForecast()), x))
                .collect(Collectors.toList());
        dailyForecastUris.addAll(currentWeatherUris());
        return dailyForecastUris;
    }

    private List<String> currentWeatherUris() {
        return Arrays.asList(
                appConfig.getRhmzUrl() + ConstHelper.RHMZ_URI_PATH + appConfig.getRhmzCurrentWeather(),
                appConfig.getRhmzUrl() + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX
        );
    }

    @Override
    public CurrentWeather provideCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", prividerName, city));
        var currentWeatherFor = getCurrentWeatherFor(city);
        var dateLine = getCurrentDateFor(city);
        var currentWeatherExtractorRhm = new CurrentWeatherParserRhm(currentWeatherFor, dateLine, appConfig);
        return currentWeatherExtractorRhm.extract();
    }

    @Override
    public List<DailyForecast> provideWeeklyForecast(String city) {
        return initializeDailyForecast(city);
    }

    @Override
    public String getProviderName() {
        return prividerName;
    }

    private Element getCurrentWeatherFor(String city) {
        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));

        var currentWeather = documents.get(appConfig.getRhmzUrl() + ConstHelper.RHMZ_URI_PATH + appConfig.getRhmzCurrentWeather());
        var citiesTable = getCurrentWeatherForAllCities(currentWeather);
        return findCity(citiesTable, city);
    }

    private String getCurrentDateFor(String city) {
        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));
        return documents.get(appConfig.getRhmzUrl() + ConstHelper.RHMZ_URI_PATH + appConfig.getRhmzCurrentWeather())
                .getElementById("sadrzaj")
                .getElementsByTag("h1").get(0)
                .text();
    }

    protected List<DailyForecast> initializeDailyForecast(String city) {
        city = city.toLowerCase();
        List<DailyForecast> resultList = new ArrayList<>();
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());

        resultList.forEach(dailyForecast -> {
            dailyForecast.setWindDirection("N/A");
            dailyForecast.setDescription("N/A");
            dailyForecast.setProvider(prividerName);
            dailyForecast.setImageUrl(appConfig.getRhmzContentNotFoundImage());
            dailyForecast.setDate("N/A");
        });

        LOGGER.debug(format("Initializing weather data for %s.", prividerName));
        var weeklyForecast = getWeeklyForecast(city);

        for(var i = 0; i < weeklyForecast.size(); i++) {
            var rowElement = weeklyForecast.get(i);
            var columnElements = rowElement.children();
            for(var j = 2; j < rowElement.children().size() - 2; j++) {
                var currentElement = columnElements.get(j);
                var dailyForecast = resultList.get(j - 1);
                if (i == 0) dailyForecast.setMaxTemp(getDoubleVal(currentElement));
                if (i == 1) dailyForecast.setMinTemp(getDoubleVal(currentElement));
                if (i == 2) {
                    var imgUrl = currentElement.child(0).attr("src");
                    dailyForecast.setImageUrl(appConfig.getRhmzUrl() + "/repository" + imgUrl.split("repository")[1]);
                }
                dailyForecast.setDescription("N/A");
                dailyForecast.setWindDirection("N/A");
            }
        }

        //Set day/date
        var weeklyForecastDoc = documents.get(
                appConfig.getRhmzUrl() +
                format(MessageFormat.format(MSG_FORMAT,
                        ConstHelper.RHMZ_URI_PATH,
                        appConfig.getRhmzWeeklyForecast()),
                        Util.rhmzLocationIdMap.get(city)));

        var headColumns = weeklyForecastDoc.getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_FIVE_DAY_FORECAST_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_THEAD)
                .get(0)
                .children()
                .get(0)
                .children();

        for(var i = 2; i < headColumns.size() - 2; i++) {
            var dailyForecast = resultList.get(i-1);
            var rawDay = headColumns.get(i).text().split(" ")[0];

            var day = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[0]);
            var month = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[1]);
            var year = Calendar.getInstance().get(Calendar.YEAR);
            dailyForecast.setDate(StringUtils.capitalize(rawDay.toLowerCase()) + " - " + Util.formatDate(year, month, day));
        }
        return resultList;
    }

    private double getDoubleVal(Element element) {
        double value = 0;
        try{
            var getElementTextValue = element.text();
            value = parseDouble(getElementTextValue);
        } catch (NumberFormatException ex) {
            LOGGER.error(MessageFormat.format("Error while parsing double value for element {0}", element));

        }
        return value;
    }

    private Elements getDailyForecastForCityTable(Document weeklyForecast) {
        return weeklyForecast.getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_FIVE_DAY_FORECAST_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children().stream()
                .filter(element -> element.childrenSize() > 1)
                .limit(3)
                .collect(Collectors.toCollection(Elements::new));
    }


    protected Elements getWeeklyForecast(String city) {
        LOGGER.debug(format("Fetching weekly weather data for %s", city));
        var weeklyForecast = documents.get(
                appConfig.getRhmzUrl() +
                format(MessageFormat.format(MSG_FORMAT,
                        ConstHelper.RHMZ_URI_PATH,
                        appConfig.getRhmzWeeklyForecast()),
                        Util.rhmzLocationIdMap.get(city)));
        return getDailyForecastForCityTable(weeklyForecast);
    }

    private Element findCity(Elements citiesTable, String city) {
        final var cityTranslation = CyrillicLatinConverter.latinToCyrillic(city);
        var collect = citiesTable.stream()
                .filter(element -> getColumnValue(element).equalsIgnoreCase(cityTranslation))
                .collect(Collectors.toList());
        return collect.isEmpty()? null : collect.get(0);
    }

    private Elements getCurrentWeatherForAllCities(Document currentWeather) {
        return currentWeather
                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_CURRENT_WEATHER_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children();
    }

    private String getColumnValue(Element city) {
        return city.child(CurrentWeatherColumnsEnum.CITY.ordinal()).text();
    }
}
