package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader.java
 *
 * Singleton utility to read values from config.properties.
 * Supports override via System properties (useful for CI/CD pipelines).
 *
 * Usage: ConfigReader.get("device.name")
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);

    // Singleton instance of Properties
    private static Properties properties;

    // Path to the config file
    private static final String CONFIG_FILE_PATH = "resources/config.properties";

    // Private constructor - prevents instantiation
    private ConfigReader() {}

    /**
     * Loads the properties file once and caches it.
     * Called automatically on first access.
     */
    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
                properties.load(fis);
                logger.info("config.properties loaded successfully from: {}", CONFIG_FILE_PATH);
            } catch (IOException e) {
                logger.error("Failed to load config.properties: {}", e.getMessage());
                throw new RuntimeException("Could not load config.properties. Check path: " + CONFIG_FILE_PATH);
            }
        }
    }

    /**
     * Get a property value by key.
     * System property takes priority over config file (for CI/CD overrides).
     *
     * @param key the property key (e.g., "device.name")
     * @return the property value as String
     */
    public static String get(String key) {
        loadProperties();

        // System property overrides config file (useful for -DdeviceName=... in CI)
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            logger.debug("Using system property for key '{}': {}", key, systemValue);
            return systemValue;
        }

        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property '{}' not found in config.properties", key);
            throw new RuntimeException("Missing required config property: " + key);
        }

        logger.debug("Config key='{}', value='{}'", key, value);
        return value.trim();
    }

    /**
     * Get a property value with a default fallback.
     * Useful for optional properties.
     *
     * @param key          the property key
     * @param defaultValue value to return if key is missing
     * @return property value or defaultValue
     */
    public static String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (RuntimeException e) {
            logger.debug("Key '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get a property value as integer.
     *
     * @param key the property key
     * @return integer value
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Get a property value as boolean.
     *
     * @param key the property key
     * @return boolean value
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
