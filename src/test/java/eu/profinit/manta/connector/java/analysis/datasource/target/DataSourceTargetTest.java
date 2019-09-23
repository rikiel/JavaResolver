package eu.profinit.manta.connector.java.analysis.datasource.target;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.TestData;
import eu.profinit.manta.connector.java.analysis.TestUtil;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeAttributeSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeComposedSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeNameSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeSpecification;
import eu.profinit.manta.connector.java.analysis.TestWrapper;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.datasource.DataSourceAnalysisPlugin;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

public class DataSourceTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.DATASOURCE_SCOPE)
                .addPlugin(new DataSourceAnalysisPlugin())
                .addTargetEntryMethodSignature(DataSourceTarget.class, testData.getRunnerMethodName() + "()V")
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
        final NodeSpecification connectionUrl = new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_URL, "ConnectionUrl");
        final NodeSpecification userName = new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_USER_NAME, "UserName");

        final NodeSpecification testWrapperStoreValString = new NodeNameSpecification(TestWrapper.class.getName(), "storeValString");

        return new Object[][] {
                {
                        new TestData(
                                "runApacheCommonsDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runTeraDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            userName,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runOracleDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runMsSqlDataSourceStandard",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runMsSqlDataSourcePooled",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runMsSqlDataSourceDistributed",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runPostgreSqlDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(connectionUrl, userName),
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runDb2SqlDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            userName,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runEmbeddedDataSource",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            new NodeComposedSpecification(
                                                    new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_URL, "jdbc:hsqldb:mem:DbName"),
                                                    new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_USER_NAME, "sa")),
                                            testWrapperStoreValString);
                                })
                },
                };
    }
}
