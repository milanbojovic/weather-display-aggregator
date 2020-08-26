package com.milanbojovic.weather.spider;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RhmdzSource extends  WeatherSource {
    static final String URL = "http://hidmet.gov.rs";
    static final String PATH = "/ciril";

    static final String FIVE_DAY_FORECAST = "/prognoza/index.php";
    static final String CURRENT_WEATHER = "/osmotreni/index.php";
    static final String UV_INDEX = "/prognoza/uv1.php";

    private final Map<String, Document> documents;

    public RhmdzSource() {
        documents = createUriList().stream()
                .map(this::uriDocTuple)
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        weatherData = initializeWeatherData();
    }

    private List<String> createUriList() {
        return Arrays.asList(
                URL + PATH + FIVE_DAY_FORECAST,
                URL + PATH + CURRENT_WEATHER,
                URL + PATH + UV_INDEX
        );
    }

    private ImmutablePair<String, Document> uriDocTuple(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImmutablePair<>(getResourcePath(url), document);
    }

    private String getResourcePath(String url) {
        return url.split(URL)[1];
    }

    private Document getCurrentWeather() {
        return documents.get( PATH + CURRENT_WEATHER);
    }

    private Document getFiveDayWeather() {
        return documents.get( PATH + FIVE_DAY_FORECAST);
    }

    private Document getUvIndexWeather() {
        return documents.get( PATH + UV_INDEX);
    }

    @Override
    public int getMinTemp() {
        //Min temp doesn't exist for current day
        return 0;
    }

    private Element getBelgradeFromFiveDayForecast() {
        return getCitiesFiveDayTable().child(3);
    }

    private Element getCitiesFiveDayTable() {
        Elements citiesTable = getFiveDayWeather().getElementsByAttributeValue("summary", "Прогноза времена за Србију");
        return citiesTable.get(0).getElementsByTag("tbody").get(0);
    }

    private Element getCitiesCurrentWeatherTable() {
        Elements citiesTable = getCurrentWeather().getElementsByAttributeValue("summary", "Подаци са метеоролошких станица - Србија");
        return citiesTable.get(0).getElementsByTag("tbody").get(0);
    }

    @Override
    public int getMaxTemp() {
        Node city = getBelgradeFromFiveDayForecast();
        Node node = city.childNodes().get(5).childNode(0);
        return Integer.parseInt(node.toString());
    }

    @Override
    public int getRealFeel() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return Integer.parseInt(city.child(6).text());
    }

    @Override
    public int getHumidity() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return Integer.parseInt(city.child(5).text());
    }

    @Override
    public int getPressure() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return (int) Math.round(Double.parseDouble(city.child(2).text()));
    }

    @Override
    public double getUvIndex() {
        Element cityLine = getUvIndexWeather().getElementById("slika_pracenje").getElementsByTag("tbody").get(0).child(10);
        return Double.parseDouble(cityLine.child(1).text());
    }

    @Override
    public double getWindSpeed() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return Double.parseDouble(city.child(4).text());
    }

    @Override
    public String getWindDirection() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return city.child(3).text();
    }

    @Override
    public String getDescription() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        return city.child(8).text();
    }

    @Override
    public String getImageUrl() {
        Element city = getCitiesCurrentWeatherTable().child(7);
        String imgUrl = city.child(7).childNode(1).attr("src");
        return URL + "/repository/" + imgUrl.split("repository")[1];
    }

    @Override
    public Date getDate() {
        Element citiesFiveDayTable = getFiveDayWeather();
        String h1 = citiesFiveDayTable.getElementsByTag("h1").text();
        String date = h1.split(" ")[h1.split(" ").length - 1].trim();
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        int year = Integer.parseInt(date.substring(6,10));
        return new Date(year, month, day);
    }
}
