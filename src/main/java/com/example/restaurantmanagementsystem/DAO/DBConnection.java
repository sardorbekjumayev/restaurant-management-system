package com.example.restaurantmanagementsystem.DAO;

import com.example.restaurantmanagementsystem.config.AppProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = AppProperties.get(
            "db.url",
            "jdbc:mysql://127.0.0.1:3306/restaurant_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tashkent"
    );
    private static final String USER = AppProperties.get("db.username", "root");
    private static final String PASSWORD = AppProperties.get("db.password", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC drayver topilmadi", e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }
}
