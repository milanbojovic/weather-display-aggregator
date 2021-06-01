package com.milanbojovic.weather.service.persistance;

import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.WeatherData;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@SuppressWarnings("deprecation")
@Service
public class MongoDao {
    MongoClientSettings clientSettings;
    ConnectionString connectionString;
    CodecRegistry codecRegistry;
    CodecRegistry pojoCodecRegistry;

    @Autowired
    public MongoDao(AppConfig appConfig) {
        connectionString = new ConnectionString(appConfig.getMongoConnectionUrl());
        pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);
        clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
    }

    public  void writeWeatherData(List<WeatherData> weatherData, String provider) {
        try (var mongoClient = MongoClients.create(clientSettings)) {
            var db = mongoClient.getDatabase("weather");
            var weatherDataCollection = db.getCollection(provider, WeatherData.class);
            weatherDataCollection.insertMany(weatherData);
        }
    }

    public WeatherData readWeatherData(String city, String collection) {
        WeatherData weatherData;
        city = StringUtils.capitalize(URLDecoder.decode(city));
        try (var mongoClient = MongoClients.create(clientSettings)) {
            var db = mongoClient.getDatabase("weather");
            var collectionWeatherData = db.getCollection(collection, WeatherData.class);

            weatherData = collectionWeatherData.find(new Document("city", city)).first();
        }
        return weatherData;
    }
}
