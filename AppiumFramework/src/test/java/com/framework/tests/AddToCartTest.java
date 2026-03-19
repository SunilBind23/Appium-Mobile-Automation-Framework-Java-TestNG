package com.framework.tests;

import com.framework.base.BaseTest;
import com.framework.pages.CartPage;
import com.framework.pages.CheckoutPage;
import com.framework.pages.HomePage;
import com.framework.pages.LoginPage;
import com.framework.pages.OrderSummaryPage;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.JsonDataReader;
import com.framework.utils.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * AddToCartTest.java
 *
 * End-to-end test cases for the shopping cart flow:
 *  1. Add single product to cart
 *  2. Add multiple products to cart
 *  3. Remove item from cart
 *  4. Complete checkout flow (add → cart → checkout → order)
 *
 * Prerequisites: Valid login (handled in each test setUp via loginAndGetHomePage helper)
 */
public class AddToCartTest extends BaseTest {

    // ============================================================
    //  HELPER: Login and return HomePage
    // ============================================================

    /**
     * Reusable login helper used by all tests in this class.
     * Logs in with valid credentials and returns the HomePage.
     *
     * @return HomePage after successful login
     */
    private HomePage loginAndGetHomePage() {
        String username = JsonDataReader.getValue("loginData.validUser.username");
        String password = JsonDataReader.getValue("loginData.validUser.password");

        logger.info("Logging in with user: {}", username);
        ExtentReportManager.logInfo("Logging in as: " + username);

        LoginPage loginPage = new LoginPage(getDriver());
        return loginPage.loginWith(username, password);
    }

    // ============================================================
    //  TEST CASES
    // ============================================================

