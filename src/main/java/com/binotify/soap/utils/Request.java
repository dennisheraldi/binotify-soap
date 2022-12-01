package com.binotify.soap.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.function.Consumer;

import com.binotify.soap.enums.ServiceType;

public class Request {
    public enum RequestType {
        GET,
        POST
    }

    public static void GET(ServiceType type, String endpoint, Consumer<? super String> action) {
        request(RequestType.GET, null, null, type, endpoint, action);
    }

    public static void POST(BodyPublisher postPub, String contentType, ServiceType type, String endpoint, Consumer<? super String> action) {
        request(RequestType.POST, postPub, contentType, type, endpoint, action);
    }

    public static void POST(BodyPublisher postPub, ServiceType type, String endpoint, Consumer<? super String> action) {
        request(RequestType.POST, postPub, null, type, endpoint, action);
    }

    public static void request(RequestType reqType, BodyPublisher postPub, String contentType, ServiceType type, String endpoint, Consumer<? super String> respCallback) {
        // Callback to the PHP service
        String urlString = "http://" +
            System.getenv().getOrDefault((type == ServiceType.PHP ? "PHP_HOST" : "REST_HOST"), "localhost") +
            endpoint;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + (
                    type == ServiceType.PHP ? Authentication.PHPAPIKey : Authentication.RESTAPIKey
                ));
            if (reqType == RequestType.GET)
                requestBuilder = requestBuilder.GET();
            else if (reqType == RequestType.POST) {
                requestBuilder = requestBuilder.POST(postPub);
                requestBuilder = requestBuilder.header("Content-Type", contentType == null ? "application/x-www-form-urlencoded" : contentType);
            }
            HttpRequest request = requestBuilder.build();
            client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(respCallback)
                .join();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
}
