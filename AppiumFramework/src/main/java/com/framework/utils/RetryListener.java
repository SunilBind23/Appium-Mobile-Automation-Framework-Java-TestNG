package com.framework.utils;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * RetryListener.java
 *
 * TestNG IAnnotationTransformer that automatically attaches RetryAnalyzer
 * to EVERY @Test method without needing to add retryAnalyzer = RetryAnalyzer.class
 * to each individual test.
 *
 * Register this in testng.xml as a listener:
 * <listeners>
 *     <listener class-name="com.framework.utils.RetryListener"/>
 * </listeners>
 */
public class RetryListener implements IAnnotationTransformer {

    /**
     * Called by TestNG for every @Test annotation before execution.
     * We use it to inject RetryAnalyzer globally.
     */
    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        // Only inject if test doesn't already have a custom retry analyzer
        if (annotation.getRetryAnalyzer() == null) {
            annotation.setRetryAnalyzerClass(RetryAnalyzer.class);
        }
    }
}
