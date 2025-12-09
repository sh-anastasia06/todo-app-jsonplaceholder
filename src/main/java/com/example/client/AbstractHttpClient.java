package com.example.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;
import java.time.Duration;

public abstract class AbstractHttpClient implements ApiClient {
    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    protected final HttpClient httpClient;
    protected String baseUrl = "https://jsonplaceholder.typicode.com";

    protected AbstractHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    protected AbstractHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        if (baseUrl != null && baseUrl.trim().isEmpty()) {
            this.baseUrl = baseUrl.endsWith("/") ?
                    baseUrl.substring(0, baseUrl.length() - 1) :
                    baseUrl;
        }
    }

    @Override
    public String getBaseUrl() { return baseUrl; }

    // helpers
    protected String buildUrl(String endpoint) {
        return baseUrl + endpoint;
    }

    // url with params
    protected String buildUrl(String endpoint, String queryParams) {
        return buildUrl(endpoint) + "?" + queryParams;
    }
}
