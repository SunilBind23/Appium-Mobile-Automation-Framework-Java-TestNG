package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * OrderSummaryPage.java
 *
 * Page Object for the Order Summary and Order Confirmation screens.
 * Used to verify total price and place the final order.
 */
public class OrderSummaryPage extends BasePage {

    // ============================================================
    //  ELEMENT LOCATORS
    // ============================================================

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='CHECKOUT: OVERVIEW']")
    private WebElement summaryTitle;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='CHECKOUT: COMPLETE!']")
    private WebElement confirmationTitle;

    // Total price label
    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Total:')]")
    private WebElement totalPrice;

    // "FINISH" button to place order
    @AndroidFindBy(accessibility = "test-FINISH")
    private WebElement finishButton;

    // "BACK HOME" button after order is placed
    @AndroidFindBy(accessibility = "test-BACK HOME")
    private WebElement backHomeButton;

    // Confirmation message (e.g., "THANK YOU FOR YOU ORDER")
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='THANK YOU FOR YOU ORDER']")
    private WebElement thankYouMessage;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public OrderSummaryPage(AndroidDriver driver) {
        super(driver);
    }

    // ============================================================
    //  PAGE ACTIONS
    // ============================================================

    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(summaryTitle);
    }

    /**
     * Get the total price text from the summary.
     *
     * @return total price string (e.g., "Total: $39.98")
     */
    public String getTotalPrice() {
        waitUtils.waitForVisibility(totalPrice);
        String price = totalPrice.getText();
        logger.info("Order total: {}", price);
        return price;
    }

    /**
     * Click FINISH to place the order.
     *
     * @return this OrderSummaryPage (now showing confirmation)
     */
    public OrderSummaryPage placeOrder() {
        logger.info("Placing order by clicking FINISH...");
        waitUtils.safeClick(finishButton);
        return this;
    }

    /**
     * Check if the Thank You confirmation message is displayed.
     *
     * @return true if order was placed successfully
     */
    public boolean isOrderConfirmed() {
        boolean confirmed = waitUtils.isElementDisplayed(thankYouMessage);
        logger.info("Order confirmed: {}", confirmed);
        return confirmed;
    }

    /**
     * Navigate back to Home after order is placed.
     *
     * @return new HomePage instance
     */
    public HomePage goBackHome() {
        logger.info("Going back to home after order...");
        waitUtils.safeClick(backHomeButton);
        return new HomePage(driver);
    }
}
