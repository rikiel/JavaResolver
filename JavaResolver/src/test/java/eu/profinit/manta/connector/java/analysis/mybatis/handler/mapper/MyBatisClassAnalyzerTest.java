package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesAnnotatedMappers;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesXmlMappers;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.multiid.MyBatisMultiIdAnnotatedMappers;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.multiid.MyBatisMultiIdXmlMappers;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdXmlMappers;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MyBatisClassAnalyzerTest extends AbstractTest {
    private static final MyBatisClassAnalyzer MY_BATIS_CLASS_ANALYZER = new MyBatisClassAnalyzer(new FileContentReader());

    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final List<Class<?>> classesToAnalyze = Lists.newArrayList();
        for (Object[][] providers : Arrays.asList(
                testAnalyseWithSingleIdDataProvider(),
                testAnalyseWithMultiIdDataProvider(),
                testAnalyseComplexFeaturesDataProvider())) {
            for (Object[] data : providers) {
                classesToAnalyze.add((Class<?>) data[0]);
            }
        }

        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classesToAnalyze, WalaAnalysisTestUtils.MY_BATIS_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
    }

    @Test(dataProvider = "testAnalyseWithSingleIdDataProvider")
    public void testAnalyseWithSingleId(Class<?> testedInterface, SqlCommand expectedCommand) {
        final IClass testedClass = classMethodCache.findClass(new ClassWrapperImpl(testedInterface));
        expectedCommand.setMethod(Iterables.getOnlyElement(testedClass.getDeclaredMethods()));

        final List<SqlCommand> sqlCommands = MY_BATIS_CLASS_ANALYZER.analyse(testedClass);

        assertReflectionEquals(Lists.newArrayList(expectedCommand), sqlCommands);
    }

    @Test(dataProvider = "testAnalyseWithMultiIdDataProvider")
    public void testAnalyseWithMultiId(Class<?> testedInterface, SqlCommand expectedCommand) {
        final IClass testedClass = classMethodCache.findClass(new ClassWrapperImpl(testedInterface));
        expectedCommand.setMethod(Iterables.getOnlyElement(testedClass.getDeclaredMethods()));

        final List<SqlCommand> sqlCommands = MY_BATIS_CLASS_ANALYZER.analyse(testedClass);

        assertReflectionEquals(Lists.newArrayList(expectedCommand), sqlCommands);
    }

    @Test(dataProvider = "testAnalyseComplexFeaturesDataProvider")
    public void testAnalyseComplexFeatures(Class<?> testedInterface, SqlCommand expectedCommand) {
        final IClass testedClass = classMethodCache.findClass(new ClassWrapperImpl(testedInterface));
        expectedCommand.setMethod(Iterables.getOnlyElement(testedClass.getDeclaredMethods()));

        final List<SqlCommand> sqlCommands = MY_BATIS_CLASS_ANALYZER.analyse(testedClass);

        assertReflectionEquals(Lists.newArrayList(expectedCommand), sqlCommands);
    }

    @DataProvider
    private static Object[][] testAnalyseWithSingleIdDataProvider() {
        return new ExpectedSqlCommandProvider()
                .addSingleIdSelectAll(MyBatisSingleIdXmlMappers.SelectAllXmlMapper.class)
                .addSingleIdSelectById(MyBatisSingleIdXmlMappers.SelectByIdXmlMapper.class)
                .addSingleIdSelectByIdWithConstructor(MyBatisSingleIdXmlMappers.SelectByIdWithConstructorArgsXmlMapper.class)
                .addSingleIdInsert(MyBatisSingleIdXmlMappers.InsertXmlMapper.class)
                .addSingleIdUpdate(MyBatisSingleIdXmlMappers.UpdateXmlMapper.class)
                .addSingleIdDelete(MyBatisSingleIdXmlMappers.DeleteXmlMapper.class)

                .addSingleIdSelectAll(MyBatisSingleIdAnnotatedMappers.SelectAllAnnotatedMapper.class)
                .addSingleIdSelectById(MyBatisSingleIdAnnotatedMappers.SelectByIdAnnotatedMapper.class)
                .addSingleIdSelectByIdWithConstructor(MyBatisSingleIdAnnotatedMappers.SelectByIdWithConstructorArgsAnnotatedMapper.class)
                .addSingleIdInsert(MyBatisSingleIdAnnotatedMappers.InsertAnnotatedMapper.class)
                .addSingleIdUpdate(MyBatisSingleIdAnnotatedMappers.UpdateAnnotatedMapper.class)
                .addSingleIdDelete(MyBatisSingleIdAnnotatedMappers.DeleteAnnotatedMapper.class)

                .build();
    }

    @DataProvider
    private static Object[][] testAnalyseWithMultiIdDataProvider() {
        return new ExpectedSqlCommandProvider()
                .addMultiIdSelectAll(MyBatisMultiIdXmlMappers.SelectAllXmlMapper.class)
                .addMultiIdSelectById(MyBatisMultiIdXmlMappers.SelectByIdXmlMapper.class)
                .addMultiIdInsert(MyBatisMultiIdXmlMappers.InsertXmlMapper.class)
                .addMultiIdUpdate(MyBatisMultiIdXmlMappers.UpdateXmlMapper.class)
                .addMultiIdDelete(MyBatisMultiIdXmlMappers.DeleteXmlMapper.class)

                .addMultiIdSelectAll(MyBatisMultiIdAnnotatedMappers.SelectAllAnnotatedMapper.class)
                .addMultiIdSelectById(MyBatisMultiIdAnnotatedMappers.SelectByIdAnnotatedMapper.class)
                .addMultiIdInsert(MyBatisMultiIdAnnotatedMappers.InsertAnnotatedMapper.class)
                .addMultiIdUpdate(MyBatisMultiIdAnnotatedMappers.UpdateAnnotatedMapper.class)
                .addMultiIdDelete(MyBatisMultiIdAnnotatedMappers.DeleteAnnotatedMapper.class)

                .build();
    }

    @DataProvider
    private static Object[][] testAnalyseComplexFeaturesDataProvider() {
        return new ExpectedSqlCommandProvider()
                .addSingleIdCallForMap(MyBatisComplexFeaturesXmlMappers.SelectToMapMapper.class)
                .addSingleIdCallForList(MyBatisComplexFeaturesXmlMappers.SelectToListMapper.class)

                .addSingleIdSelectAll(MyBatisComplexFeaturesXmlMappers.SelectAllWithCommentsXmlMapper.class)
                .addSingleIdSelectAll(MyBatisComplexFeaturesXmlMappers.SelectAllIncludedXmlMapper.class)
                .addSingleIdSelectAllLike(MyBatisComplexFeaturesXmlMappers.SelectAllIfXmlMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesXmlMappers.SelectAllForeachXmlMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesXmlMappers.SelectAllForeachDefaultValuesXmlMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesXmlMappers.SelectAllChooseXmlMapper.class)
                .addSingleIdSelectAllWhere(MyBatisComplexFeaturesXmlMappers.SelectAllWhereXmlMapper.class)
                .addSingleIdSelectAllWhere(MyBatisComplexFeaturesXmlMappers.SelectAllWhereTrimXmlMapper.class)
                .addSingleIdUpdateSet(MyBatisComplexFeaturesXmlMappers.UpdateWithSetXmlMapper.class)
                .addSingleIdUpdateSet(MyBatisComplexFeaturesXmlMappers.UpdateWithSetTrimXmlMapper.class)

                .addSingleIdSelectAll(MyBatisComplexFeaturesAnnotatedMappers.SelectAllWithCommentsScriptMapper.class)
                .addSingleIdSelectAllLike(MyBatisComplexFeaturesAnnotatedMappers.SelectAllIfScriptMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesAnnotatedMappers.SelectAllForeachScriptMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesAnnotatedMappers.SelectAllForeachDefaultValuesScriptMapper.class)
                .adddSingleIdSelectAllForeach(MyBatisComplexFeaturesAnnotatedMappers.SelectAllChooseScriptMapper.class)
                .addSingleIdSelectAllWhere(MyBatisComplexFeaturesAnnotatedMappers.SelectAllWhereScriptMapper.class)
                .addSingleIdSelectAllWhere(MyBatisComplexFeaturesAnnotatedMappers.SelectAllWhereTrimScriptMapper.class)
                .addSingleIdUpdateSet(MyBatisComplexFeaturesAnnotatedMappers.UpdateWithSetScriptMapper.class)
                .addSingleIdUpdateSet(MyBatisComplexFeaturesAnnotatedMappers.UpdateWithSetTrimScriptMapper.class)

                .build();
    }
}