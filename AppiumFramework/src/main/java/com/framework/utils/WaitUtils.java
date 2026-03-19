package com.framework.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils.java
 *
 * Provides explicit wait methods to handle dynamic elements in mobile apps.
 * Avoids flaky tests caused by elements not being ready when interacted with.
 *
 * All methods use configurable timeouts (from config.properties).
 */
public class WaitUtils {

    private static final Logger logger = LogManager.getLogger(WaitUtils.class);

    private final AndroidDriver driver;
    private final int defaultTimeout;

    /**
     * Constructor - reads default timeout from config.
     *
     * @param driver the AndroidDriver instance
     */
    public WaitUtils(AndroidDriver driver) {
        this.driver = driver;
        this.defaultTimeout = ConfigReader.getInt("explicit.wait");
    }

    /**
     * Creates a WebDriverWait with the specified timeout.
     */
    private WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // ============================================================
    //  VISIBILITY WAITS
    // ============================================================

    /**
     * Wait for element to be visible (present AND displayed on screen).
     *
     * @param element WebElement to wait for
     * @return the visible WebElement
     */
    public WebElement waitForVisibility(WebElement element) {
        logger.debug("Waiting for element to be visible...");
        return getWait(defaultTimeout)
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element to be visible with a custom timeout.
     */
    public WebElement waitForVisibility(WebElement element, int timeoutSeconds) {
        logger.debug("Waiting {}s for element visibility...", timeoutSeconds);
        return getWait(timeoutSeconds)
            .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element located by By locator to be visible.
     */
    public WebElement waitForVisibility(By locator) {
        logger.debug("Waiting for element by locator: {}", locator);
        return getWait(defaultTimeout)
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ============================================================
    //  CLICKABILITY WAITS
    // ============================================================

    /**
     * Wait for element to be clickable (visible + enabled).
     * Use this before any click() action.
     *
     * @param element WebElement to wait for
     * @return the clickable WebElement
     */
    public WebElement waitForClickability(WebElement element) {
        logger.debug("Waiting for element to be clickable...");
        return getWait(defaultTimeout)
            .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait for element by locator to be clickable.
     */
    public WebElement waitForClickability(By locator) {
        return getWait(defaultTimeout)
            .until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ============================================================
    //  PRESENCE WAITS
    // ============================================================

    /**
     * Wait for element to be present in the DOM (does not need to be visible).
     */
    public WebElement waitForPresence(By locator) {
        logger.debug("Waiting for element presence: {}", locator);
        return getWait(defaultTimeout)
            .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ============================================================
    //  TEXT WAITS
    // ============================================================

    /**
     * Wait for an element to contain specific text.
     *
     * @param element       the WebElement
     * @param expectedText  text to wait for
     * @return true when element contains the text
     */
    public boolean waitForTextToAppear(WebElement element, String expectedText) {
        logger.debug("Waiting for text '{}' in element...", expectedText);
        return getWait(defaultTimeout)
            .until(ExpectedConditions.textToBePresentInElement(element, expectedText));
    }

    // ============================================================
    //  INVISIBILITY WAITS
    // ============================================================

    /**
     * Wait for an element (like a loading spinner) to disappear.
     *
     * @param locator By locator for the element
     */
    public void waitForInvisibility(By locator) {
        logger.debug("Waiting for element to disappear: {}", locator);
        getWait(defaultTimeout)
            .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ============================================================
    //  SAFE CLICK AND TYPE
    // ============================================================

    /**
     * Wait for element to be clickable, then click it.
     * Preferred over direct element.click().
     *
     * @param element the element to click
     */
    public void safeClick(WebElement element) {
        try {
            WebElement clickable = waitForClickability(element);
            clickable.click();
            logger.debug("Element clicked successfully.");
        } catch (Exception e) {
            logger.error("Failed to click element: {}", e.getMessage());
            throw new RuntimeException("safeClick failed: " + e.getMessage(), e);
        }
    }

    /**
     * Wait for element to be visible, clear it, then type text.
     *
     * @param element the input field
     * @param text    the text to type
     */
    public void safeType(WebElement element, String text) {
        try {
            WebElement visible = waitForVisibility(element);
            visible.clear();
            visible.sendKeys(text);
            logger.debug("Typed '{}' into element.", text);
        } catch (Exception e) {
            logger.error("Failed to type into element: {}", e.getMessage());
            throw new RuntimeException("safeType failed: " + e.getMessage(), e);
        }
    }

    // ============================================================
    //  ELEMENT CHECK HELPERS
    // ============================================================

    /**
     * Checks if an element is displayed without throwing an exception.
     * Returns false if element is not found or not visible.
     *
     * @param element WebElement to check
     * @return true if visible, false otherwise
     */
    public boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | org.openqa.selenium.StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Hard pause - use sparingly, only when dynamic waits are not possible.
     *
     * @param milliseconds duration to sleep
     */
    public void hardWait(long milliseconds) {
        try {
            logger.debug("Hard wait for {}ms", milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Hard wait interrupted.");
        }
    }
}
