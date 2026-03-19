package com.framework.base;

import com.framework.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

/**
 * BasePage.java
 *
 * Parent class for all Page Objects in the framework.
 * Initializes PageFactory so @FindBy annotations work automatically.
 * Provides shared access to driver and WaitUtils for all pages.
 *
 * Every page class must extend BasePage and call super(driver) in its constructor.
 */
public abstract class BasePage {

    protected static final Logger logger = LogManager.getLogger(BasePage.class);

    // The driver for this page
    protected AndroidDriver driver;

    // WaitUtils for explicit waits - available in all pages
    protected WaitUtils waitUtils;

    /**
     * Constructor - initializes PageFactory and WaitUtils.
     * PageFactory processes all @FindBy annotations in the subclass.
     *
     * @param driver the AndroidDriver instance
     */
    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);

        // Initialize @FindBy annotations using AppiumFieldDecorator
        // AppiumFieldDecorator understands Appium-specific locators (@AndroidFindBy, etc.)
        PageFactory.initElements(
            new AppiumFieldDecorator(driver, Duration.ofSeconds(10)),
            this
        );

        logger.debug("{} page initialized.", this.getClass().getSimpleName());
    }

    /**
     * Returns true if the page is loaded (implement in each page).
     * Override this method to add page-specific load verification.
     */
    public abstract boolean isLoaded();
}
