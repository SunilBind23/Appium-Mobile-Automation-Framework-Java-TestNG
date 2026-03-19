package com.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ExtentReportManager.java
 *
 * Manages the lifecycle of Extent Reports:
 *  - Creates the ExtentReports instance (once per suite)
 *  - Creates per-test ExtentTest instances (thread-safe via ThreadLocal)
 *  - Attaches screenshots to failed tests
 *  - Flushes (writes) the report at end of suite
 *
 * Used by BaseTest @Before/@AfterMethod and TestNG listeners.
 */
public class ExtentReportManager {

    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);

    // Single ExtentReports instance shared across all threads
    private static ExtentReports extent;

    // ThreadLocal ensures each parallel test has its own ExtentTest node
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    // Private constructor - static utility class
    private ExtentReportManager() {}

    /**
     * Initialize ExtentReports with the Spark (HTML) reporter.
     * Call this ONCE in @BeforeSuite.
     */
    public static synchronized void initReport() {
        if (extent == null) {
            String reportPath = ConfigReader.get("extent.report.dir", "reports/ExtentReport.html");

            // SparkReporter generates a modern, interactive HTML report
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("Mobile Automation Test Report");
            sparkReporter.config().setReportName("Appium Android Test Results");
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Add system information to report
            extent.setSystemInfo("Framework", "Appium + TestNG + Java");
            extent.setSystemInfo("Platform", "Android");
            extent.setSystemInfo("Automation", "UiAutomator2");
            extent.setSystemInfo("Tester", System.getProperty("user.name"));

            logger.info("Extent Report initialized. Output: {}", reportPath);
        }
    }

    /**
     * Create a new test node in the report for the current thread.
     * Call this in @BeforeMethod.
     *
     * @param testName        the test method name
     * @param testDescription optional description of what the test does
     */
    public static void createTest(String testName, String testDescription) {
        ExtentTest test = extent.createTest(testName, testDescription);
        testThreadLocal.set(test);
        logger.debug("Extent test created: {}", testName);
    }

    /**
     * Get the ExtentTest for the current thread.
     *
     * @return current thread's ExtentTest node
     */
    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    /**
     * Log an INFO message to the current test node.
     *
     * @param message the log message
     */
    public static void logInfo(String message) {
        if (getTest() != null) {
            getTest().info(message);
        }
    }

    /**
     * Log a PASS status to the current test node.
     *
     * @param message success message
     */
    public static void logPass(String message) {
        if (getTest() != null) {
            getTest().pass(message);
        }
    }

    /**
     * Log a FAIL status with screenshot attached.
     * Call this in @AfterMethod when test fails.
     *
     * @param message failure message
     * @param driver  AndroidDriver for screenshot capture
     */
    public static void logFail(String message, AndroidDriver driver) {
        if (getTest() != null) {
            // Capture screenshot as Base64 and embed in report
            String base64Screenshot = ScreenshotUtils.captureScreenshotAsBase64(driver);
            if (base64Screenshot != null) {
                try {
                    getTest().fail(message,
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build()
                    );
                    logger.debug("Screenshot attached to Extent Report for failure.");
                } catch (Exception e) {
                    logger.warn("Could not attach screenshot to report: {}", e.getMessage());
                    getTest().fail(message);
                }
            } else {
                getTest().fail(message);
            }
        }
    }

    /**
     * Log a SKIP status to the current test node.
     *
     * @param message skip reason
     */
    public static void logSkip(String message) {
        if (getTest() != null) {
            getTest().skip(message);
        }
    }

    /**
     * Write all test results to the HTML file.
     * MUST be called in @AfterSuite, otherwise report won't be saved.
     */
    public static synchronized void flushReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent Report flushed (saved to disk).");
        }
    }

    /**
     * Clean up ThreadLocal to prevent memory leaks in parallel execution.
     */
    public static void removeTest() {
        testThreadLocal.remove();
    }
}
