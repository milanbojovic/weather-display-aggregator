package com.milanbojovic.weather.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milanbojovic.weather.spider.AccuWeatherSource;
import com.milanbojovic.weather.spider.RhmdzSource;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.complete;

public class Server {

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
        return concat(
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
                                }))));
    }
}
