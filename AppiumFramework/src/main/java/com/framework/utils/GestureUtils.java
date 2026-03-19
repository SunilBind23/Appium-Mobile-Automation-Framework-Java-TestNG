package com.framework.utils;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GestureUtils.java
 *
 * Provides touch gesture methods for mobile automation:
 *  - Scroll (up, down, to element)
 *  - Swipe (directional, element-based)
 *  - Tap (single, double, long press)
 *  - Pinch/Zoom (basic)
 *
 * Uses W3C Actions API (Appium 2.x compatible).
 * Also includes UiScrollable for text-based scrolling (Android-specific).
 */
public class GestureUtils {

    private static final Logger logger = LogManager.getLogger(GestureUtils.class);

    private final AndroidDriver driver;

    // Finger pointer for W3C touch actions
    private static final PointerInput FINGER = new PointerInput(PointerInput.Kind.TOUCH, "finger");

    public GestureUtils(AndroidDriver driver) {
        this.driver = driver;
    }

    // ================================================================
    //  SCROLL METHODS
    // ================================================================

    /**
     * Scroll DOWN on the screen (most common use case).
     * Simulates a swipe from bottom to top.
     */
    public void scrollDown() {
        logger.debug("Scrolling down...");
        Dimension size = driver.manage().window().getSize();

        int startX = size.width / 2;
        int startY = (int) (size.height * 0.75);   // Start 75% down
        int endY   = (int) (size.height * 0.25);   // End 25% down

        swipeByCoordinates(startX, startY, startX, endY, 600);
    }

    /**
     * Scroll UP on the screen.
     * Simulates a swipe from top to bottom.
     */
    public void scrollUp() {
        logger.debug("Scrolling up...");
        Dimension size = driver.manage().window().getSize();

        int startX = size.width / 2;
        int startY = (int) (size.height * 0.25);
        int endY   = (int) (size.height * 0.75);

        swipeByCoordinates(startX, startY, startX, endY, 600);
    }

    /**
     * Scroll down N times.
     *
     * @param times number of scroll actions
     */
    public void scrollDown(int times) {
        for (int i = 0; i < times; i++) {
            scrollDown();
            logger.debug("Scroll down {}/{}", i + 1, times);
        }
    }

