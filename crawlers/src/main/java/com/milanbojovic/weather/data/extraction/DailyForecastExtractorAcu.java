package com.milanbojovic.weather.data.extraction;

import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.extraction.template.api.DailyForecastExtractor;
import com.milanbojovic.weather.data.model.DailyForecast;
import com.milanbojovic.weather.util.ConstHelper;
import org.apache.commons.math3.util.Precision;
import org.cyrlat.CyrillicLatinConverter;

public class DailyForecastExtractorAcu implements DailyForecastExtractor {
    private final DailyForecast dailyForecast;
    private final AppConfig appConfig;

    public DailyForecastExtractorAcu(String city, AppConfig appConfig) {
        this.appConfig = appConfig;
        this.dailyForecast = createDailyForecastFor(city);
    }

    private DailyForecast createDailyForecastFor(String city) {
        return DailyForecast.builder()
                .minTemp(getMinTemp(city))
                .maxTemp(getMaxTemp(city))
                .windSpeed(getWindSpeed(city))
                .windDirection(getWindDirection(city))
                .uvIndex(getUvIndex(city))
                .description(getDescription(city))
                .date(getDate(city))
                .imageUrl(getImageUrl(city))
                .build();
    }

    public DailyForecast extract() {
        return dailyForecast;
    }

    @Override
    public double getMinTemp(String city) {
        return JsonPath.read(city, "$.Temperature.Minimum.Value");
    }

    @Override
    public double getMaxTemp(String city) {
        return JsonPath.read(city, "$.Temperature.Maximum.Value");
    }

    @Override
    public double getWindSpeed(String city) {
        double speedInKmh = JsonPath.read(city, "$.Day.Wind.Speed.Value");
        return Precision.round(speedInKmh / 3.6, 2);
    }

    @Override
    public String getWindDirection(String city) {
        return JsonPath.read(city, "$.Day.Wind.Direction.English");
    }

    @Override
    public double getUvIndex(String city) {
        int uvIndex = JsonPath.read(city, "$.AirAndPollen[5].Value");
        return Double.parseDouble(Integer.toString(uvIndex));
    }

    @Override
    public String getDescription(String city) {
        String latinString = JsonPath.read(city, "$.Day.IconPhrase");
        return CyrillicLatinConverter.latinToCyrillic(latinString);
    }

    @Override
    public String getDate(String city) {
        String dateStr = JsonPath.read(city, "$.Date");
        return getDayFromDateString(toDate(dateStr)) + " - " + toDate(dateStr);
    }

    @Override
    public String getImageUrl(String city) {
        String imgId = JsonPath.read(city, "$.Day.Icon").toString();
        return assembleAccuWeatherImageUrl(imgId);
    }

    private String assembleAccuWeatherImageUrl(String imgId) {
        return String.format(
                String.format("%s%s", appConfig.getAccuWeatherImageFetchUrl(), ConstHelper.ACCU_WEATHER_API_IMAGES_LOCATION),
                String.format("%02d", Integer.parseInt(imgId)));
    }
}
