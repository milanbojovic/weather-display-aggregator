//package com.milanbojovic.weather.spider;
//
//import com.jayway.jsonpath.JsonPath;
//import com.milanbojovic.weather.util.ConstHelper;
//import com.milanbojovic.weather.util.CurrentWeatherColumnsEnum;
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.text.MessageFormat;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class RhmzSource extends AbstractWeatherSource {
//    private static final Logger LOGGER = LoggerFactory.getLogger(RhmzSource.class);
//    public static final String CURRENT_WEATHER_ALL_CITIES_TABLE = "Подаци са метеоролошких станица - Србија";
//    public static final String FIVE_DAY_FORECAST_ALL_CITIES_TABLE = "5 day forecast for Beograd";
//    private final Map<String, Document> documents;
//
//    public RhmzSource(List<String> cities) {
//        super("Rhmz Provider");
//        LOGGER.info("Creating Rhmz Source");
//        documents = createUriList().stream()
//                .map(url -> this.requestUriToResponseDocTuple(url, RhmzSource::getResourcePath))
//                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
//        persistAllWeatherDataToMap(cities);
//    }
//
//    private List<String> createUriList() {
//        return Arrays.asList(
//                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_THREE_DAY_FORECAST,
//                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER,
//                ConstHelper.RHMZ_URL + ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX
//        );
//    }
//
//    private static String getResourcePath(String uri) {
//        return uri.split(ConstHelper.RHMZ_URL)[1];
//    }
//
//    private Element getCurrentWeatherFor(String city) {
//        LOGGER.debug(MessageFormat.format("Fetching current weather data for {0}", city));
//        Document currentWeather = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_CURRENT_WEATHER);
//        Elements citiesTable = getCurrentWeatherForAllCities(currentWeather);
//        return findCity(citiesTable, city);
//    }
//
//    private Element getFiveDayWeatherFor(String city) {
//        LOGGER.debug(String.format("Fetching weekly weather data for %s", city));
//        Document weeklyForecast = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_THREE_DAY_FORECAST);
//        Elements citiesTable = getDailyForecastForAllCities(weeklyForecast);
//        return findCity(citiesTable, city);
//    }
//
//    private Element getUvIndexWeatherFor(String city) {
//        LOGGER.debug(String.format("Fetching UV index data for {0}", city));
//        Document uvIndexDoc = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_UV_INDEX);
//        Elements uvIndexTable = getUvIndexForAllCities(uvIndexDoc);
//        return findCity(uvIndexTable, city);
//    }
//
//    private Elements getDailyForecastForAllCities(Document fiveDayWeather) {
//        return (Elements) fiveDayWeather
//                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, FIVE_DAY_FORECAST_ALL_CITIES_TABLE)
//                .get(0)
//                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
//                .children().stream()
//                .filter(element -> element.childrenSize() > 1)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    protected Elements getWeeklyForecast(String city) {
//        LOGGER.debug(String.format("Fetching weekly weather data for %s", city));
//        Document weeklyForecast = documents.get(ConstHelper.RHMZ_URI_PATH + ConstHelper.RHMZ_URI_THREE_DAY_FORECAST);
//        return getDailyForecastForAllCities(weeklyForecast);
//    }
//
//    @Override
//    protected double getCurrentTemp(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String temperature = getColumnValue(cityElement, CurrentWeatherColumnsEnum.TEMPERATURE);
//        return Double.parseDouble(temperature);
//    }
//
//    private Element findCity(Elements citiesTable, String city) {
//        return citiesTable.stream()
//                .filter(element -> getColumnValue(element, CurrentWeatherColumnsEnum.CITY).equalsIgnoreCase(city))
//                .collect(Collectors.toList())
//                .get(0);
//    }
//
//    private String getColumnValue(Element city, CurrentWeatherColumnsEnum column) {
//        return city.child(column.ordinal()).text();
//    }
//
//    private Elements getCurrentWeatherForAllCities(Document currentWeather) {
//        return currentWeather
//                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, CURRENT_WEATHER_ALL_CITIES_TABLE)
//                .get(0)
//                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
//                .children();
//    }
//
//    private Elements getUvIndexForAllCities(Document currentWeather) {
//        return currentWeather
//                .getElementById("slika_pracenje")
//                .getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0)
//                .children();
//    }
//
//    @Override
//    public double getRealFeel(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String realFeel = getColumnValue(cityElement, CurrentWeatherColumnsEnum.REAL_FEEL);
//        return Double.parseDouble(realFeel);
//    }
//
//    @Override
//    public int getHumidity(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String humidity = getColumnValue(cityElement, CurrentWeatherColumnsEnum.HUMIDITY);
//        return Integer.parseInt(humidity);
//    }
//
//    @Override
//    public double getPressure(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String pressure = getColumnValue(cityElement, CurrentWeatherColumnsEnum.PRESSURE);
//        return Double.parseDouble(pressure);
//    }
//
//    @Override
//    public double getUvIndex(String city) {
//        Element dailyUxIndexForCity = getUvIndexWeatherFor(city);
//        return Double.parseDouble(dailyUxIndexForCity.child(1).text());
//    }
//
//    @Override
//    public double getWindSpeed(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String windSpeed = getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_SPEED);
//        return Double.parseDouble(windSpeed);
//    }
//
//    @Override
//    public String getWindDirection(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.WIND_DIRECTION);
//    }
//
//    @Override
//    public String getDescription(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        return getColumnValue(cityElement, CurrentWeatherColumnsEnum.DESCRIPTION);
//    }
//
//    @Override
//    public String getImageUrl(String city) {
//        Element cityElement = getCurrentWeatherFor(city);
//        String imgUrl = cityElement
//                .child(CurrentWeatherColumnsEnum.IMAGE.ordinal())
//                .childNode(1)
//                .attr("src");
//        return ConstHelper.RHMZ_URL + "/repository/" + imgUrl.split("repository")[1];
//    }
//
//    @Override
//    public double getMinTemp(Element element) {
//        //Min temp doesn't exist for current day
//        return 0;
//    }
//
//
//    @Override
//    public double getMaxTemp(String city) {
////        Element cityElement = getCurrentWeatherFor(city);
////        getColumnValue(cityElement, CurrentWeatherColumnsEnum.CITY);
////        Node node = city.childNodes().get(3).childNode(0);
////        return Double.parseDouble(node.toString());
//        return 0;
//    }
//
////    private Element getWeatherForecastForecastFor(String city) {
////        LOGGER.debug("Fetching five day forecast");
////        return getCitiesFiveDayTable().child(3);
////    }
////
////    private Element getCitiesFiveDayTable() {
////        Elements citiesTable = getFiveDayWeatherFor()
////                .getElementsByAttributeValue(ConstHelper.W2U_SUMMARY, "Прогноза времена за Србију");
////        return citiesTable.get(0).getElementsByTag(ConstHelper.RHMZ_TAG_TBODY).get(0);
////    }
//
//    @Override
//    public String getDate(String city) {
//        Element citiesFiveDayTable = getFiveDayWeatherFor(city);
//        String h1 = citiesFiveDayTable.getElementsByTag("h1").text();
//        String date = h1.split(" ")[h1.split(" ").length - 1].trim();
//        int day = Integer.parseInt(date.substring(0,2));
//        int month = Integer.parseInt(date.substring(3,5));
//        int year = Integer.parseInt(date.substring(6,10));
//        return year + "-" + month + "-" + day;
//    }
//}
