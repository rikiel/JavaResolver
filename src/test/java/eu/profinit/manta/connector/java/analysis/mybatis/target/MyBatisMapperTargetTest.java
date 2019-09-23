package eu.profinit.manta.connector.java.analysis.mybatis.target;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.ApplicationConfiguration;
import eu.profinit.manta.connector.java.analysis.TestData;
import eu.profinit.manta.connector.java.analysis.TestUtil;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeAttributeSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeNameSpecification;
import eu.profinit.manta.connector.java.analysis.TestUtil.NodeSpecification;
import eu.profinit.manta.connector.java.analysis.TestWrapper;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.datasource.DataSourceAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAttributes;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

public class MyBatisMapperTargetTest extends AbstractTest {
    @Test(dataProvider = "testAnalyseTargetDataProvider")
    public void testAnalyseTarget(TestData testData) {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.MY_BATIS_SCOPE)
                .addPlugin(new MyBatisAnalysisPlugin())
                .addTargetEntryMethodSignature(MyBatisMapperTarget.class, testData.getRunnerMethodName() + "()V")
                .addApplicationPackagePrefix("eu.profinit")
                .addStdlib()
                .addExclusions()
                .run()
                .generateVisualization()
                .getGraph();

        testData.validateResultGraph(iGraph);
    }

    @Test
    public void testMapperWithDataSource() {
        final IGraph iGraph = new ApplicationConfiguration()
                .addGraphOutputDir("target/img")
                .addJarFiles(WalaAnalysisTestUtils.TESTS_SCOPE)
                .addJarFiles(WalaAnalysisTestUtils.MY_BATIS_SCOPE)
                .addPlugin(new MyBatisAnalysisPlugin())
                .addPlugin(new DataSourceAnalysisPlugin())
                .addTargetEntryMethodSignature(MyBatisMapperTarget.class, "runSelectAllWithDataSource()V")
                .addApplicationPackagePrefix("eu.profinit")
                .addStdlib()
                .addExclusions()
                .run()
                .generateVisualization()
                .getGraph();

        TestUtil.assertFlowExistsInGraph(iGraph,
                new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_URL, "jdbc:oracle:thin:@//192.168.0.16:1521/orcl"),
                new NodeAttributeSpecification(Attributes.DATABASE_QUERY, "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME"));
        TestUtil.assertFlowExistsInGraph(iGraph,
                new NodeAttributeSpecification(Attributes.DATABASE_QUERY, "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME"),
                new NodeNameSpecification(TestWrapper.class.getName(), "storeValString"));
    }

    @DataProvider
    private Object[][] testAnalyseTargetDataProvider() {
        final NodeSpecification configuration = new NodeAttributeSpecification(Attributes.DATABASE_CONNECTION_URL,
                "jdbc:oracle:thin:@//192.168.0.16:1521/orcl");

        final NodeSpecification testWrapperStoreValString = new NodeNameSpecification(TestWrapper.class.getName(), "storeValString");
        final NodeSpecification testWrapperLoadValString = new NodeNameSpecification(TestWrapper.class.getName(), "loadValString");

        final NodeSpecification selectAll = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME");
        final NodeSpecification selectById = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_ID = 1");
        final NodeSpecification insert = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "INSERT INTO TABLE_NAME (TABLE_ID, TABLE_VALUE) VALUES (?, ?)");
        final NodeSpecification delete = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "DELETE FROM TABLE_NAME WHERE TABLE_ID = ?");
        final NodeSpecification update = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "UPDATE TABLE_NAME SET TABLE_VALUE = ?, TABLE_ID = ? WHERE TABLE_ID = ?");
        final NodeSpecification updateFromMap = new NodeAttributeSpecification(Attributes.DATABASE_QUERY,
                "UPDATE TABLE_NAME SET TABLE_VALUE = newValue WHERE TABLE_ID = ?");
        final NodeSpecification selectCall = new NodeAttributeSpecification(Attributes.DATABASE_QUERY, "CALL selectForMap");

        return new Object[][] {
                {
                        new TestData(
                                "runSelectAllAndDelete",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            configuration,
                                            Lists.newArrayList(selectAll, delete));
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            delete);
                                })
                },
                {
                        new TestData(
                                "runSelectAll",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            configuration,
                                            selectAll);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runSelectById",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            configuration,
                                            selectById);
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectById,
                                            testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runInsert",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(
                                                    configuration,
                                                    testWrapperLoadValString),
                                            insert);
                                })
                },
                {
                        new TestData(
                                "runDelete",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(
                                                    configuration,
                                                    testWrapperLoadValString),
                                            delete);
                                })
                },
                {
                        new TestData(
                                "runUpdate",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            Lists.newArrayList(
                                                    configuration,
                                                    testWrapperLoadValString),
                                            update);
                                })
                },
                {
                        new TestData(
                                "runFullTest",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            configuration,
                                            Lists.newArrayList(selectAll, delete, update, insert));
                                    TestUtil.assertFlowExistsInGraph(iGraph,
                                            selectAll,
                                            Lists.newArrayList(
                                                    delete,
                                                    update,
                                                    insert
                                            ));
                                })
                },
                {
                        new TestData(
                                "runUpdateValuesFromMap",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph, configuration, updateFromMap);
                                })
                },
                {
                        new TestData(
                                "runSelectForMap",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph, configuration, selectCall);
                                    final NodeSpecification mapResult = new NodeAttributeSpecification(MyBatisAttributes.DATASOURCE_MAP_KEY, "result");
                                    TestUtil.assertFlowExistsInGraph(iGraph, selectCall, mapResult);
                                    TestUtil.assertFlowExistsInGraph(iGraph, mapResult, testWrapperStoreValString);
                                })
                },
                {
                        new TestData(
                                "runSelectForList",
                                iGraph -> {
                                    TestUtil.assertFlowExistsInGraph(iGraph, configuration, selectCall);
                                    final NodeSpecification mapResult = new NodeAttributeSpecification(MyBatisAttributes.DATASOURCE_MAP_KEY, "result");
                                    TestUtil.assertFlowExistsInGraph(iGraph, selectCall, mapResult);
                                    TestUtil.assertFlowExistsInGraph(iGraph, mapResult, testWrapperStoreValString);
                                })
                },
                };
    }
}
