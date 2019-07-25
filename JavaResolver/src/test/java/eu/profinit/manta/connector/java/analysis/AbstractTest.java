package eu.profinit.manta.connector.java.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners({ LogTestStatusListener.class, RunGarbageCollectorBeforeTestListener.class })
public class AbstractTest {
    protected final Logger log = LoggerFactory.getLogger(getClass());
}
