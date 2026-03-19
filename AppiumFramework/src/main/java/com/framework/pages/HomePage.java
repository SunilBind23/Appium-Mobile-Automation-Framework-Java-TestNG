package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * HomePage.java
 *
 * Page Object representing the Home (Products) screen after login.
 * Exposes actions like:
 *  - Verify page is loaded
 *  - Get product list title
 *  - Add product to cart
 *  - Navigate to cart
 *  - Logout
 *
 * App under test: Sauce Labs Demo App
 */
public class HomePage extends BasePage {

    // ============================================================
    //  ELEMENT LOCATORS
    // ============================================================

    // Page title (e.g., "Products")
    @AndroidFindBy(accessibility = "test-Cart drop zone")
    private WebElement cartDropZone;

    @AndroidFindBy(xpath = "//android.widget.TextView[@content-desc='test-Item title']")
    private List<WebElement> productTitles;

    // Menu button (top-left hamburger icon)
    @AndroidFindBy(accessibility = "test-Menu")
    private WebElement menuButton;

    // Cart icon / badge
    @AndroidFindBy(accessibility = "test-Cart")
    private WebElement cartIcon;

    // Cart badge count (shows number of items in cart)
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='test-Cart']/android.view.ViewGroup/android.widget.TextView")
    private WebElement cartBadgeCount;

    // Products container (indicates page is loaded)
    @AndroidFindBy(accessibility = "test-PRODUCTS")
    private WebElement productsHeader;

    // All "Add to Cart" buttons (one per product)
    @AndroidFindBy(accessibility = "test-ADD TO CART")
    private List<WebElement> addToCartButtons;

    // Logout option in side menu
    @AndroidFindBy(accessibility = "test-LOGOUT")
    private WebElement logoutButton;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public HomePage(AndroidDriver driver) {
        super(driver);
    }

    // ============================================================
    //  PAGE ACTIONS
    // ============================================================

    /**
     * Check if the Products page has loaded.
     *
     * @return true if products header is visible
     */
    @Override
    public boolean isLoaded() {
        return waitUtils.isElementDisplayed(productsHeader);
    }

    /**
     * Get the text of the Products page header.
     *
     * @return header text (e.g., "PRODUCTS")
     */
    public String getPageTitle() {
        waitUtils.waitForVisibility(productsHeader);
        String title = productsHeader.getText();
        logger.info("Home page title: {}", title);
        return title;
    }

    /**
     * Add the first product to cart.
     * Use this for a simple "add to cart" test.
     *
     * @return this HomePage (still on same screen)
     */
    public HomePage addFirstProductToCart() {
        logger.info("Adding first product to cart...");
        if (!addToCartButtons.isEmpty()) {
            waitUtils.safeClick(addToCartButtons.get(0));
            logger.info("First product added to cart.");
        } else {
            throw new RuntimeException("No 'Add to Cart' buttons found on the page.");
        }
        return this;
    }

    /**
     * Add a product to cart by its index (0-based).
     *
     * @param index 0-based index of the product in the list
     * @return this HomePage
     */
    public HomePage addProductToCartByIndex(int index) {
        logger.info("Adding product at index {} to cart...", index);
        waitUtils.waitForVisibility(addToCartButtons.get(0));
        waitUtils.safeClick(addToCartButtons.get(index));
        return this;
    }

    /**
     * Get the cart badge count (number shown on cart icon).
     *
     * @return cart item count as integer, 0 if badge not shown
     */
    public int getCartItemCount() {
        try {
            waitUtils.waitForVisibility(cartBadgeCount, 5);
            int count = Integer.parseInt(cartBadgeCount.getText().trim());
            logger.info("Cart item count: {}", count);
            return count;
        } catch (Exception e) {
            logger.debug("Cart badge not visible - cart likely empty.");
            return 0;
        }
    }

    /**
     * Navigate to the Cart screen.
     *
     * @return new CartPage instance
     */
    public CartPage goToCart() {
        logger.info("Navigating to Cart...");
        waitUtils.safeClick(cartIcon);
        return new CartPage(driver);
    }

    /**
     * Get all visible product titles on the page.
     *
     * @return list of product title strings
     */
    public List<String> getAllProductNames() {
        waitUtils.waitForVisibility(productTitles.get(0));
        return productTitles.stream()
            .map(WebElement::getText)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Open the side navigation menu.
     *
     * @return this HomePage
     */
    public HomePage openMenu() {
        logger.info("Opening side menu...");
        waitUtils.safeClick(menuButton);
        return this;
    }

    /**
     * Logout from the app.
     * Opens the menu and clicks Logout.
     *
     * @return new LoginPage instance
     */
    public LoginPage logout() {
        logger.info("Logging out...");
        openMenu();
        waitUtils.waitForVisibility(logoutButton);
        waitUtils.safeClick(logoutButton);
        logger.info("Logged out successfully.");
        return new LoginPage(driver);
    }
}
