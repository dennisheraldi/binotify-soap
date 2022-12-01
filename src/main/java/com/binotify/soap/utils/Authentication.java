package com.binotify.soap.utils;

import java.util.List;
import java.util.Map;

import com.binotify.soap.enums.ServiceType;

import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

public class Authentication {
    public static final String RESTAPIKey = System.getenv().getOrDefault("REST_API_KEY", "DefaultRESTAPIKey");
    public static final String PHPAPIKey = System.getenv().getOrDefault("PHP_API_KEY", "DefaultPHPAPIKey");

    public static boolean IsAuthenticated(WebServiceContext ctx, ServiceType type) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>)
            ctx.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> auth = headers.get("authorization");
        String key = "";
        if (type == ServiceType.REST) {
            key = RESTAPIKey;
        } else if (type == ServiceType.PHP) {
            key = PHPAPIKey;
        }
        return auth.size() > 0 && auth.get(0).equals("Bearer " + key);
    }
}
