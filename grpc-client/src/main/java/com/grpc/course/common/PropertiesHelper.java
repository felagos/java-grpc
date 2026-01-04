package com.grpc.course.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesHelper.class);

    private PropertiesHelper() {
    }

    public static Map<String, String> loadPropertiesFromFile() {
        Map<String, String> config = new HashMap<>();
        Properties properties = new Properties();
        
        try (InputStream input = PropertiesHelper.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("Unable to find application.properties, using defaults");
                return config;
            }
            properties.load(input);

            properties.stringPropertyNames().forEach(key -> {
                config.put(key, properties.getProperty(key));
            });
            
            return config;
        } catch (IOException e) {
            logger.error("Error loading application.properties, using defaults", e);
            return config;
        }
    }
}
