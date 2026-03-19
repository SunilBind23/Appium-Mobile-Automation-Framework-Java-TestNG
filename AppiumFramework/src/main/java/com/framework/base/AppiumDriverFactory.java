package com.framework.base;

import com.framework.utils.ConfigReader;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * AppiumDriverFactory.java
 *
 * Responsible for creating and configuring AndroidDriver instances.
 * Reads all configuration from config.properties via ConfigReader.
 *
 * Supports:
 *  - Emulator setup
 *  - Real device setup (via UDID)
 *  - Auto-grant permissions (no manual popup handling needed for basic permissions)
 *  - System property override for CI/CD (device name, UDID, platform version)
 */
public class AppiumDriverFactory {

    private static final Logger logger = LogManager.getLogger(AppiumDriverFactory.class);

    /**
     * Creates an AndroidDriver using UiAutomator2Options.
     * All values are read from config.properties (with optional system property overrides).
     *
     * @return fully initialized AndroidDriver
     */
    public static AndroidDriver createDriver() {
        return createDriver(null, null, null);
    }

    /**
     * Overloaded: creates driver with device-specific overrides.
     * Used for parallel execution where each thread targets a different device.
     *
     * @param deviceName      device name override (nullable → falls back to config)
     * @param platformVersion platform version override (nullable)
     * @param udid            device UDID override (nullable)
     * @return fully initialized AndroidDriver
     */
    public static AndroidDriver createDriver(String deviceName, String platformVersion, String udid) {

        logger.info("Initializing AndroidDriver...");

        // ---- Resolve device config (system prop → parameter → config file) ----
        String resolvedDevice    = resolve("device.name",       deviceName);
        String resolvedVersion   = resolve("platform.version",  platformVersion);
        String resolvedUdid      = resolve("udid",              udid);
        String appPath           = ConfigReader.get("app.path");
        String appPackage        = ConfigReader.get("app.package");
        String appActivity       = ConfigReader.get("app.activity");
        String automationName    = ConfigReader.get("automation.name");

        // ---- Resolve absolute path to APK ----
        File appFile = new File(appPath);
        if (!appFile.exists()) {
            // Try relative to project root
            appFile = new File(System.getProperty("user.dir"), appPath);
        }
        if (!appFile.exists()) {
            logger.warn("APK not found at: {}. Proceeding anyway (app may already be installed).", appFile.getAbsolutePath());
        }

        // ---- Build UiAutomator2Options (replacement for DesiredCapabilities in Appium 2.x) ----
        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName(ConfigReader.get("platform.name"));
        options.setDeviceName(resolvedDevice);
        options.setPlatformVersion(resolvedVersion);
        options.setUdid(resolvedUdid);
        options.setAutomationName(automationName);

        // App configuration
        if (appFile.exists()) {
            options.setApp(appFile.getAbsolutePath());
        } else {
            // If APK not found, use package + activity (app must already be installed)
            options.setAppPackage(appPackage);
            options.setAppActivity(appActivity);
        }

        // Reset behavior
        options.setNoReset(ConfigReader.getBoolean("no.reset"));
        options.setFullReset(ConfigReader.getBoolean("full.reset"));

        // Auto-grant runtime permissions (handles basic permission popups automatically)
        options.setAutoGrantPermissions(ConfigReader.getBoolean("auto.grant.permissions"));

        // Prevent driver from timing out during long test steps
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getInt("new.command.timeout")));

        // Faster app launch - skip system bar visibility checks
        options.setSkipServerInstallation(false);

        logger.info("Capabilities configured:");
        logger.info("  Device    : {}", resolvedDevice);
        logger.info("  UDID      : {}", resolvedUdid);
        logger.info("  Platform  : Android {}", resolvedVersion);
        logger.info("  App       : {}", appFile.getAbsolutePath());
        logger.info("  Automation: {}", automationName);

        // ---- Connect to Appium Server ----
        URL appiumServerUrl = getAppiumServerUrl();

        try {
            AndroidDriver driver = new AndroidDriver(appiumServerUrl, options);
            logger.info("AndroidDriver created successfully for device: {}", resolvedDevice);
            return driver;
        } catch (Exception e) {
            logger.error("Failed to create AndroidDriver: {}", e.getMessage());
            throw new RuntimeException("Driver initialization failed. " +
                "Is Appium server running at " + appiumServerUrl + "?", e);
        }
    }

    /**
     * Builds Appium server URL from config.
     */
    private static URL getAppiumServerUrl() {
        String serverUrl = ConfigReader.get("appium.server.url");
        try {
            return new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + serverUrl, e);
        }
    }

    /**
     * Resolves a config value with fallback priority:
     * 1. System property (CI/CD override)
     * 2. Direct parameter (parallel test override)
     * 3. config.properties value
     */
    private static String resolve(String configKey, String paramValue) {
        // Check system property first
        String sysVal = System.getProperty(configKey);
        if (sysVal != null && !sysVal.isEmpty()) {
            return sysVal;
        }
        // Then use passed parameter
        if (paramValue != null && !paramValue.isEmpty()) {
            return paramValue;
        }
        // Finally fall back to config file
        return ConfigReader.get(configKey);
    }
}
