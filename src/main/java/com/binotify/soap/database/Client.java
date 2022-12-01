package com.binotify.soap.database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Statement;

import java.sql.SQLException;

public final class Client {
    static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3307");
    static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "binotify-soap");
    static final String DB_USER = System.getenv().getOrDefault("DB_USER", "binotify-user");
    static final String DB_PASS = System.getenv().getOrDefault("DB_PASS", "b1n0tify!pass-user");
    static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    static Connection conn;
    static Client client;

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Client getInstance() {
        if (client == null) {
            client = new Client();
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("initialize db");
        }
        return client;
    }

    public void runExecute(String sql) throws SQLException {
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute(sql);
            stmt.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement prep(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }

    public PreparedStatement prep(String sql, boolean auto_increment) throws SQLException {
        return getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
}
