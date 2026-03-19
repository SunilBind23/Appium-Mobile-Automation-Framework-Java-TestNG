package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CartPage.java
 *
 * Page Object for the Shopping Cart screen.
 * Used in AddToCartTest to verify items were added correctly.
 */
public class CartPage extends BasePage {

    // ============================================================
    //  ELEMENT LOCATORS
    // ============================================================

    // Cart page header
    @AndroidFindBy(accessibility = "test-Cart Content")
    private WebElement cartContent;

    // "YOUR CART" title
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='YOUR CART']")
    private WebElement cartTitle;

    // All items in cart
    @AndroidFindBy(accessibility = "test-Item")
    private List<WebElement> cartItems;

    // Product names inside the cart
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='test-Item']//" +
                           "android.widget.TextView[@content-desc='test-Item title']")
    private List<WebElement> cartItemNames;

    // Proceed to checkout button
    @AndroidFindBy(accessibility = "test-CHECKOUT")
    private WebElement checkoutButton;

    // Continue shopping button (go back to products)
    @AndroidFindBy(accessibility = "test-CONTINUE SHOPPING")
    private WebElement continueShoppingButton;

    // Remove item buttons
    @AndroidFindBy(accessibility = "test-REMOVE")
    private List<WebElement> removeButtons;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public CartPage(AndroidDriver driver) {
        super(driver);
    }

    // ============================================================
    //  PAGE ACTIONS
    // ============================================================

    /**
     * Verify cart page is loaded.
     */
    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(cartTitle);
    }

    /**
     * Get all product names currently in the cart.
     *
     * @return list of product name strings
     */
    public List<String> getCartItemNames() {
        waitUtils.waitForVisibility(cartContent);
        return cartItemNames.stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }

    /**
     * Get the count of items in the cart.
     *
     * @return number of items
     */
    public int getCartItemCount() {
        int count = cartItems.size();
        logger.info("Cart contains {} item(s).", count);
        return count;
    }

    /**
     * Check if a specific product is in the cart by name.
     *
     * @param productName the product name to look for
     * @return true if found in cart
     */
    public boolean isProductInCart(String productName) {
        List<String> names = getCartItemNames();
        boolean found = names.stream()
            .anyMatch(name -> name.equalsIgnoreCase(productName));
        logger.info("Product '{}' in cart: {}", productName, found);
        return found;
    }

    /**
     * Remove the first item from the cart.
     *
     * @return this CartPage
     */
    public CartPage removeFirstItem() {
        logger.info("Removing first item from cart...");
        if (!removeButtons.isEmpty()) {
            waitUtils.safeClick(removeButtons.get(0));
        }
        return this;
    }

    /**
     * Click Checkout button.
     *
     * @return new CheckoutPage instance
     */
    public CheckoutPage proceedToCheckout() {
        logger.info("Proceeding to Checkout...");
        waitUtils.safeClick(checkoutButton);
        return new CheckoutPage(driver);
    }

    /**
     * Go back to shopping (Products page).
     *
     * @return new HomePage instance
     */
    public HomePage continueShopping() {
        logger.info("Continuing shopping...");
        waitUtils.safeClick(continueShoppingButton);
        return new HomePage(driver);
    }
}
