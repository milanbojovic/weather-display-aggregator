package com.milanbojovic.weather;

import com.milanbojovic.weather.http.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Main - started");
        Server server = new Server();
        System.out.println("Main - finished");
    }
}
