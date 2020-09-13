package com.milanbojovic.weather.data.model;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@Getter
public class WeatherData {
    @BsonProperty(value = "_id")
    private ObjectId id;
    @BsonProperty(value = "city")
    private String city;
    @BsonProperty(value = "current_weather")
    private CurrentWeather currentWeather;
    @BsonProperty(value = "weekly_forecast")
    private List<DailyForecast> weeklyForecast;
}
