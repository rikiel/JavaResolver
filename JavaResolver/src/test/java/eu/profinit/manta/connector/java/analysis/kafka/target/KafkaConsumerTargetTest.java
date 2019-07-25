package eu.profinit.manta.connector.java.analysis.kafka.target;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.TestData;
import eu.profinit.manta.connector.java.analysis.TestUtil;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeAttributeSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeSpecification;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

public class KafkaConsumerTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.KAFKA_SCOPE)
                .addPlugin(new KafkaAnalysisPlugin())
                .addTargetEntryMethodSignature(KafkaConsumerTarget.class, testData.getRunnerMethodName() + "()V")
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

        final NodeSpecification subscribedTopic = new NodeAttributeSpecification(KafkaAttributes.TOPIC, AbstractKafkaTarget.CONSUMER_TOPIC);

        return new Object[][] {
                {
                        new TestData(
                                "runKafkaConsumer",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            subscribedTopic,
                                            serverName);
                                })
                },
                {
                        new TestData(
                                "runKafkaConsumer1",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            subscribedTopic,
                                            serverName);
                                })
                },
                {
                        new TestData(
                                "runKafkaConsumerAssign",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            subscribedTopic,
                                            serverName);
                                })
                },
                };
    }
}
