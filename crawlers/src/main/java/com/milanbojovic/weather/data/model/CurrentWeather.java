package com.milanbojovic.weather.data.model;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@Getter
public class CurrentWeather {
    @BsonProperty(value = "current_temp")
    double currentTemp;

    @BsonProperty(value = "real_feel")
    double realFeel;

    @BsonProperty(value = "humidity")
    int humidity;

    @BsonProperty(value = "pressure")
    double pressure;

    @BsonProperty(value = "uv_index")
    double uvIndex;

    @BsonProperty(value = "wind_speed")
    double windSpeed;

    @BsonProperty(value = "wind_direction")
    String windDirection;

    @BsonProperty(value = "description")
    String description;

    @BsonProperty(value = "image_url")
    String imageUrl;

    @BsonProperty(value = "date")
    String date;
}

