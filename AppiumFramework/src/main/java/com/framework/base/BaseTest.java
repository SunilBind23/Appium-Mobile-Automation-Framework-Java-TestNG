package com.framework.base;

import com.framework.utils.ExtentReportManager;
import com.framework.utils.GestureUtils;
import com.framework.utils.ScreenshotUtils;
import com.framework.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * BaseTest.java
 *
 * Parent class for ALL test classes in the framework.
 * Handles:
 *  - Driver initialization before each test (@BeforeMethod)
 *  - Driver teardown after each test (@AfterMethod)
 *  - Extent Report creation per test
 *  - Screenshot capture on test failure
 *  - Suite-level report init and flush (@BeforeSuite / @AfterSuite)
 *
 * Every test class MUST extend BaseTest.
 *
 * Parallel execution:
 *  - Each test thread gets its own driver via DriverManager (ThreadLocal)
 *  - Device targeting is controlled by testng.xml parameters
 */
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);

    // Shared utilities - available in all test subclasses
    protected WaitUtils waitUtils;
    protected GestureUtils gestureUtils;

    // ============================================================
    //  SUITE-LEVEL HOOKS
    // ============================================================

    /**
     * Called ONCE before the entire test suite starts.
     * Initializes the Extent Report file.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        logger.info("===== TEST SUITE STARTED =====");
        ExtentReportManager.initReport();
    }

    /**
     * Called ONCE after all tests in the suite complete.
     * Flushes (writes) the Extent Report to disk.
     */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        ExtentReportManager.flushReport();
        logger.info("===== TEST SUITE COMPLETED =====");
    }

    // ============================================================
    //  TEST-LEVEL HOOKS
    // ============================================================

    /**
     * Called before EACH test method.
     *
     * Steps:
     * 1. Read device params from TestNG XML (supports parallel device targeting)
     * 2. Create AndroidDriver via AppiumDriverFactory
     * 3. Store driver in DriverManager (ThreadLocal)
     * 4. Initialize utility classes
     * 5. Create Extent Report test node
     *
     * @param deviceName      injected from testng.xml <parameter> (optional)
     * @param platformVersion injected from testng.xml <parameter> (optional)
     * @param udid            injected from testng.xml <parameter> (optional)
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters({"deviceName", "platformVersion", "udid"})
    public void setUp(
            @Optional("") String deviceName,
            @Optional("") String platformVersion,
            @Optional("") String udid,
            ITestResult result) {

        String testName = result.getMethod().getMethodName();
        String testDescription = result.getMethod().getDescription();

        logger.info("------------------------------------------------------------");
        logger.info("STARTING TEST: {}", testName);
        logger.info("Device: {} | Platform: {} | UDID: {}",
            deviceName.isEmpty() ? "config default" : deviceName,
            platformVersion.isEmpty() ? "config default" : platformVersion,
            udid.isEmpty() ? "config default" : udid);

        // Initialize driver - pass nulls to use config defaults
        AndroidDriver driver = AppiumDriverFactory.createDriver(
            deviceName.isEmpty()      ? null : deviceName,
            platformVersion.isEmpty() ? null : platformVersion,
            udid.isEmpty()            ? null : udid
        );

        // Store in ThreadLocal for parallel safety
        DriverManager.setDriver(driver);

        // Initialize shared utility instances
        waitUtils    = new WaitUtils(driver);
        gestureUtils = new GestureUtils(driver);

        // Create Extent Report test entry for this test
        ExtentReportManager.createTest(testName,
            testDescription != null && !testDescription.isEmpty()
                ? testDescription : "Test: " + testName);

        ExtentReportManager.logInfo("Test started on device: " +
            (deviceName.isEmpty() ? "default" : deviceName));

        logger.info("Driver initialized. Test setup complete.");
    }

    /**
     * Called after EACH test method (regardless of pass/fail).
     *
     * Steps:
     * 1. Log pass/fail to Extent Report
     * 2. Capture screenshot on failure
     * 3. Quit driver
     * 4. Clean up ThreadLocal
     *
     * @param result the TestNG result (PASS, FAIL, SKIP)
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        AndroidDriver driver = DriverManager.hasDriver() ? DriverManager.getDriver() : null;

        String testName = result.getMethod().getMethodName();

        try {
            switch (result.getStatus()) {
                case ITestResult.SUCCESS:
                    logger.info("TEST PASSED: {}", testName);
                    ExtentReportManager.logPass("Test passed successfully.");
                    break;

                case ITestResult.FAILURE:
                    logger.error("TEST FAILED: {} | Reason: {}",
                        testName,
                        result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");

                    // Save screenshot to file
                    if (driver != null) {
                        String screenshotPath = ScreenshotUtils.captureScreenshot(driver, testName);
                        logger.info("Failure screenshot saved: {}", screenshotPath);
                    }

                    // Attach screenshot to Extent Report
                    ExtentReportManager.logFail(
                        "Test FAILED: " + (result.getThrowable() != null
                            ? result.getThrowable().getMessage() : "Unknown error"),
                        driver
                    );
                    break;

                case ITestResult.SKIP:
                    logger.warn("TEST SKIPPED: {}", testName);
                    ExtentReportManager.logSkip("Test was skipped: " +
                        (result.getThrowable() != null ? result.getThrowable().getMessage() : "No reason"));
                    break;

                default:
                    logger.warn("TEST STATUS UNKNOWN: {}", testName);
            }
        } finally {
            // Always quit driver and clean up — even if logging fails
            if (driver != null) {
                try {
                    driver.quit();
                    logger.info("Driver quit successfully.");
                } catch (Exception e) {
                    logger.warn("Error while quitting driver: {}", e.getMessage());
                }
            }
            DriverManager.removeDriver();
            ExtentReportManager.removeTest();
            logger.info("TEST TEARDOWN COMPLETE: {}", testName);
            logger.info("------------------------------------------------------------");
        }
    }

    // ============================================================
    //  HELPER: get driver in test methods
    // ============================================================

    /**
     * Convenience method to get the current thread's driver.
     * Use this in test classes instead of DriverManager directly.
     *
     * @return AndroidDriver for current thread
     */
    protected AndroidDriver getDriver() {
        return DriverManager.getDriver();
    }
}
