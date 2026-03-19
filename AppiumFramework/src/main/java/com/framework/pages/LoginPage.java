package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * LoginPage.java
 *
 * Page Object representing the Login screen of the app.
 * All element locators are defined here using @AndroidFindBy.
 * No test logic here — only actions and state-checks for this page.
 *
 * App under test: Sauce Labs Demo App (Android)
 * Locators may vary — update resource-id/xpath to match your actual app.
 */
public class LoginPage extends BasePage {

    // ============================================================
    //  ELEMENT LOCATORS (@AndroidFindBy = Appium-aware @FindBy)
    // ============================================================

    // Username input field
    @AndroidFindBy(accessibility = "test-Username")
    private WebElement usernameField;

    // Password input field
    @AndroidFindBy(accessibility = "test-Password")
    private WebElement passwordField;

    // Login button
    @AndroidFindBy(accessibility = "test-LOGIN")
    private WebElement loginButton;

    // Error message displayed on invalid login
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='test-Error message']/android.widget.TextView")
    private WebElement errorMessage;

    // App logo (used to verify page is loaded)
    @AndroidFindBy(accessibility = "test-Login")
    private WebElement loginContainer;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    /**
     * @param driver the AndroidDriver from BaseTest
     */
    public LoginPage(AndroidDriver driver) {
        super(driver);  // Calls BasePage → initializes PageFactory
    }

    // ============================================================
    //  PAGE ACTIONS
    // ============================================================

    /**
     * Type username into the username field.
     *
     * @param username the username string
     * @return this LoginPage (fluent API for method chaining)
     */
    public LoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        waitUtils.safeType(usernameField, username);
        return this;
    }

    /**
     * Type password into the password field.
     *
     * @param password the password string
     * @return this LoginPage
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password: [HIDDEN]");
        waitUtils.safeType(passwordField, password);
        return this;
    }

    /**
     * Click the Login button.
     * Returns HomePage since a successful login navigates there.
     *
     * @return new HomePage instance
     */
    public HomePage clickLoginButton() {
        logger.info("Clicking Login button.");
        waitUtils.safeClick(loginButton);
        return new HomePage(driver);
    }

    /**
     * Perform a complete login flow in one call.
     * Use this for happy-path login tests.
     *
     * @param username login username
     * @param password login password
     * @return HomePage after successful login
     */
    public HomePage loginWith(String username, String password) {
        return enterUsername(username)
               .enterPassword(password)
               .clickLoginButton();
    }

    /**
     * Attempt login that is expected to fail (returns LoginPage).
     * Use this for negative login tests where we want to verify the error.
     *
     * @param username login username
     * @param password login password
     * @return this LoginPage (because we stay on login screen on failure)
     */
    public LoginPage loginWithInvalidCredentials(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        waitUtils.safeClick(loginButton);
        return this;  // Stay on login page
    }

    // ============================================================
    //  PAGE STATE CHECKS
    // ============================================================

    /**
     * Returns true if the login page is currently displayed.
     */
    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(loginContainer);
    }

    /**
     * Returns true if an error message is visible after failed login.
     */
    public boolean isErrorMessageDisplayed() {
        return waitUtils.isElementDisplayed(errorMessage);
    }

    /**
     * Get the text of the error message.
     *
     * @return error message string
     */
    public String getErrorMessage() {
        logger.info("Reading error message from login page.");
        waitUtils.waitForVisibility(errorMessage);
        String text = errorMessage.getText();
        logger.info("Error message: {}", text);
        return text;
    }
}
