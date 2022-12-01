package com.binotify.soap.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

import org.json.JSONArray;

public class Mailer {
    static final String from = System.getenv().getOrDefault("MAILER_FROM", "Binotify SOAP <noreply@soap.binotify.com>");
    static final String username = System.getenv().getOrDefault("MAILER_USER", "mail-user");
    static final String password = System.getenv().getOrDefault("MAILER_PASS", "mail-pw");
    static final Properties props = new Properties() {
        {
            put("mail.smtp.auth", "true");
            put("mail.smtp.starttls.enable", "true");
            put("mail.smtp.host", System.getenv().getOrDefault("MAILER_HOST", "smtp.gmail.com"));
            put("mail.smtp.port", System.getenv().getOrDefault("MAILER_PORT", "465"));
        }
    };
    static final Session session = Session.getInstance(props,
        new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

    public static void sendMail(String to, String subject, String textMsg) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(textMsg);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void notifyAdminNewSub(String textMsg, String adminJson) {
        JSONArray resp = new JSONArray(adminJson);
        if (resp.length() == 0) {
            System.out.println("No admin to notify: " + textMsg);
            return;
        }
        String to = resp.getJSONObject(0).getString("email");
        sendMail(to, "New subscription request to Binotify SOAP", textMsg);
        System.out.println("Sent email to admin <" + to + "> with message: " + textMsg);
    }
}
