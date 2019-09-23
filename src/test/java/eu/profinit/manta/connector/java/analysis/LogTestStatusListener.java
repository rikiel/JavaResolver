package eu.profinit.manta.connector.java.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.reporters.VerboseReporter;

public class LogTestStatusListener extends VerboseReporter {
    private static final Logger log = LoggerFactory.getLogger(LogTestStatusListener.class);

    public LogTestStatusListener() {
        super("");
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        if (tr.getThrowable() != null) {
            log.trace("vvv Test successfully throw exception vvv", tr.getThrowable());
            log.trace("^^^ Test successfully throw exception ^^^");
        }
    }

    @Override
    protected void log(String message) {
        log.trace(message);
    }
}
