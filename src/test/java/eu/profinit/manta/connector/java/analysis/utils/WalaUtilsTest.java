package eu.profinit.manta.connector.java.analysis.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils.PrimitiveType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class WalaUtilsTest extends AbstractTest {
    @Test(dataProvider = "testGetAllInterfaceMethodsDataProvider")
    public void testGetAllInterfaceMethods(List<Class<?>> classes, Map<TypeName, Set<String>> expectedMethods) {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classes, null);
        final Map<TypeName, Set<String>> actualMethods = Lists.newArrayList(classHierarchy.getLoader(ClassLoaderReference.Application).iterateAllClasses())
                .stream()
                .collect(Collectors.toMap(
                        IClass::getName,
                        iClass -> WalaUtils.getAllInterfaceMethods(iClass).stream()
                                .map(method -> method.getName().toString())
                                .collect(Collectors.toSet())));
        assertReflectionEquals(expectedMethods, actualMethods);
    }

    @Test(dataProvider = "testGetAllSuperClassesDataProvider")
    public void testGetAllSuperClasses(Class<?> testedClass, List<Class<?>> classesToAnalyze, List<Class<?>> expectedSuperClasses) {
        final IClass testedIClass = WalaAnalysisTestUtils.constructClassHierarchyAndGetApplicationClass(testedClass, classesToAnalyze);
        final List<Class<?>> superClasses = WalaUtils.getAllSuperClasses(testedIClass).stream()
                .map(iClass -> {
                    try {
                        return Class.forName(new ClassWrapperImpl(iClass.getName()).getJavaClassName());
                    } catch (ClassNotFoundException e) {
                        return Validate.fail(e);
                    }
                })
                .collect(Collectors.toList());
        assertReflectionEquals(expectedSuperClasses, superClasses);
    }

    @Test(dataProvider = "testGetAllInstanceFieldsDataProvider")
    public void testGetAllInstanceFields(Class<?> testedClass, List<Class<?>> classesToAnalyze, List<String> expectedFieldNames) {
        final IClass testedIClass = WalaAnalysisTestUtils.constructClassHierarchyAndGetApplicationClass(testedClass, classesToAnalyze);
        final List<String> fieldNames = WalaUtils.getAllInstanceFields(testedIClass).stream()
                .map(iField -> iField.getReference().getName().toString())
                .collect(Collectors.toList());
        assertReflectionEquals(expectedFieldNames, fieldNames);
    }

    @Test(dataProvider = "testGetArgumentsDataProvider")
    public void testGetArguments(IMethod iMethod, List<Class<?>> expectedArgumentsTypes) {
        final List<String> argumentsTypeNames = WalaUtils.getArguments(iMethod).stream()
                .map(ClassWrapper::getJavaClassName)
                .collect(Collectors.toList());
        final List<String> expectedArgumentsTypeNames = expectedArgumentsTypes.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
        assertReflectionEquals(expectedArgumentsTypeNames, argumentsTypeNames);
    }

    @Test
    public void testFindParentMethod() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(Lists.newArrayList(FindMethodParentInterface.class), null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);

        final ClassWrapperImpl parentInterface = new ClassWrapperImpl(FindMethodParentInterface.class);
        final IMethod expectedMethod = classMethodCache.getAllMethods().stream()
                .filter(iMethod -> WalaUtils.isSupertype(iMethod.getDeclaringClass(), parentInterface))
                .filter(iMethod -> WalaUtils.isNamed(iMethod, "method1"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find method in cache"));

        assertSame(
                WalaUtils.findMethod(classMethodCache,
                        parentInterface,
                        "method1",
                        PrimitiveType.LONG.getPrimitiveClass(),
                        PrimitiveType.BOOLEAN.getObjectClass()),
                expectedMethod);

        assertSame(
                WalaUtils.findMethod(classMethodCache,
                        parentInterface,
                        "method1",
                        PrimitiveType.LONG.getObjectClass(),
                        PrimitiveType.BOOLEAN.getPrimitiveClass()),
                expectedMethod);
    }

    @Test
    public void testFindChildMethod() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(
                Lists.newArrayList(FindMethodParentInterface.class, FindMethodChildClass.class), null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);

        final ClassWrapperImpl childInterface = new ClassWrapperImpl(FindMethodChildClass.class);
        final Set<IMethod> expectedMethods = classMethodCache.getAllMethods().stream()
                .filter(iMethod -> WalaUtils.isSupertype(iMethod.getDeclaringClass(), childInterface))
                .filter(iMethod -> WalaUtils.isNamed(iMethod, "method2"))
                .collect(Collectors.toSet());

        final IMethod method2Parent = WalaUtils.findMethod(classMethodCache,
                childInterface,
                "method2",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Object.class),
                PrimitiveType.INTEGER.getPrimitiveClass());
        final IMethod method2ChildOverride = WalaUtils.findMethod(classMethodCache,
                childInterface,
                "method2",
                PrimitiveType.LONG.getPrimitiveClass(),
                new ClassWrapperImpl(Object.class),
                PrimitiveType.INTEGER.getObjectClass());
        final IMethod method2ChildPublic = WalaUtils.findMethod(classMethodCache,
                childInterface,
                "method2",
                PrimitiveType.LONG.getPrimitiveClass(),
                new ClassWrapperImpl(String.class),
                PrimitiveType.INTEGER.getObjectClass());

        assertEquals(Sets.newHashSet(method2Parent, method2ChildOverride, method2ChildPublic), expectedMethods);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Failed to found one matching method for pattern.*")
    public void testFindChildMethodError() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(
                Lists.newArrayList(FindMethodParentInterface.class, FindMethodChildClass.class), null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);

        WalaUtils.findMethod(classMethodCache,
                new ClassWrapperImpl(FindMethodChildClass.class),
                "method1",
                PrimitiveType.LONG.getPrimitiveClass(),
                PrimitiveType.BOOLEAN.getObjectClass());
    }

    @Test
    public void testIsMatchingArguments() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(
                Lists.newArrayList(FindMethodParentInterface.class, FindMethodChildClass.class), null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);

        final IMethod method2Object = WalaUtils.findMethod(classMethodCache,
                new ClassWrapperImpl(FindMethodParentInterface.class),
                "method2",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Object.class),
                PrimitiveType.INTEGER.getPrimitiveClass());

        assertTrue(WalaUtils.isMatchingArguments(method2Object, new ClassWrapperImpl(Object.class), PrimitiveType.INTEGER.getPrimitiveClass()));
        assertFalse(WalaUtils.isMatchingArguments(method2Object, new ClassWrapperImpl(Object.class)));
        assertFalse(WalaUtils.isMatchingArguments(method2Object, new ClassWrapperImpl(String.class), PrimitiveType.INTEGER.getPrimitiveClass()));
    }

    @DataProvider
    private static Object[][] testGetAllInterfaceMethodsDataProvider() {
        return new Object[][] {
                {
                        Lists.newArrayList(ParentInterface.class),
                        ImmutableMap.of(new ClassWrapperImpl(ParentInterface.class).getWalaTypeName(), ImmutableSet.of("method"))
                },
                {
                        Lists.newArrayList(ParentInterface.class, ChildInterface1.class),
                        ImmutableMap.of(new ClassWrapperImpl(ParentInterface.class).getWalaTypeName(),
                                ImmutableSet.of("method"),
                                new ClassWrapperImpl(ChildInterface1.class).getWalaTypeName(),
                                ImmutableSet.of("method"))
                },
                {
                        Lists.newArrayList(ParentInterface.class, ChildInterface2.class),
                        ImmutableMap.of(new ClassWrapperImpl(ParentInterface.class).getWalaTypeName(),
                                ImmutableSet.of("method"),
                                new ClassWrapperImpl(ChildInterface2.class).getWalaTypeName(),
                                ImmutableSet.of("method", "method2"))
                },
                };
    }

    @DataProvider
    private static Object[][] testGetAllSuperClassesDataProvider() {
        return new Object[][] {
                {
                        ParentInterface.class,
                        Lists.newArrayList(ParentInterface.class),
                        Lists.newArrayList(ParentInterface.class, Object.class)
                },
                {
                        ParentClass.class,
                        Lists.newArrayList(ParentClass.class, ParentInterface.class),
                        Lists.newArrayList(ParentClass.class, Object.class, ParentInterface.class)
                },
                {
                        ChildClass1.class,
                        Lists.newArrayList(ChildClass1.class, ParentClass.class, ChildInterface1.class, ParentInterface.class),
                        Lists.newArrayList(ChildClass1.class, ParentClass.class, Object.class, ChildInterface1.class, ParentInterface.class)
                },
                };
    }

    @DataProvider
    private static Object[][] testGetAllInstanceFieldsDataProvider() {
        return new Object[][] {
                {
                        ParentInterface.class,
                        Lists.newArrayList(ParentInterface.class),
                        Lists.newArrayList()
                },
                {
                        ParentClass.class,
                        Lists.newArrayList(ParentClass.class, ParentInterface.class),
                        Lists.newArrayList("b", "c")
                },
                {
                        ChildClass1.class,
                        Lists.newArrayList(ParentClass.class, ParentInterface.class, ChildClass1.class, ChildInterface1.class),
                        Lists.newArrayList("b", "d", "b", "c")
                },
                };
    }

    @DataProvider
    private static Object[][] testGetArgumentsDataProvider() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(Lists.newArrayList(ClassForMethodArgumentsTest.class), null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);

        return new Object[][] {
                {
                        WalaUtils.findMethod(classMethodCache,
                                new ClassWrapperImpl(ClassForMethodArgumentsTest.class),
                                "method",
                                new ClassWrapperImpl(void.class),
                                new ClassWrapperImpl(String.class)),
                        Lists.newArrayList(String.class)
                },
                {
                        WalaUtils.findMethod(classMethodCache,
                                new ClassWrapperImpl(ClassForMethodArgumentsTest.class),
                                "staticMethod",
                                new ClassWrapperImpl(void.class),
                                new ClassWrapperImpl(String.class),
                                new ClassWrapperImpl(Integer.class)),
                        Lists.newArrayList(String.class, Integer.class)
                },
                };
    }

    private interface ParentInterface {
        void method();
    }

    private interface ChildInterface1 extends ParentInterface {
        int X = 0;

        @Override
        void method();
    }

    private interface ChildInterface2 extends ParentInterface {
        void method2();
    }

    private interface FindMethodParentInterface {
        long method1(Boolean value);

        Object method2(Object value1, int value2);
    }

    private class FindMethodChildClass implements FindMethodParentInterface {
        @Override
        public long method1(Boolean value) {
            return 0;
        }

        @Override
        public Long method2(Object value1, int value2) {
            return 0L;
        }

        /* Not override */
        public long method1(boolean value) {
            return 0;
        }

        /* Not override */
        public Long method2(String value1, int value2) {
            return 0L;
        }
    }

    private static class ClassForMethodArgumentsTest {
        public static void staticMethod(String value, Integer i) {
        }

        private void method() {
        }

        private void method(String value) {
        }
    }

    private static abstract class ParentClass implements ParentInterface {
        static int a;
        public final int b = 1;
        protected int c;
    }

    private static class ChildClass1 extends ParentClass implements ChildInterface1 {
        public int b;
        private int d;

        @Override
        public void method() {
        }
    }
}