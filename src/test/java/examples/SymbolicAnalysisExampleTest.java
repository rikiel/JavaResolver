package examples;

import org.testng.annotations.Test;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;

public class SymbolicAnalysisExampleTest extends AbstractTest {
    @Test
    public void test() {
        new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addTargetEntryMethodSignature(SymbolicAnalysisExample.class, "writeValueForIdToFile(I)V")
                .addApplicationPackagePrefix("eu.profinit")
                .addStdlib()
                .addExclusions()
                .run()
                .generateVisualization();
    }
}
