package eu.profinit.manta.connector.java.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;

public class RunGarbageCollectorBeforeTestListener implements IResultListener {
    private static final Logger log = LoggerFactory.getLogger(RunGarbageCollectorBeforeTestListener.class);

    @Override
    public void onTestStart(ITestResult iTestResult) {
        gc();
    }

    @Override
    public void onConfigurationSuccess(ITestResult iTestResult) {
    }

    @Override
    public void onConfigurationFailure(ITestResult iTestResult) {
    }

    @Override
    public void onConfigurationSkip(ITestResult iTestResult) {
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
    }

    @Override
    public void onStart(ITestContext iTestContext) {
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
    }

    private void gc() {
        log.trace("Running garbage collector");
        System.gc();
    }
}
