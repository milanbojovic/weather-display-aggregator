package com.milanbojovic.weather.service.weather.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);
    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private String provideJson(HttpRequest request) {
        Optional<HttpResponse<String>> response = Optional.empty();
        try {
            LOGGER.debug(String.format("Executing API request: %s", request.toString()));
            response = Optional.of(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            LOGGER.error("Error occurred while executing http request.", e);
            Thread.currentThread().interrupt();
        }
        return getResponseString(response);
    }

    public Pair<String, String> provideStringStringPair(HttpRequest request) {
        return new ImmutablePair<>(request.uri().getPath(), provideJson(request));
    }

    private String getResponseString(Optional<HttpResponse<String>> response) {
        return response.isPresent() ? response.get().body() : "Empty";
    }
}
