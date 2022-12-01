package com.binotify.soap.utils;

import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import com.sun.net.httpserver.HttpExchange;

import java.sql.SQLException;

import com.binotify.soap.database.models.Logging;

public class Logger {
    public static void log(WebServiceContext ctx, String description, String endpoint) {
        MessageContext mc = ctx.getMessageContext();
        HttpExchange exchange = (HttpExchange) mc.get("com.sun.xml.ws.http.exchange");
        Logging l = new Logging();
        l.ip = exchange.getRemoteAddress().getHostName();
        l.description = description;
        l.endpoint = endpoint;
        try {
            l.save();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
