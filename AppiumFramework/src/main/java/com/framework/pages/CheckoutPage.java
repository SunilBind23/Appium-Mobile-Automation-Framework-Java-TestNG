package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * CheckoutPage.java
 *
 * Page Object for the Checkout Information screen.
 * Collects first name, last name, and postal code before placing order.
 */
public class CheckoutPage extends BasePage {

    // ============================================================
    //  ELEMENT LOCATORS
    // ============================================================

    @AndroidFindBy(accessibility = "test-First Name")
    private WebElement firstNameField;

    @AndroidFindBy(accessibility = "test-Last Name")
    private WebElement lastNameField;

    @AndroidFindBy(accessibility = "test-Zip/Postal Code")
    private WebElement postalCodeField;

    @AndroidFindBy(accessibility = "test-CONTINUE")
    private WebElement continueButton;

    @AndroidFindBy(accessibility = "test-CANCEL")
    private WebElement cancelButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='CHECKOUT: INFORMATION']")
    private WebElement checkoutTitle;

    // Error shown when required fields are missing
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='test-Error message']/android.widget.TextView")
    private WebElement errorMessage;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public CheckoutPage(AndroidDriver driver) {
        super(driver);
    }

    // ============================================================
    //  PAGE ACTIONS
    // ============================================================

    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(checkoutTitle);
    }

    /**
     * Fill in checkout information and continue.
     *
     * @param firstName  buyer's first name
     * @param lastName   buyer's last name
     * @param postalCode postal / zip code
     * @return OrderSummaryPage after filling info
     */
    public OrderSummaryPage fillCheckoutInfo(String firstName, String lastName, String postalCode) {
        logger.info("Filling checkout info: {} {} - {}", firstName, lastName, postalCode);
        waitUtils.safeType(firstNameField, firstName);
        waitUtils.safeType(lastNameField, lastName);
        waitUtils.safeType(postalCodeField, postalCode);
        waitUtils.safeClick(continueButton);
        return new OrderSummaryPage(driver);
    }

    /**
     * Get error message text (e.g., when required fields are empty).
     */
    public String getErrorMessage() {
        waitUtils.waitForVisibility(errorMessage);
        return errorMessage.getText();
    }

    /**
     * Cancel checkout and go back to cart.
     */
    public CartPage cancelCheckout() {
        logger.info("Cancelling checkout.");
        waitUtils.safeClick(cancelButton);
        return new CartPage(driver);
    }
}
