package com.framework.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotUtils.java
 *
 * Captures screenshots on demand or on test failure.
 * Screenshots are saved with a timestamp to the reports/screenshots/ directory.
 *
 * Used by:
 *  - BaseTest (@AfterMethod on failure)
 *  - ExtentReportManager (to attach to reports)
 */
public class ScreenshotUtils {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);

    // Read screenshot directory from config
    private static final String SCREENSHOT_DIR = ConfigReader.get("screenshot.dir", "reports/screenshots/");

    // Date format for screenshot file names
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

    // Private constructor - utility class
    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and saves it to the screenshots directory.
     *
     * @param driver   the AndroidDriver
     * @param fileName base name for the screenshot file (test name recommended)
     * @return absolute path of the saved screenshot file, or null if capture failed
     */
    public static String captureScreenshot(AndroidDriver driver, String fileName) {
        if (driver == null) {
            logger.warn("Cannot capture screenshot - driver is null.");
            return null;
        }

        try {
            // Create screenshots directory if it doesn't exist
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Build unique file name with timestamp
            String timestamp = DATE_FORMAT.format(new Date());
            String sanitizedName = fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String filePath = SCREENSHOT_DIR + sanitizedName + "_" + timestamp + ".png";

            // Take screenshot
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File(filePath);

            // Copy to destination
            FileUtils.copyFile(srcFile, destFile);

            logger.info("Screenshot saved: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            logger.error("Failed to save screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot and returns it as a Base64 string.
     * Base64 is used by Extent Reports to embed screenshots directly in the HTML report.
     *
     * @param driver the AndroidDriver
     * @return Base64-encoded screenshot string, or null if capture failed
     */
    public static String captureScreenshotAsBase64(AndroidDriver driver) {
        if (driver == null) {
            logger.warn("Cannot capture screenshot - driver is null.");
            return null;
        }

        try {
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            logger.debug("Screenshot captured as Base64.");
            return base64Screenshot;
        } catch (Exception e) {
            logger.error("Failed to capture Base64 screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot as a byte array.
     * Useful for attaching to Allure reports.
     *
     * @param driver the AndroidDriver
     * @return byte array of the screenshot
     */
    public static byte[] captureScreenshotAsBytes(AndroidDriver driver) {
        if (driver == null) {
            logger.warn("Cannot capture screenshot - driver is null.");
            return new byte[0];
        }

        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
}
