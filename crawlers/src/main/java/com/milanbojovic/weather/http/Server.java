package com.milanbojovic.weather.http;

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
import com.milanbojovic.weather.data.model.WeatherData;
import com.milanbojovic.weather.service.AccuWeatherService;
import com.milanbojovic.weather.service.RhmzService;
import com.milanbojovic.weather.service.Weather2UmbrellaService;
import com.milanbojovic.weather.service.WeatherProvider;
import com.milanbojovic.weather.util.ConstHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.StreamSupport;

import static akka.http.javadsl.server.Directives.*;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final RhmzService rhmzService;
    private final AccuWeatherService accuSource;
    private final Weather2UmbrellaService w2uSource;

    public Server(List<String> citiesList) throws IOException {
        LOGGER.debug("Creating ActorSystem.");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);

        rhmzService = new RhmzService(citiesList);
        accuSource = new AccuWeatherService(citiesList);
        w2uSource = new Weather2UmbrellaService(citiesList);

        final CompletionStage<ServerBinding> binding = startHttpServer(http);

        LOGGER.info("Akka HTTP Server online at http://" + ConstHelper.SERVER_HOST + ":" + ConstHelper.SERVER_PORT);
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
        return http.newServerAt(ConstHelper.SERVER_HOST, ConstHelper.SERVER_PORT)
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
                pathPrefix(PathMatchers.segment("rhmz").slash(), () ->
                    extractUnmatchedPath(city -> getAllWeatherDataFrom(rhmzService, city))
                )
        );
    }

    private Route createW2uRoute() {
        return concat(
                pathPrefix(PathMatchers.segment("w2u").slash(), () ->
                        extractUnmatchedPath(city -> getAllWeatherDataFrom(w2uSource, city))
                )
        );
    }

    private Route createAccuRoute() {
        return concat(
                pathPrefix(PathMatchers.segment("accu").slash(), () ->
                        extractUnmatchedPath(city -> getAllWeatherDataFrom(accuSource, city))
                )
        );
    }

    private Route getAllWeatherDataFrom(WeatherProvider source, String city) {
        LOGGER.debug("Getting all weather data for source: %s, city: %s", city);
        String cityDecoded = URLDecoder.decode(StringUtils.capitalize(city));
        WeatherData cityWeather = source.provideWeather(city);
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
