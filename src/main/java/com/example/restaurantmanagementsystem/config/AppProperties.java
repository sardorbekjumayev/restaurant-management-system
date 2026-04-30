package com.example.restaurantmanagementsystem.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppProperties {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = AppProperties.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("application.properties yuklanmadi", e);
        }
    }

    private AppProperties() {
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }
}