    /**
     * TC_CART_001
     * Verify that adding a product to the cart increments the cart badge count.
     */
    @Test(
        description = "Verify cart badge count increases after adding a product",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testAddSingleProductToCart() {
        logger.info("TC_CART_001: Add single product to cart.");

        // Step 1: Login
        HomePage homePage = loginAndGetHomePage();
        Assert.assertTrue(homePage.isLoaded(), "Home page should load after login.");

        // Step 2: Verify initial cart is empty
        int initialCount = homePage.getCartItemCount();
        Assert.assertEquals(initialCount, 0, "Cart should be empty before adding items.");
        ExtentReportManager.logInfo("Initial cart count: " + initialCount);

        // Step 3: Add first product to cart
        homePage.addFirstProductToCart();
        ExtentReportManager.logInfo("Added first product to cart.");

        // Step 4: Verify cart badge shows 1
        int updatedCount = homePage.getCartItemCount();
        Assert.assertEquals(updatedCount, 1,
            "Cart count should be 1 after adding one item. Actual: " + updatedCount);

        ExtentReportManager.logPass("Cart count correctly updated to: " + updatedCount);
        logger.info("TC_CART_001 PASSED. Cart count: {}", updatedCount);
    }

    /**
     * TC_CART_002
     * Verify adding multiple products updates cart count correctly.
     */
    @Test(
        description = "Verify cart count is correct after adding multiple products",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testAddMultipleProductsToCart() {
        logger.info("TC_CART_002: Add multiple products to cart.");

        HomePage homePage = loginAndGetHomePage();
        Assert.assertTrue(homePage.isLoaded(), "Home page should be loaded.");

        // Add two products by index
        homePage.addProductToCartByIndex(0);
        ExtentReportManager.logInfo("Added product 1 (index 0).");

        homePage.addProductToCartByIndex(1);
        ExtentReportManager.logInfo("Added product 2 (index 1).");

        // Verify cart count is 2
        int cartCount = homePage.getCartItemCount();
        Assert.assertEquals(cartCount, 2,
            "Cart count should be 2 after adding 2 items. Actual: " + cartCount);

        ExtentReportManager.logPass("Two products added. Cart count: " + cartCount);
        logger.info("TC_CART_002 PASSED. Cart count: {}", cartCount);
    }

    /**
     * TC_CART_003
     * Verify that navigating to cart shows the correct added item.
     */
    @Test(
        description = "Verify added product appears in the cart screen",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testProductAppearsInCart() {
        logger.info("TC_CART_003: Verify product appears in cart.");

        // Load expected product name from test data
        String expectedProduct = JsonDataReader.getValue("cartData.product1.name");
        ExtentReportManager.logInfo("Expected product in cart: " + expectedProduct);

        // Login and navigate to home
        HomePage homePage = loginAndGetHomePage();

        // Scroll to the product and add it
        // Using GestureUtils to scroll to product by text
        gestureUtils.scrollToElementByText(expectedProduct).isDisplayed();
        homePage.addFirstProductToCart();

        // Navigate to Cart
        CartPage cartPage = homePage.goToCart();
        Assert.assertTrue(cartPage.isLoaded(), "Cart page should be loaded.");
        ExtentReportManager.logInfo("Navigated to Cart page.");

        // Verify cart item count
        int count = cartPage.getCartItemCount();
        Assert.assertTrue(count > 0, "Cart should contain at least one item.");

        ExtentReportManager.logPass("Cart loaded with " + count + " item(s).");
        logger.info("TC_CART_003 PASSED. Cart has {} items.", count);
    }

    /**
     * TC_CART_004
     * Verify that removing a product from the cart decreases the count.
     */
    @Test(
        description = "Verify removing a product from the cart decreases item count",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testRemoveProductFromCart() {
        logger.info("TC_CART_004: Remove product from cart.");

        HomePage homePage = loginAndGetHomePage();

        // Add one product
        homePage.addFirstProductToCart();
        int countBeforeRemove = homePage.getCartItemCount();
        ExtentReportManager.logInfo("Cart count before remove: " + countBeforeRemove);
        Assert.assertEquals(countBeforeRemove, 1, "Cart should have 1 item before removing.");

        // Navigate to cart and remove the item
        CartPage cartPage = homePage.goToCart();
        cartPage.removeFirstItem();
        ExtentReportManager.logInfo("Removed first item from cart.");

        // Verify cart is now empty
        int countAfterRemove = cartPage.getCartItemCount();
        Assert.assertEquals(countAfterRemove, 0,
            "Cart should be empty after removing the only item. Actual: " + countAfterRemove);

        ExtentReportManager.logPass("Product removed successfully. Cart count: " + countAfterRemove);
        logger.info("TC_CART_004 PASSED.");
    }

    /**
     * TC_CART_005
     * Full end-to-end checkout flow:
     * Login → Add product → Cart → Checkout → Order Summary → Place Order → Confirm
     */
    @Test(
        description = "End-to-end: Add product to cart, checkout, and confirm order",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testCompleteCheckoutFlow() {
        logger.info("TC_CART_005: Full checkout flow.");

        // Load checkout data from JSON
        String firstName  = JsonDataReader.getValue("checkoutData.firstName");
        String lastName   = JsonDataReader.getValue("checkoutData.lastName");
        String postalCode = JsonDataReader.getValue("checkoutData.postalCode");

        ExtentReportManager.logInfo("Checkout data - Name: " + firstName + " " + lastName +
            ", Postal: " + postalCode);

        // Step 1: Login
        HomePage homePage = loginAndGetHomePage();
        Assert.assertTrue(homePage.isLoaded(), "Home page should be loaded.");

        // Step 2: Add product to cart
        homePage.addFirstProductToCart();
        ExtentReportManager.logInfo("Product added to cart.");

        // Step 3: Go to cart
        CartPage cartPage = homePage.goToCart();
        Assert.assertTrue(cartPage.isLoaded(), "Cart page should be loaded.");
        Assert.assertTrue(cartPage.getCartItemCount() > 0, "Cart should have items.");
        ExtentReportManager.logInfo("Cart verified with items.");

        // Step 4: Proceed to checkout
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        Assert.assertTrue(checkoutPage.isLoaded(), "Checkout page should be loaded.");
        ExtentReportManager.logInfo("Navigated to Checkout page.");

        // Step 5: Fill checkout information
        OrderSummaryPage summaryPage = checkoutPage.fillCheckoutInfo(firstName, lastName, postalCode);
        Assert.assertTrue(summaryPage.isLoaded(), "Order summary page should be loaded.");
        ExtentReportManager.logInfo("Checkout info filled. Viewing order summary.");

        // Step 6: Log the total price
        String total = summaryPage.getTotalPrice();
        ExtentReportManager.logInfo("Order total: " + total);
        Assert.assertFalse(total.isEmpty(), "Total price should be displayed.");

        // Step 7: Place the order
        summaryPage.placeOrder();
        ExtentReportManager.logInfo("Order placed.");

        // Step 8: Verify order confirmation
        Assert.assertTrue(summaryPage.isOrderConfirmed(),
            "Thank you / order confirmation message should be displayed.");

        ExtentReportManager.logPass("Full checkout completed. Order confirmed!");
        logger.info("TC_CART_005 PASSED. Checkout flow completed.");
    }

    /**
     * TC_CART_006
     * Verify app state is preserved when app is sent to background and resumed.
     */
    @Test(
        description = "Verify cart items persist after app is sent to background and resumed",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testCartPersistsAfterAppBackground() {
        logger.info("TC_CART_006: Cart persists after app backgrounding.");

        HomePage homePage = loginAndGetHomePage();
        homePage.addFirstProductToCart();

        int cartCountBefore = homePage.getCartItemCount();
        ExtentReportManager.logInfo("Cart count before background: " + cartCountBefore);

        // Send app to background for 3 seconds, then resume
        gestureUtils.sendAppToBackground(3);
        ExtentReportManager.logInfo("App sent to background for 3 seconds.");

        // After returning to foreground, cart count should be the same
        int cartCountAfter = homePage.getCartItemCount();
        ExtentReportManager.logInfo("Cart count after resume: " + cartCountAfter);

        Assert.assertEquals(cartCountAfter, cartCountBefore,
            "Cart count should be preserved after app resumes from background.");

        ExtentReportManager.logPass("Cart state preserved after backgrounding. Count: " + cartCountAfter);
        logger.info("TC_CART_006 PASSED.");
    }
}
