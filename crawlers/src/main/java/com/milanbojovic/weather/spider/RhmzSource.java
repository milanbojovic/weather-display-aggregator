package com.milanbojovic.weather.spider;

import com.milanbojovic.weather.data.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import com.milanbojovic.weather.util.CurrentWeatherColumnsEnum;
import com.milanbojovic.weather.util.Util;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

public class RhmzSource extends AbstractWeatherSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RhmzSource.class);
    public static final String WEATHER_PROVIDER_NAME = "RHMZ";
    private final Map<String, Document> documents;

    public RhmzSource(List<String> cities) {
        super(WEATHER_PROVIDER_NAME);
        LOGGER.info("Creating Rhmz Source");

        documents = createUriList(cities).stream()
                .map(url -> this.requestUriToResponseDocTuple(url, RhmzSource::getResourcePath))
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        persistAllWeatherDataToMap(cities);
    }

    private List<String> createUriList(List<String> cities) {
        List<String> dailyForecastUris = cities.stream()
                .map(String::toLowerCase)
                .map(Util.rhmzLocationIdMap::get)
                .map(x -> format(ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST, x))
                .collect(Collectors.toList());
        dailyForecastUris.addAll(Arrays.asList(
                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER,
                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX
        ));
        return dailyForecastUris;
    }

    private static String getResourcePath(String uri) {
        return uri.split(ConstHelper.RHMZ_URL)[1];
    }

    private Element getCurrentWeatherFor(String city) {
        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));
        Document currentWeather = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER);
        Elements citiesTable = getCurrentWeatherForAllCities(currentWeather);
        return findCity(citiesTable, city);
    }

    private Element getFiveDayWeatherFor(String city) {
        LOGGER.debug(format("Fetching weekly weather data for %s", city));
        Document weeklyForecast = documents.get(format(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST, city));
        Elements citiesTable = getDailyForecastForCityTable(weeklyForecast);
        return findCity(citiesTable, city);
    }

    @Override
    protected List<DailyForecast> initializeDailyForecast(String city) {
        city = city.toLowerCase();
        List<DailyForecast> resultList = new ArrayList<>();
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());
        resultList.add(DailyForecast.builder().build());

        LOGGER.debug(format("Initializing weather data for %s.", weatherProvider));
        Elements weeklyForecast = getWeeklyForecast(city);

        for(int i = 0; i < weeklyForecast.size(); i++) {
            Element rowElement = weeklyForecast.get(i);
            Elements columnElements = rowElement.children();
            for(int j = 2; j < rowElement.children().size() - 1; j++) {
                Element currentElement = columnElements.get(j);
                DailyForecast dailyForecast = resultList.get(j - 2);
                if (i == 0) dailyForecast.setMaxTemp(getDoubleVal(currentElement));
                if (i == 1) dailyForecast.setMinTemp(getDoubleVal(currentElement));
                if (i == 2) {
                    String imgUrl = currentElement.child(0).attr("src");
                    dailyForecast.setImageUrl(ConstHelper.RHMZ_URL + "/repository/" + imgUrl.split("repository")[1]);
                }
                dailyForecast.setProvider(WEATHER_PROVIDER_NAME);
                dailyForecast.setDescription("N/A");
                dailyForecast.setWindDirection("N/A");
            }
        }

        //Set day/date
        Document weeklyForecastDoc = documents.get(
                format(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_FIVE_DAY_FORECAST,
                        Util.rhmzLocationIdMap.get(city)));

        Elements headColumns = weeklyForecastDoc.getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_FIVE_DAY_FORECAST_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_THEAD)
                .get(0)
                .children()
                .get(0)
                .children();

        for(int i = 2; i < headColumns.size() - 1; i++) {
            DailyForecast dailyForecast = resultList.get(i-2);
            dailyForecast.setDay(headColumns.get(i).text().split(" ")[0]);

            int day = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[0]);
            int month = Integer.parseInt(headColumns.get(i).text().split(" ")[1].split("\\.")[1]);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            dailyForecast.setDate(Util.formatDate(year, month, day));
        }
        return resultList;
    }

    private double getDoubleVal(Element element) {
        double value = 0;
        try{
            String getElementTextValue = element.text();
            value = parseDouble(getElementTextValue);
        } catch (NumberFormatException ex) {
            LOGGER.error("Error while par");

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

    @Override
    protected Elements getWeeklyForecast(String city) {
        LOGGER.debug(format("Fetching weekly weather data for %s", city));
        Document weeklyForecast = documents.get(
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

    @Override
    protected double getCurrentTemp(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String temperature = getColumnValue(cityElement, CurrentWeatherColumnsEnum.TEMPERATURE);
        return parseDouble(temperature);
    }

    private Element findCity(Elements citiesTable, String city) {
        final String cityTranslation = CyrillicLatinConverter.latinToCyrillic(city);
        List<Element> collect = citiesTable.stream()
                .filter(element -> getColumnValue(element, CurrentWeatherColumnsEnum.CITY).equalsIgnoreCase(cityTranslation))
                .collect(Collectors.toList());
        return collect.isEmpty()? null : collect.get(0);
    }

    private String getColumnValue(Element city, CurrentWeatherColumnsEnum column) {
        return city.child(column.ordinal()).text();
    }

    private Elements getCurrentWeatherForAllCities(Document currentWeather) {
        return currentWeather
                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, ConstHelper.RHMZ_CURRENT_WEATHER_ALL_CITIES_TABLE)
                .get(0)
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children();
    }

    private Elements getUvIndexForAllCities(Document currentWeather) {
        return currentWeather
                .getElementById("slika_pracenje")
                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
                .children();
    }

    @Override
    public double getCurrentRealFeel(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String realFeel = getColumnValue(cityElement, CurrentWeatherColumnsEnum.REAL_FEEL);
        return parseDouble(realFeel);
    }

    @Override
    public int getCurrentHumidity(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String humidity = getColumnValue(cityElement, CurrentWeatherColumnsEnum.HUMIDITY);
        return Integer.parseInt(humidity);
    }

    @Override
    public double getCurrentPressure(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String pressure = getColumnValue(cityElement, CurrentWeatherColumnsEnum.PRESSURE);
        return parseDouble(pressure);
    }

    @Override
    public double getCurrentUvIndex(String city) {
        Element uvElem = getUvIndexWeatherFor(city);
        return uvElem == null ? 0 : parseDouble(uvElem.child(1).text());
    }

    @Override
    public double getCurrentWindSpeed(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String windSpeedStr = getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_SPEED);
        double windSpeed = 0;
        try{
            windSpeed = parseDouble(windSpeedStr);
        } catch (NumberFormatException e) {
            LOGGER.error(String.format("Error while converting wind speed value to double: %s", windSpeedStr));
        }
        return windSpeed;
    }

    @Override
    public String getCurrentWindDirection(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_DIRECTION);
    }

    @Override
    public String getCurrentDescription(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.DESCRIPTION);
    }

    @Override
    public String getCurrentDate(String city) {
        String heading = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER)
                .getElementById("sadrzaj")
                .getElementsByTag("h1").get(0)
                .text();
        String dateStr = heading.split(" ")[6];
        String[] split = dateStr.replace('.', ' ').split(" ");
        int year = Integer.parseInt(split[2]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[0]);
        return Util.formatDate(year, month, day);
    }

    @Override
    public String getCurrentImageUrl(String city) {
        Element cityElement = getCurrentWeatherFor(city);
        String imgUrl = cityElement
                .child(CurrentWeatherColumnsEnum.IMAGE.ordinal())
                .childNode(1)
                .attr("src");
        return ConstHelper.RHMZ_URL + "/repository/" + imgUrl.split("repository")[1];
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

    //NULLS NOT OVERRIDED NOT NEEDED

    @Override
    public double getForecastedMinTemp(String json) {
        return 0;
    }

    @Override
    public double getForecastedMaxTemp(String city) {
        return 0;
    }

    @Override
    public double getForecastedWindSpeed(String json) {
        return 0;
    }

    @Override
    public String getForecastedWindDirection(String json) {
        return null;
    }

    @Override
    public double getForecastedUvIndex(String json) {
        return 0;
    }

    @Override
    public String getForecastedDescription(String json) {
        return null;
    }

    @Override
    public String getForecastedDate(String city) {
        Element citiesFiveDayTable = getFiveDayWeatherFor(city);
        String h1 = citiesFiveDayTable.getElementsByTag("h1").text();
        String date = h1.split(" ")[h1.split(" ").length - 1].trim();
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        int year = Integer.parseInt(date.substring(6,10));
        return Util.formatDate(year, month, day);
    }

    @Override
    public String getForecastedImageUrl(String json) {
        return null;
    }

    @Override
    public String toString() {
        return "RhmzSource{" +
                "weatherDataMap=" + weatherDataMap +
                '}';
    }
}
