package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer.java
 *
 * Automatically retries failed tests up to MAX_RETRY_COUNT times.
 * Useful for handling flaky tests caused by network issues or animation delays.
 *
 * How to use:
 *   Option 1 - Per test: @Test(retryAnalyzer = RetryAnalyzer.class)
 *   Option 2 - All tests via listener: add RetryListener to TestNG suite
 *
 * Each test instance gets its own RetryAnalyzer (count resets per test).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);

    // Maximum number of retry attempts (not including the first execution)
    private static final int MAX_RETRY_COUNT = 2;

    // Counter tracks how many times current test has been retried
    private int retryCount = 0;

    /**
     * Called by TestNG after a test fails.
     * Return true to retry, false to mark as failed.
     *
     * @param result the failed test result
     * @return true if test should be retried
     */
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warn("RETRYING test '{}' - Attempt {}/{} | Reason: {}",
                result.getName(),
                retryCount,
                MAX_RETRY_COUNT,
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown"
            );
            return true;
        }

        logger.error("Test '{}' FAILED after {} retry attempts.", result.getName(), MAX_RETRY_COUNT);
        return false;
    }
}
