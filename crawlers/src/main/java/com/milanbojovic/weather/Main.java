package com.milanbojovic.weather;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.milanbojovic.weather.http.Server;
import com.milanbojovic.weather.spider.Weather2UmbrellaSource;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Main - started");
        Server server = new Server();
        System.out.println("Main - finished");
    }
}
