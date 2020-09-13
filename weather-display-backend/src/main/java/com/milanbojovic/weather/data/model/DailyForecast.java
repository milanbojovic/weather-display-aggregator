package com.milanbojovic.weather.data.model;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@Getter
public class DailyForecast {
    @BsonProperty(value = "provider")
    String provider;

    @BsonProperty(value = "min_temp")
    double minTemp;

    @BsonProperty(value = "max_temp")
    double maxTemp;

    @BsonProperty(value = "wind_speed")
    double windSpeed;

    @BsonProperty(value = "wind_direction")
    String windDirection;

    @BsonProperty(value = "uv_index")
    double uvIndex;

    @BsonProperty(value = "description")
    String description;

    @BsonProperty(value = "date")
    String date;

    @BsonProperty(value = "image_url")
    String imageUrl;
}
