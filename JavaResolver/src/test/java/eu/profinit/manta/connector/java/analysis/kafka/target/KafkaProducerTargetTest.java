package eu.profinit.manta.connector.java.analysis.kafka.target;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.TestData;
import eu.profinit.manta.connector.java.analysis.TestUtil;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeAttributeSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeNameSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeSpecification;
import eu.profinit.manta.connector.java.analysis.TestWrapper;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.model.IGraph;

public class KafkaProducerTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.KAFKA_SCOPE)
                .addPlugin(new KafkaAnalysisPlugin())
                .addTargetEntryMethodSignature(KafkaProducerTarget.class, testData.getRunnerMethodName() + "()V")
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
        final NodeSpecification serverName = new NodeAttributeSpecification(KafkaAttributes.SERVER, "ServerName");

        final NodeSpecification message1 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message1]", AbstractKafkaTarget.PRODUCER_TOPIC);
        final NodeSpecification message2 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message2]", AbstractKafkaTarget.PRODUCER_TOPIC);
        final NodeSpecification message3 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message3]", AbstractKafkaTarget.PRODUCER_TOPIC);
        final NodeSpecification message4 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message4]", AbstractKafkaTarget.PRODUCER_TOPIC);
        final NodeSpecification message5 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message5]", AbstractKafkaTarget.PRODUCER_TOPIC);
        final NodeSpecification message6 = new NodeAttributeSpecification(KafkaAttributes.BACKEND_CALL,
                "Producer#send", "value=[Message6]", AbstractKafkaTarget.PRODUCER_TOPIC);

        return new Object[][] {
                {
                        new TestData(
                                "runKafkaProducer",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Arrays.asList(message1, message2, message3, message4, message5, message6),
                                            serverName);
                                })
                },
                {
                        new TestData(
                                "runKafkaProducerWithPropertiesFromFile",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            message1,
                                            serverName);
                                })
                },
                };
    }
}
