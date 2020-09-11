package com.milanbojovic.weather.data.extraction;

import com.jayway.jsonpath.JsonPath;
import com.milanbojovic.weather.data.extraction.template.api.CurrentWeatherExtractor;
import com.milanbojovic.weather.data.model.CurrentWeather;
import com.milanbojovic.weather.util.ConstHelper;
import org.apache.commons.math3.util.Precision;
import org.cyrlat.CyrillicLatinConverter;

public class CurrentWeatherExtractorAcu implements CurrentWeatherExtractor {
    private CurrentWeather currentWeather;

    public CurrentWeatherExtractorAcu(String city) {
        currentWeather = createCurrentWeatherFrom(city);
    }

    public CurrentWeather extract() {
        return currentWeather;
    }

    @Override
    public double getTemp(String city) {
        return JsonPath.read(city, "$[0].Temperature.Metric.Value");
    }

    @Override
    public double getRealFeel(String city) {
        return JsonPath.read(city, "$[0].RealFeelTemperature.Metric.Value");
    }

    @Override
    public int getHumidity(String city) {
        return JsonPath.read(city, "$[0].RelativeHumidity");
    }

    @Override
    public double getPressure(String city) {
        return JsonPath.read(city, "$[0].Pressure.Metric.Value");
    }

    @Override
    public double getWindSpeed(String city) {
        double speedInKmh = JsonPath.read(city, "$[0].Wind.Speed.Metric.Value");
        return Precision.round(speedInKmh / 3.6, 2);
    }

    @Override
    public String getWindDirection(String city) {
                return JsonPath.read(city, "$[0].Wind.Direction.English");
    }

    @Override
    public double getUvIndex(String city) {
        int uvIndex = JsonPath.read(city, "$[0].UVIndex");
        return Double.parseDouble(Integer.toString(uvIndex));
    }

    @Override
    public String getImageUrl(String city) {
        String imgId = JsonPath.read(city, "$[0].WeatherIcon").toString();
        return assembleAccuWeatherImageUrl(imgId);
    }

    private String assembleAccuWeatherImageUrl(String imgId) {
        return String.format(
                String.format("%s%s", ConstHelper.ACCU_WEATHER_IMAGE_URL, ConstHelper.ACCU_WEATHER_API_IMAGES_LOCATION),
                String.format("%02d", Integer.parseInt(imgId)));
    }
    
    @Override
    public String getDescription(String city) {
                String latinString = JsonPath.read(city, "$[0].WeatherText");
        return CyrillicLatinConverter.latinToCyrillic(latinString);
    }

    @Override
    public String getDate(String city) {
                String dateStr = JsonPath.read(city, "$[0].LocalObservationDateTime");
        return getDayFromDateString(toDate(dateStr)) + " - " + toDate(dateStr);
    }
}
