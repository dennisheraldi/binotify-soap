package com.binotify.soap;

import com.binotify.soap.database.Client;
import com.binotify.soap.database.models.*;
import com.binotify.soap.service.SubscriptionService;

import jakarta.xml.ws.Endpoint;

public class Main {
    public static void main(String[] args) {
        Client.getInstance();
        Logging.init();
        Subscription.init();
        Endpoint.publish("http://0.0.0.0:8080/binotify/Subscription", new SubscriptionService());
    }
}
