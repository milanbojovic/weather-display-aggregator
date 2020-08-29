package com.milanbojovic.weather.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.*;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milanbojovic.weather.spider.AccuWeatherSource;
import com.milanbojovic.weather.spider.RhmdzSource;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static akka.http.javadsl.server.Directives.*;

public class Server {

    private static final List<HttpHeader> CORS_HEADERS = Arrays.asList(
            AccessControlAllowOrigin.create(HttpOriginRanges.ALL),
            AccessControlAllowMethods.create(HttpMethods.OPTIONS, HttpMethods.GET, HttpMethods.PUT,
                    HttpMethods.POST, HttpMethods.HEAD, HttpMethods.DELETE));

    public Server() throws IOException {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);

        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                        .bind(createRoutes());

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done

        System.out.println("Main - finished");
    }

    private Route createRoutes() {
        return Directives.optionalHeaderValueByType(AccessControlRequestHeaders.class, corsRequestHeaders -> {
            final ArrayList<HttpHeader> newHeaders = new ArrayList<>(CORS_HEADERS);
            corsRequestHeaders.ifPresent(toAdd ->
                    newHeaders.add(AccessControlAllowHeaders.create(
                            StreamSupport.stream(toAdd.getHeaders().spliterator(), false).toArray(String[]::new))
                    )
            );
            return route(options(() -> complete(
                    HttpResponse.create().withStatus(StatusCodes.OK).addHeaders(newHeaders))),
                    respondWithHeaders(newHeaders, () -> concat(
                            path("accuw", () ->
                                    get(() ->
                                    {
                                        try {
                                            return complete( new ObjectMapper().writeValueAsString(new AccuWeatherSource().getWeatherData()));
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    })),
                            concat(
                                    path("w2u", () ->
                                            get(() ->
                                            {
                                                try {
                                                    return complete( new ObjectMapper().writeValueAsString(new Weather2UmbrellaSource().getWeatherData()));
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }))),
                            concat(
                                    path("rhmdz", () ->
                                            get(() ->
                                            {
                                                try {
                                                    return complete( new ObjectMapper().writeValueAsString(new RhmdzSource().getWeatherData()));
                                                } catch (JsonProcessingException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            })))))
            );
        });
    }
}
