package com.framework.base;

import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DriverManager.java
 *
 * Manages AndroidDriver instances using ThreadLocal.
 * ThreadLocal ensures each thread (parallel test) gets its own driver instance,
 * preventing conflicts during parallel execution.
 *
 * This class is used by BaseTest and should never be accessed from test classes directly.
 */
public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);

    /**
     * ThreadLocal holds one AndroidDriver per thread.
     * When tests run in parallel (different threads), each thread has its own isolated driver.
     */
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    // Private constructor - this is a utility class, no instances needed
    private DriverManager() {}

    /**
     * Store the driver for the current thread.
     *
     * @param driver initialized AndroidDriver instance
     */
    public static void setDriver(AndroidDriver driver) {
        driverThreadLocal.set(driver);
        logger.info("Driver set for thread: {}", Thread.currentThread().getName());
    }

    /**
     * Retrieve the driver for the current thread.
     *
     * @return AndroidDriver for this thread
     */
    public static AndroidDriver getDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                "Driver not initialized for thread: " + Thread.currentThread().getName() +
                ". Make sure @BeforeMethod in BaseTest ran successfully."
            );
        }
        return driver;
    }

    /**
     * Remove the driver for the current thread to prevent memory leaks.
     * Always call this in @AfterMethod.
     */
    public static void removeDriver() {
        driverThreadLocal.remove();
        logger.info("Driver removed for thread: {}", Thread.currentThread().getName());
    }

    /**
     * Check if a driver exists for the current thread.
     *
     * @return true if driver is initialized
     */
    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
}
