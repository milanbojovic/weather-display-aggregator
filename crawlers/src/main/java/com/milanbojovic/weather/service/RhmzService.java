package com.milanbojovic.weather.service;

import com.milanbojovic.weather.client.HtmlClient;
import com.milanbojovic.weather.data.extraction.CurrentWeatherExtractorRhm;
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

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;

public class RhmzService implements WeatherProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RhmzService.class);
    public final String weatherProvider;
    public static final String CONTENT_MISSING_IMAGE_URL = "https://icon-library.net/images/error-icon-png/error-icon-png-4.jpg";
    private final Map<String, Document> documents;

    public RhmzService(List<String> cities) {
        weatherProvider = "RHMZ";
        LOGGER.info("Creating Rhmz Source");
        HtmlClient connectionClient = new HtmlClient();

        documents = createUriList(cities).stream()
                .map(connectionClient::provideStringDocumentPair)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private List<String> createUriList(List<String> cities) {
        List<String> dailyForecastUris = cities.stream()
                .map(String::toLowerCase)
                .map(Util.rhmzLocationIdMap::get)
                .map(x -> format(ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST, x))
                .collect(Collectors.toList());
        dailyForecastUris.addAll(currentWeatherUris());
        return dailyForecastUris;
    }

    private List<String> currentWeatherUris() {
        return Arrays.asList(
                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER,
                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX
        );
    }

    @Override
    public CurrentWeather provideCurrentWeather(String city) {
        LOGGER.debug(String.format("Initializing current weather data for source=[%s], City=[%s]", weatherProvider, city));
        Element currentWeatherFor = getCurrentWeatherFor(city);
        String dateLine = getCurrentDateFor(city);
        CurrentWeatherExtractorRhm currentWeatherExtractorRhm = new CurrentWeatherExtractorRhm(currentWeatherFor, dateLine);
        return currentWeatherExtractorRhm.extract();
    }

    @Override
    public List<DailyForecast> provideWeeklyForecast(String city) {
        return initializeDailyForecast(city);
    }

    private Element getCurrentWeatherFor(String city) {
        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));
        Document currentWeather = documents.get(ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER);
        Elements citiesTable = getCurrentWeatherForAllCities(currentWeather);
        return findCity(citiesTable, city);
    }

    private String getCurrentDateFor(String city) {
        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));
        return documents.get(ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER)
                .getElementById("sadrzaj")
                .getElementsByTag("h1").get(0)
                .text();
    }


    private Element getFiveDayWeatherFor(String city) {
        LOGGER.debug(format("Fetching weekly weather data for %s", city));
        Document weeklyForecast = documents.get(format(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST, city));
        Elements citiesTable = getDailyForecastForCityTable(weeklyForecast);
        return findCity(citiesTable, city);
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
            dailyForecast.setProvider(weatherProvider);
            dailyForecast.setImageUrl(CONTENT_MISSING_IMAGE_URL);
            dailyForecast.setDate("N/A");
        });

        LOGGER.debug(format("Initializing weather data for %s.", weatherProvider));
        Elements weeklyForecast = getWeeklyForecast(city);

        for(int i = 0; i < weeklyForecast.size(); i++) {
            Element rowElement = weeklyForecast.get(i);
            Elements columnElements = rowElement.children();
            for(int j = 2; j < rowElement.children().size() - 2; j++) {
                Element currentElement = columnElements.get(j);
                DailyForecast dailyForecast = resultList.get(j - 1);
                if (i == 0) dailyForecast.setMaxTemp(getDoubleVal(currentElement));
                if (i == 1) dailyForecast.setMinTemp(getDoubleVal(currentElement));
                if (i == 2) {
                    String imgUrl = currentElement.child(0).attr("src");
                    dailyForecast.setImageUrl(ConstHelper.RHMZ_URL + "/repository" + imgUrl.split("repository")[1]);
                }
                dailyForecast.setDescription("N/A");
                dailyForecast.setWindDirection("N/A");
            }
        }

        //Set day/date
        Document weeklyForecastDoc = documents.get(
                ConstHelper.RHMZ_URL +
                format(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST,
                        Util.rhmzLocationIdMap.get(city)));

        Elements headColumns = weeklyForecastDoc.getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_FIVE_DAY_FORECAST_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_THEAD)
                .get(0)
                .children()
                .get(0)
                .children();

        for(int i = 2; i < headColumns.size() - 2; i++) {
            DailyForecast dailyForecast = resultList.get(i-1);
            String rawDay = headColumns.get(i).text().split(" ")[0];

            int day = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[0]);
            int month = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[1]);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            dailyForecast.setDate(StringUtils.capitalize(rawDay.toLowerCase()) + " - " + Util.formatDate(year, month, day));
        }
        return resultList;
    }

    private double getDoubleVal(Element element) {
        double value = 0;
        try{
            String getElementTextValue = element.text();
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
        Document weeklyForecast = documents.get(
                ConstHelper.RHMZ_URL +
                format(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST,
                        Util.rhmzLocationIdMap.get(city)));
        return getDailyForecastForCityTable(weeklyForecast);
    }

    private Element getUvIndexWeatherFor(String city) {
        LOGGER.debug(format("Fetching UV index data for %s", city));
        Document uvIndexDoc = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX);
        Elements uvIndexTable = getUvIndexForAllCities(uvIndexDoc);
        return findCity(uvIndexTable, city);
    }

    private Element findCity(Elements citiesTable, String city) {
        final String cityTranslation = CyrillicLatinConverter.latinToCyrillic(city);
        List<Element> collect = citiesTable.stream()
                .filter(element -> getColumnValue(element, CurrentWeatherColumnsEnum.CITY).equalsIgnoreCase(cityTranslation))
                .collect(Collectors.toList());
        return collect.isEmpty()? null : collect.get(0);
    }
//
    private Elements getCurrentWeatherForAllCities(Document currentWeather) {
        return currentWeather
                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_CURRENT_WEATHER_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children();
    }

    private String getColumnValue(Element city, CurrentWeatherColumnsEnum column) {
        return city.child(column.ordinal()).text();
    }

    private Elements getUvIndexForAllCities(Document currentWeather) {
        return currentWeather
                .getElementById("slika_pracenje")
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children();
    }

    public String getForecastedDate(String city) {
        Element citiesFiveDayTable = getFiveDayWeatherFor(city);
        String h1 = citiesFiveDayTable.getElementsByTag("h1").text();
        String date = h1.split(" ")[h1.split(" ").length - 1].trim();
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        int year = Integer.parseInt(date.substring(6,10));
        return Util.formatDate(year, month, day);
    }
}
