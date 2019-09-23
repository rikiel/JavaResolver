package eu.profinit.manta.connector.java.analysis.jdbctemplate.target;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
import eu.profinit.manta.connector.java.analysis.jdbctemplate.JdbcTemplateAnalysisPlugin;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

public class JdbcTemplateBasicTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.JDBC_TEMPLATE_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.DATASOURCE_SCOPE)
                .addPlugin(new JdbcTemplateAnalysisPlugin())
                .addPlugin(new DataSourceAnalysisPlugin())
                .addTargetEntryMethodSignature(JdbcTemplateBasicTarget.class, testData.getRunnerMethodName() + "()V")
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
        final NodeSpecification databaseConnection = new NodeComposedSpecification(
                new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_USER_NAME, "java_martin"),
                new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_URL, "jdbc:oracle:thin:@//192.168.0.16:1521/orcl"));
        final NodeSpecification selectAll = new NodeAttributeSpecification(Attributes.DATABASE_QUERY, JdbcTemplateQueryConstants.SELECT_ALL);
        final NodeSpecification callProcedure = new NodeAttributeSpecification(Attributes.DATABASE_QUERY, JdbcTemplateQueryConstants.PROCEDURE_CALL);
        final NodeSpecification testWrapperStoreValString = new NodeNameSpecification(TestWrapper.class.getName(), "storeValString");

        return new Object[][] {
                {
                        new TestData(
                                "runHandleExecuteWithConnectionCallback",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            databaseConnection,
                                            selectAll);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runHandleExecuteWithStatementCallback",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            databaseConnection,
                                            selectAll);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runHandleExecuteWithPreparedStatementCreatorAndPreparedStatementCallback",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            databaseConnection,
                                            selectAll);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runHandleExecuteWithCallableStatementCreatorAndCallableStatementCallback",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            databaseConnection,
                                            callProcedure);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            callProcedure,
                                            testWrapperStoreValString);
                                })
                },
                };
    }
}
