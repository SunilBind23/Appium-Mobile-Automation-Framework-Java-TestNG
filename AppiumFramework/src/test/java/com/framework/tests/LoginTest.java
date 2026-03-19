package com.framework.tests;

import com.framework.base.BaseTest;
import com.framework.pages.HomePage;
import com.framework.pages.LoginPage;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.JsonDataReader;
import com.framework.utils.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * LoginTest.java
 *
 * Test class covering all login scenarios:
 *  1. Valid login → should navigate to Home page
 *  2. Invalid credentials → should show error message
 *  3. Locked out user → should show specific error
 *  4. Empty credentials → should show validation error
 *
 * Test data is loaded from resources/testdata.json
 * All tests extend BaseTest for driver setup/teardown.
 */
public class LoginTest extends BaseTest {

    // ============================================================
    //  TEST CASES
    // ============================================================

    /**
     * TC_LOGIN_001
     * Verify successful login with valid credentials.
     * Expected: User lands on the Products (Home) page.
     */
    @Test(
        description = "Verify successful login with valid credentials navigates to Home page",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testValidLogin() {
        logger.info("TC_LOGIN_001: Testing valid login flow.");
        ExtentReportManager.logInfo("Loading valid user credentials from testdata.json");

        // Load test data from JSON
        String username = JsonDataReader.getValue("loginData.validUser.username");
        String password = JsonDataReader.getValue("loginData.validUser.password");
        String expectedTitle = JsonDataReader.getValue("loginData.validUser.expectedHomeTitle");

        ExtentReportManager.logInfo("Username: " + username);

        // Step 1: Initialize Login Page
        LoginPage loginPage = new LoginPage(getDriver());
        Assert.assertTrue(loginPage.isLoaded(), "Login page should be loaded on app launch.");
        ExtentReportManager.logInfo("Login page loaded successfully.");

        // Step 2: Perform login
        HomePage homePage = loginPage.loginWith(username, password);
        ExtentReportManager.logInfo("Login action performed.");

        // Step 3: Verify navigation to Home page
        Assert.assertTrue(homePage.isLoaded(),
            "Home page should be displayed after successful login.");

        // Step 4: Verify page title
        String actualTitle = homePage.getPageTitle();
        Assert.assertTrue(actualTitle.toUpperCase().contains(expectedTitle.toUpperCase()),
            "Expected page title to contain '" + expectedTitle + "' but got: " + actualTitle);

        ExtentReportManager.logPass("Valid login successful. Home page title: " + actualTitle);
        logger.info("TC_LOGIN_001 PASSED. Home page title: {}", actualTitle);
    }

    /**
     * TC_LOGIN_002
     * Verify login fails with invalid credentials.
     * Expected: Error message is displayed on the login screen.
     */
    @Test(
        description = "Verify error message appears when invalid credentials are entered",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testInvalidLogin() {
        logger.info("TC_LOGIN_002: Testing invalid login flow.");

        // Load invalid user data from JSON
        String username      = JsonDataReader.getValue("loginData.invalidUser.username");
        String password      = JsonDataReader.getValue("loginData.invalidUser.password");
        String expectedError = JsonDataReader.getValue("loginData.invalidUser.expectedError");

        ExtentReportManager.logInfo("Testing login with invalid user: " + username);

        // Step 1: Initialize Login Page
        LoginPage loginPage = new LoginPage(getDriver());
        Assert.assertTrue(loginPage.isLoaded(), "Login page should be loaded.");

        // Step 2: Attempt login with invalid credentials
        loginPage.loginWithInvalidCredentials(username, password);
        ExtentReportManager.logInfo("Login attempted with invalid credentials.");

        // Step 3: Verify still on Login page (no navigation)
        Assert.assertTrue(loginPage.isLoaded(),
            "Should remain on Login page after failed login.");

        // Step 4: Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Error message should be visible after invalid login.");

        // Step 5: Verify error message text
        String actualError = loginPage.getErrorMessage();
        Assert.assertTrue(actualError.contains(expectedError),
            "Expected error: '" + expectedError + "' but got: '" + actualError + "'");

        ExtentReportManager.logPass("Invalid login correctly shows error: " + actualError);
        logger.info("TC_LOGIN_002 PASSED. Error message shown: {}", actualError);
    }

    /**
     * TC_LOGIN_003
     * Verify locked-out user cannot log in.
     * Expected: Specific locked-out error message is displayed.
     */
    @Test(
        description = "Verify locked-out user sees appropriate error message",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testLockedOutUserLogin() {
        logger.info("TC_LOGIN_003: Testing locked-out user login.");

        String username      = JsonDataReader.getValue("loginData.lockedUser.username");
        String password      = JsonDataReader.getValue("loginData.lockedUser.password");
        String expectedError = JsonDataReader.getValue("loginData.lockedUser.expectedError");

        ExtentReportManager.logInfo("Testing locked-out user: " + username);

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.loginWithInvalidCredentials(username, password);

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Error message should be shown for locked-out user.");

        String actualError = loginPage.getErrorMessage();
        Assert.assertTrue(actualError.contains(expectedError),
            "Locked-out error mismatch. Expected: '" + expectedError + "' Got: '" + actualError + "'");

        ExtentReportManager.logPass("Locked-out user correctly blocked. Error: " + actualError);
        logger.info("TC_LOGIN_003 PASSED.");
    }

    /**
     * TC_LOGIN_004
     * Verify validation when login is attempted with empty username field.
     * Expected: Error message prompts user to enter username.
     */
    @Test(
        description = "Verify error is shown when login is attempted with empty username",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testLoginWithEmptyUsername() {
        logger.info("TC_LOGIN_004: Testing login with empty username.");
        ExtentReportManager.logInfo("Attempting login with blank username.");

        LoginPage loginPage = new LoginPage(getDriver());

        // Only enter password, leave username blank
        loginPage.loginWithInvalidCredentials("", "some_password");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Error message should appear for empty username.");

        String error = loginPage.getErrorMessage();
        Assert.assertFalse(error.isEmpty(), "Error message text should not be empty.");

        ExtentReportManager.logPass("Empty username correctly blocked. Error: " + error);
        logger.info("TC_LOGIN_004 PASSED. Error: {}", error);
    }

    /**
     * TC_LOGIN_005
     * Verify login with empty password shows an error.
     */
    @Test(
        description = "Verify error is shown when login is attempted with empty password",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testLoginWithEmptyPassword() {
        logger.info("TC_LOGIN_005: Testing login with empty password.");
        ExtentReportManager.logInfo("Attempting login with blank password.");

        String username = JsonDataReader.getValue("loginData.validUser.username");
        LoginPage loginPage = new LoginPage(getDriver());

        // Enter username but leave password blank
        loginPage.loginWithInvalidCredentials(username, "");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Error message should appear for empty password.");

        String error = loginPage.getErrorMessage();
        Assert.assertFalse(error.isEmpty(), "Error message should not be empty.");

        ExtentReportManager.logPass("Empty password correctly blocked. Error: " + error);
        logger.info("TC_LOGIN_005 PASSED. Error: {}", error);
    }
}