    /**
     * Scroll to find an element by its visible text using UiScrollable (Android only).
     * More reliable than coordinate-based scrolling for finding specific items.
     *
     * @param visibleText the text of the element to scroll to
     * @return the found WebElement
     */
    public WebElement scrollToElementByText(String visibleText) {
        logger.info("Scrolling to element with text: {}", visibleText);

        // UiScrollable is an Android UiAutomator strategy
        String uiScrollableSelector =
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().text(\"" + visibleText + "\"))";

        try {
            return driver.findElement(
                AppiumBy.androidUIAutomator(uiScrollableSelector)
            );
        } catch (Exception e) {
            logger.error("Could not scroll to element with text: {}", visibleText);
            throw new RuntimeException("scrollToElementByText failed for: " + visibleText, e);
        }
    }

    /**
     * Scroll to find an element by partial text.
     *
     * @param partialText partial text of the target element
     * @return the found WebElement
     */
    public WebElement scrollToElementByPartialText(String partialText) {
        logger.info("Scrolling to element with partial text: {}", partialText);

        String uiScrollableSelector =
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().textContains(\"" + partialText + "\"))";

        return driver.findElement(
            AppiumBy.androidUIAutomator(uiScrollableSelector)
        );
    }

    /**
     * Scroll within a specific container element (not the full screen).
     *
     * @param container the scrollable container element
     * @param direction "up" or "down"
     */
    public void scrollWithinElement(WebElement container, String direction) {
        logger.debug("Scrolling {} within element...", direction);

        Point location = container.getLocation();
        Dimension size  = container.getSize();

        int centerX = location.getX() + size.width / 2;
        int startY, endY;

        if (direction.equalsIgnoreCase("down")) {
            startY = location.getY() + (int) (size.height * 0.8);
            endY   = location.getY() + (int) (size.height * 0.2);
        } else {
            startY = location.getY() + (int) (size.height * 0.2);
            endY   = location.getY() + (int) (size.height * 0.8);
        }

        swipeByCoordinates(centerX, startY, centerX, endY, 500);
    }

    // ================================================================
    //  SWIPE METHODS
    // ================================================================

    /**
     * Swipe LEFT (common for carousels, tab navigation).
     */
    public void swipeLeft() {
        logger.debug("Swiping left...");
        Dimension size = driver.manage().window().getSize();

        int startX = (int) (size.width * 0.85);
        int endX   = (int) (size.width * 0.15);
        int centerY = size.height / 2;

        swipeByCoordinates(startX, centerY, endX, centerY, 400);
    }

    /**
     * Swipe RIGHT.
     */
    public void swipeRight() {
        logger.debug("Swiping right...");
        Dimension size = driver.manage().window().getSize();

        int startX = (int) (size.width * 0.15);
        int endX   = (int) (size.width * 0.85);
        int centerY = size.height / 2;

        swipeByCoordinates(startX, centerY, endX, centerY, 400);
    }

    /**
     * Swipe an element off-screen to the left (e.g., delete a list item).
     *
     * @param element the element to swipe
     */
    public void swipeElementLeft(WebElement element) {
        logger.debug("Swiping element to the left...");

        Point location = element.getLocation();
        Dimension size  = element.getSize();

        int startX = location.getX() + size.width - 10;
        int endX   = location.getX() + 10;
        int centerY = location.getY() + size.height / 2;

        swipeByCoordinates(startX, centerY, endX, centerY, 500);
    }

    /**
     * Core method: swipe from one coordinate to another.
     * All swipe/scroll methods call this under the hood.
     *
     * @param startX   starting X coordinate
     * @param startY   starting Y coordinate
     * @param endX     ending X coordinate
     * @param endY     ending Y coordinate
     * @param durationMs duration in milliseconds (higher = slower, more natural)
     */
    public void swipeByCoordinates(int startX, int startY, int endX, int endY, int durationMs) {
        logger.debug("Swiping from ({},{}) to ({},{}) in {}ms", startX, startY, endX, endY, durationMs);

        // W3C Actions API for touch gestures (Appium 2.x standard)
        Sequence swipe = new Sequence(FINGER, 0);

        swipe.addAction(FINGER.createPointerMove(Duration.ZERO,
            PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(FINGER.createPointerMove(Duration.ofMillis(durationMs),
            PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(swipe));
    }

    // ================================================================
    //  TAP METHODS
    // ================================================================

    /**
     * Tap on specific screen coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void tapByCoordinates(int x, int y) {
        logger.debug("Tapping at ({}, {})", x, y);

        Sequence tap = new Sequence(FINGER, 0);
        tap.addAction(FINGER.createPointerMove(Duration.ZERO,
            PointerInput.Origin.viewport(), x, y));
        tap.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(tap));
    }

    /**
     * Tap on the center of a WebElement.
     *
     * @param element element to tap
     */
    public void tapOnElement(WebElement element) {
        Point location = element.getLocation();
        Dimension size  = element.getSize();

        int centerX = location.getX() + size.width / 2;
        int centerY = location.getY() + size.height / 2;

        logger.debug("Tapping on element center: ({}, {})", centerX, centerY);
        tapByCoordinates(centerX, centerY);
    }

    /**
     * Long press on an element (used for context menus, drag handles, etc.).
     *
     * @param element         element to long press
     * @param durationSeconds how long to hold (seconds)
     */
    public void longPress(WebElement element, int durationSeconds) {
        logger.debug("Long pressing element for {}s...", durationSeconds);

        Point location = element.getLocation();
        Dimension size  = element.getSize();
        int centerX = location.getX() + size.width / 2;
        int centerY = location.getY() + size.height / 2;

        Sequence longPress = new Sequence(FINGER, 0);
        longPress.addAction(FINGER.createPointerMove(Duration.ZERO,
            PointerInput.Origin.viewport(), centerX, centerY));
        longPress.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(FINGER.createPointerMove(Duration.ofSeconds(durationSeconds),
            PointerInput.Origin.viewport(), centerX, centerY));
        longPress.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(longPress));
    }

    // ================================================================
    //  APP BACKGROUND / FOREGROUND
    // ================================================================

    /**
     * Send app to background for specified seconds, then bring it back.
     * Useful for testing app state persistence.
     *
     * @param seconds time to keep app in background
     */
    public void sendAppToBackground(int seconds) {
        logger.info("Sending app to background for {} seconds...", seconds);
        driver.runAppInBackground(Duration.ofSeconds(seconds));
        logger.info("App brought back to foreground.");
    }

    /**
     * Activate the app (bring to foreground if in background).
     *
     * @param appPackage the package name of the app
     */
    public void activateApp(String appPackage) {
        logger.info("Activating app: {}", appPackage);
        driver.activateApp(appPackage);
    }

    /**
     * Terminate (force-close) the app.
     *
     * @param appPackage the package name of the app
     */
    public void terminateApp(String appPackage) {
        logger.info("Terminating app: {}", appPackage);
        driver.terminateApp(appPackage);
    }

    // ================================================================
    //  PERMISSION POPUP HANDLING
    // ================================================================

    /**
     * Handles Android permission popups (Allow / Deny).
     * Attempts to click "Allow" button by common resource IDs.
     * Safe to call even if no popup is present.
     *
     * @param allow true to click Allow, false to click Deny
     */
    public void handlePermissionPopup(boolean allow) {
        logger.info("Checking for permission popup...");
        try {
            WebElement btn;
            if (allow) {
                // Try common Allow button IDs across Android versions
                btn = driver.findElement(By.id("com.android.packageinstaller:id/permission_allow_button"));
                if (btn == null) {
                    btn = driver.findElement(By.xpath(
                        "//android.widget.Button[@text='Allow' or @text='ALLOW' or @text='Allow only while using the app']"
                    ));
                }
            } else {
                btn = driver.findElement(By.xpath(
                    "//android.widget.Button[@text='Deny' or @text='DENY' or @text='Don\\'t allow']"
                ));
            }
            if (btn != null && btn.isDisplayed()) {
                btn.click();
                logger.info("Permission popup handled: {}", allow ? "ALLOW" : "DENY");
            }
        } catch (Exception e) {
            // No popup present — this is fine
            logger.debug("No permission popup found (this is OK).");
        }
    }

    /**
     * Dismiss any system alert dialog using mobile: dismissAlert command.
     */
    public void dismissSystemAlert() {
        try {
            Map<String, Object> args = new HashMap<>();
            driver.executeScript("mobile: dismissAlert", args);
            logger.info("System alert dismissed.");
        } catch (Exception e) {
            logger.debug("No system alert to dismiss.");
        }
    }
}

// ---- Inner helper class to avoid import issues with AppiumBy ----
class AppiumBy {
    public static org.openqa.selenium.By androidUIAutomator(String selector) {
        return io.appium.java_client.AppiumBy.androidUIAutomator(selector);
    }
}
