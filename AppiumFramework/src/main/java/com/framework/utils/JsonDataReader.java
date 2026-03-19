package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * JsonDataReader.java
 *
 * Reads test data from a JSON file using Jackson library.
 * Supports nested JSON structures (e.g., loginData.validUser.username).
 *
 * Usage:
 *   String username = JsonDataReader.getValue("loginData.validUser.username");
 *   String error    = JsonDataReader.getValue("loginData.invalidUser.expectedError");
 */
public class JsonDataReader {

    private static final Logger logger = LogManager.getLogger(JsonDataReader.class);

    // Singleton ObjectMapper - reuse for performance
    private static final ObjectMapper mapper = new ObjectMapper();

    // Cached root JSON node
    private static JsonNode rootNode;

    // Private constructor - utility class
    private JsonDataReader() {}

    /**
     * Loads the JSON file once and caches the root node.
     */
    private static void loadJsonFile() {
        if (rootNode == null) {
            String jsonPath = ConfigReader.get("testdata.path");
            File jsonFile = new File(jsonPath);

            if (!jsonFile.exists()) {
                // Try relative to project root
                jsonFile = new File(System.getProperty("user.dir"), jsonPath);
            }

            try {
                rootNode = mapper.readTree(jsonFile);
                logger.info("Test data loaded from: {}", jsonFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to load test data JSON: {}", e.getMessage());
                throw new RuntimeException("Cannot load test data from: " + jsonPath, e);
            }
        }
    }

    /**
     * Get a string value using a dot-notation path.
     * Example: "loginData.validUser.username"
     *
     * @param dotPath dot-separated path to the value
     * @return value as String
     */
    public static String getValue(String dotPath) {
        loadJsonFile();

        String[] keys = dotPath.split("\\.");
        JsonNode current = rootNode;

        for (String key : keys) {
            current = current.get(key);
            if (current == null) {
                throw new RuntimeException("JSON path not found: " + dotPath + " (failed at key: " + key + ")");
            }
        }

        logger.debug("Test data [{}] = {}", dotPath, current.asText());
        return current.asText();
    }

    /**
     * Get a JsonNode object for a given path (for complex nested data).
     *
     * @param dotPath dot-separated path
     * @return JsonNode at that path
     */
    public static JsonNode getNode(String dotPath) {
        loadJsonFile();

        String[] keys = dotPath.split("\\.");
        JsonNode current = rootNode;

        for (String key : keys) {
            current = current.get(key);
            if (current == null) {
                throw new RuntimeException("JSON node not found: " + dotPath);
            }
        }

        return current;
    }

    /**
     * Reload JSON file (call this if test data changes between test runs).
     */
    public static void reload() {
        rootNode = null;
        loadJsonFile();
    }
}
