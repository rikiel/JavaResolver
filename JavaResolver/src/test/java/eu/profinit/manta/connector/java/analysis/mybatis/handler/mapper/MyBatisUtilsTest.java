package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.ibatis.annotations.Param;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils.MyBatisSqlVariable;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

import static org.testng.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MyBatisUtilsTest extends AbstractTest {
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(Lists.newArrayList(TestedClass.class), null);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
    }

    @Test(dataProvider = "testGetArgumentsFromSqlDataProvider")
    public void testGetArgumentsFromSql(String sql, List<MyBatisSqlVariable> expectedResult) {
        final List<MyBatisSqlVariable> result = MyBatisUtils.getArgumentsFromSql(sql);
        assertReflectionEquals(expectedResult, result);
    }

    @Test(dataProvider = "testGetArgumentIndexDataProvider")
    public void testGetArgumentIndex(String parameterName, IMethod iMethod, int expectedIndex) {
        final int index = MyBatisUtils.getArgumentIndex(parameterName, iMethod);
        assertEquals(index, expectedIndex);
    }

    @Test(dataProvider = "testGetArgumentIndexErrorDataProvider", expectedExceptions = RuntimeException.class)
    public void testGetArgumentIndexError(String parameterName, IMethod iMethod) {
        MyBatisUtils.getArgumentIndex(parameterName, iMethod);
    }

    @Test(dataProvider = "testGetSqlWithSubstitutedArgumentsDataProvider")
    public void testGetSqlWithSubstitutedArguments(String expectedSql, String sql, IMethod iMethod, List<ObjectAttributesInput> attributes) {
        final String actualSql = MyBatisUtils.getSqlWithSubstitutedArguments(sql, iMethod, attributes);
        assertEquals(actualSql, expectedSql);
    }

    @DataProvider
    private Object[][] testGetArgumentsFromSqlDataProvider() {
        return new Object[][] {
                {
                        "",
                        Lists.newArrayList()
                }, {
                        "SELECT * FROM TABLE",
                        Lists.newArrayList()
                }, {
                        "TEXT #{arg.A.B.C} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C}", "arg.A.B.C", null, null))
                }, {
                        "TEXT #{arg.A.B.C, someProperty=XYZ} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, someProperty=XYZ}", "arg.A.B.C", null, null))
                },
                // test with result map
                {
                        "TEXT #{arg.A.B.C, someProperty=XYZ, resultMap=map} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, someProperty=XYZ, resultMap=map}", "arg.A.B.C", null, "map"))
                }, {
                        "TEXT #{arg.A.B.C, resultMap='map'} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, resultMap='map'}", "arg.A.B.C", null, "map"))
                }, {
                        "TEXT #{arg.A.B.C, resultMap =\t'map'} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, resultMap =\t'map'}", "arg.A.B.C", null, "map"))
                }, {
                        "TEXT #{arg.A.B.C, resultMap=\"map\"} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, resultMap=\"map\"}", "arg.A.B.C", null, "map"))
                }, {
                        "TEXT #{arg.A.B.C, resultMap=map, someProperty=XYZ} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, resultMap=map, someProperty=XYZ}", "arg.A.B.C", null, "map"))
                },
                // test multiple arguments
                {
                        "TEXT #{arg.A.B.C} #{arg.B.C.D}",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C}", "arg.A.B.C", null, null),
                                new MyBatisSqlVariable("#{arg.B.C.D}", "arg.B.C.D", null, null))
                }, {
                        "TEXT #{arg.A.B.C, resultMap=map} TEXT #{arg.B.C.D, resultMap=map2, someProperty=XYZ} TEXT",
                        Lists.newArrayList(
                                new MyBatisSqlVariable("#{arg.A.B.C, resultMap=map}", "arg.A.B.C", null, "map"),
                                new MyBatisSqlVariable("#{arg.B.C.D, resultMap=map2, someProperty=XYZ}", "arg.B.C.D", null, "map2"))
                },
                // test mode
                {
                        "TEXT #{arg.A.B.C, mode=IN}",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, mode=IN}", "arg.A.B.C", "IN", null))
                }, {
                        "TEXT #{arg.A.B.C, mode = 'OUT'}",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, mode = 'OUT'}", "arg.A.B.C", "OUT", null))
                }, {
                        "TEXT #{arg.A.B.C, mode=INOUT, someProperty=XYZ}",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, mode=INOUT, someProperty=XYZ}", "arg.A.B.C", "INOUT", null))
                },
                // test mode + resultMap
                {
                        "TEXT #{arg.A.B.C, mode=INOUT, resultMap=map} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, mode=INOUT, resultMap=map}", "arg.A.B.C", "INOUT", "map"))
                }, {
                        "TEXT #{arg.A.B.C, resultMap=map, mode=INOUT} TEXT",
                        Lists.newArrayList(new MyBatisSqlVariable("#{arg.A.B.C, resultMap=map, mode=INOUT}", "arg.A.B.C", "INOUT", "map"))
                },
                };
    }

    @DataProvider
    private Object[][] testGetArgumentIndexDataProvider() {
        final ClassWrapperImpl callerClass = new ClassWrapperImpl(TestedClass.class);
        final ClassWrapperImpl resultClass = new ClassWrapperImpl(void.class);
        final ClassWrapperImpl argumentClass = new ClassWrapperImpl(Model.class);

        final IMethod method1 = WalaUtils.findMethod(classMethodCache, callerClass, "method1", resultClass, argumentClass);
        final IMethod method2 = WalaUtils.findMethod(classMethodCache, callerClass, "method2", resultClass, argumentClass, argumentClass);
        return new Object[][] {
                { "notUsedName", method1, 0 },

                { "arg0", method2, 0 },
                { "arg1", method2, 1 },
                { "arg1.value", method2, 1 },

                { "param1", method2, 0 },
                { "param2", method2, 1 },
                { "param2.value", method2, 1 },

                { "secondParameterName", method2, 1 },
                { "secondParameterName.value", method2, 1 },
                };
    }

    @DataProvider
    private Object[][] testGetArgumentIndexErrorDataProvider() {
        final ClassWrapperImpl callerClass = new ClassWrapperImpl(TestedClass.class);
        final ClassWrapperImpl resultClass = new ClassWrapperImpl(void.class);
        final ClassWrapperImpl argumentClass = new ClassWrapperImpl(Model.class);

        final IMethod method2 = WalaUtils.findMethod(classMethodCache, callerClass, "method2", resultClass, argumentClass, argumentClass);
        return new Object[][] {
                { "arg", method2 },
                { "param", method2 },
                { "paramXXX", method2 },
                { "arg200", method2 },
                { "param200", method2 },
                { "notexistedName", method2 },
                };
    }

    @DataProvider
    private Object[][] testGetSqlWithSubstitutedArgumentsDataProvider() {
        final IMethod method2 = WalaUtils.findMethod(classMethodCache,
                new ClassWrapperImpl(TestedClass.class),
                "method2",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Model.class),
                new ClassWrapperImpl(Model.class));
        final IMethod method3 = WalaUtils.findMethod(classMethodCache,
                new ClassWrapperImpl(TestedClass.class),
                "method3",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Map.class));

        return new Object[][] {
                // single substitution
                {
                        "TEXT VALUE TEXT", "TEXT #{arg0.value} TEXT", method2,
                        Lists.newArrayList(createConstantAttribute(String.class, "VALUE"))
                },
                {
                        "TEXT 123.0 TEXT", "TEXT #{arg0.value} TEXT", method2,
                        Lists.newArrayList(createConstantAttribute(double.class, 123.))
                },
                {
                        "TEXT ? TEXT", "TEXT #{arg0.value} TEXT", method2,
                        Lists.newArrayList(createConstantAttribute(int.class, 123, 234))
                },
                // multiple substitutions
                {
                        "TEXT VALUE TEXT ? TEXT", "TEXT #{arg0.value} TEXT #{arg1.value} TEXT",
                        method2,
                        Lists.newArrayList(createConstantAttribute(String.class, "VALUE"), createConstantAttribute(String.class, "V2", "V3"))
                },
                {
                        "TEXT ? TEXT VALUE TEXT", "TEXT #{arg0.value} TEXT #{arg1.value} TEXT",
                        method2,
                        Lists.newArrayList(createConstantAttribute(String.class, "V1", "V2"), createConstantAttribute(String.class, "VALUE"))
                },
                // map substitution
                {
                        "TEXT ? TEXT", "TEXT #{arg0.value} TEXT",
                        method3,
                        Lists.newArrayList(createMapAttribute(ImmutableMap.of("value", ImmutableSet.of())))
                },
                {
                        "TEXT ? TEXT", "TEXT #{arg0.value} TEXT",
                        method3,
                        Lists.newArrayList(createMapAttribute(ImmutableMap.of("value", ImmutableSet.of("V1", "V2"))))
                },
                {
                        "TEXT VALUE TEXT", "TEXT #{arg0.value} TEXT",
                        method3,
                        Lists.newArrayList(createMapAttribute(ImmutableMap.of("value", ImmutableSet.of("VALUE"))))
                },
                };
    }

    @Nonnull
    private ObjectAttributesInput createMapAttribute(Map<String, Set<String>> map) {
        return createAttribute(new ClassWrapperImpl(Map.class), Attributes.MAP_KEYS_TO_VALUES, map);
    }

    @Nonnull
    private ObjectAttributesInput createConstantAttribute(Class<?> argumentType, Object... values) {
        return createAttribute(new ClassWrapperImpl(argumentType), Attributes.CONSTANT_VALUE, values);
    }

    @Nonnull
    private ObjectAttributesInput createAttribute(ClassWrapper clazz, IAttributeName attributeName, Object... values) {
        final ObjectAttributesInput attributes = new ObjectAttributesInput(classMethodCache.findClass(clazz), ImmutableMap.of());
        attributes.addAll(attributeName, Sets.newHashSet(values));
        return attributes;
    }

    public interface TestedClass {
        void method1(Model argument1);

        void method2(Model argument1, @Param("secondParameterName") Model argument2);

        void method3(Map<String, String> argument1);
    }

    public static class Model {
        private String value;
    }
}