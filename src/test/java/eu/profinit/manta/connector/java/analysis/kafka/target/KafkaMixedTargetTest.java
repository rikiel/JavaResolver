package eu.profinit.manta.connector.java.analysis.kafka.target;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.TestData;
import eu.profinit.manta.connector.java.analysis.TestUtil;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeAttributeSpecification;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.model.IGraph;

public class KafkaMixedTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.KAFKA_SCOPE)
                .addPlugin(new KafkaAnalysisPlugin())
                .addTargetEntryMethodSignature(KafkaMixedTarget.class, testData.getRunnerMethodName() + "()V")
                .addApplicationPackagePrefix("eu.profinit")
                .addStdlib()
                .addExclusions()
                .run()
                .generateVisualization()
                .getGraph();

        testData.validateResultGraph(iGraph);
    }

    @DataProvider
    private Object[][] testAnalyseTargetDataProvider() {
        return new Object[][] {
                {
                        new TestData(
                                "runKafkaProducerAndConsumer",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL, "Consumer#poll"),
                                            new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL, "Producer#send"));
                                })
                },
                };
    }
}
