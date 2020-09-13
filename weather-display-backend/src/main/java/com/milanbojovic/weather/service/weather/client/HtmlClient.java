package com.milanbojovic.weather.service.weather.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HtmlClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlClient.class);

    private Document provideDocument(String url) {
        Document document = null;
        try {
            LOGGER.debug(String.format("Creating Jsoup connection to: %s", url));
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOGGER.error("Error while executing Jsoup connection", e);
        }
        return document;
    }

    public Pair<String, Document> provideStringDocumentPair(String url) {
        return new ImmutablePair<>(url, provideDocument(url));

    }
}
