package com.milanbojovic.weather.http.routes;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.*;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milanbojovic.weather.config.AppConfig;
import com.milanbojovic.weather.data.model.WeatherData;
import com.milanbojovic.weather.service.persistance.MongoDao;
import com.milanbojovic.weather.service.weather.AccuWeatherService;
import com.milanbojovic.weather.service.weather.RhmzService;
import com.milanbojovic.weather.service.weather.Weather2UmbrellaService;
import com.milanbojovic.weather.service.weather.WeatherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.StreamSupport;

import static akka.http.javadsl.server.Directives.*;

@RestController
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final RhmzService rhmzService;
    private final Weather2UmbrellaService weather2UmbrellaService;
    private final AccuWeatherService accuWeatherService;
    private final AppConfig appConfig;
    private final MongoDao mongoDao;

    @Autowired
    public Server(AccuWeatherService accuWeatherService, RhmzService rhmzService, Weather2UmbrellaService weather2UmbrellaService, AppConfig appConfig, MongoDao mongoDao) throws IOException {
        LOGGER.debug("Creating ActorSystem.");
        this.accuWeatherService = accuWeatherService;
        this.rhmzService = rhmzService;
        this.weather2UmbrellaService = weather2UmbrellaService;
        this.appConfig = appConfig;
        this.mongoDao = mongoDao;

        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);

        final CompletionStage<ServerBinding> binding = startHttpServer(http);

        LOGGER.info(MessageFormat.format("Akka HTTP Server online at http://{0}:{1}",
                appConfig.getServerUrl(),
                appConfig.getServerPort()));
        LOGGER.info("Press RETURN to stop...");
        System.in.read();

        stopHttpServer(system, binding);
        LOGGER.info("Akka HTTP Server - stopped");
    }

    private void stopHttpServer(ActorSystem system, CompletionStage<ServerBinding> binding) {
        LOGGER.debug("Stopping AKKA HTTP Server");
        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    private CompletionStage<ServerBinding> startHttpServer(Http http) {
        LOGGER.debug("Starting Akka HTTP Server");
        return http.newServerAt(appConfig.getServerUrl(), Integer.parseInt(appConfig.getServerPort()))
                .bind(createRoutes());
    }

    private Route createRoutes() {
        LOGGER.debug("Creating routes");
        return optionalHeaderValueByType(AccessControlRequestHeaders.class, corsRequestHeaders -> {
            final ArrayList<HttpHeader> newHeaders = new ArrayList<>(createCorsHttpHeaders());
            corsRequestHeaders.ifPresent(toAdd ->
                    newHeaders.add(AccessControlAllowHeaders.create(
                            StreamSupport.stream(toAdd.getHeaders().spliterator(), false).toArray(String[]::new))
                    )
            );
            return route(options(() -> complete(
                    HttpResponse.create().withStatus(StatusCodes.OK).addHeaders(newHeaders))),
                    respondWithHeaders(newHeaders, () ->
                            concat(
                                    createAccuRoute(),
                                    createW2uRoute(),
                                    createRhmzRoute(),
                                    complete("Boom - Rhmz Http route not matched")
                            )
                    )
            );
        });
    }

    private Route createRhmzRoute() {
        return concat(
                pathPrefix(PathMatchers.segment(appConfig.getServerRouteRhmz()).slash(), () ->
                    extractUnmatchedPath(city -> getAllWeatherDataFrom(rhmzService, city))
                )
        );
    }

    private Route createW2uRoute() {
        return concat(
                pathPrefix(PathMatchers.segment(appConfig.getServerRouteW2u()).slash(), () ->
                        extractUnmatchedPath(city -> getAllWeatherDataFrom(weather2UmbrellaService, city))
                )
        );
    }

    private Route createAccuRoute() {
        return concat(
                pathPrefix(PathMatchers.segment(appConfig.getServerRouteAccu()).slash(), () ->
                        extractUnmatchedPath(city -> getAllWeatherDataFrom(accuWeatherService, city))
                )
        );
    }

    private Route getAllWeatherDataFrom(WeatherProvider source, String city) {
        city = city.toLowerCase();
        LOGGER.debug(String.format("Getting all weather data for city - %s", city));
        WeatherData cityWeather = source.fetchPersistedWeatherData(mongoDao, city, source.getProviderName());
        try {
            return complete(new ObjectMapper().writeValueAsString(cityWeather));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while trying to serialze weatherData to JSON");
            e.printStackTrace();
        }
        return null;
    }

    private static List<HttpHeader> createCorsHttpHeaders() {
        LOGGER.debug("Creating CORS headers");
        return Arrays.asList(
                AccessControlAllowOrigin.create(HttpOriginRanges.ALL),
                AccessControlAllowMethods.create(HttpMethods.OPTIONS, HttpMethods.GET, HttpMethods.PUT,
                        HttpMethods.POST, HttpMethods.HEAD, HttpMethods.DELETE));
    }
}
