package eu.profinit.manta.connector.java.analysis.utils;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils.PrimitiveType;

public class ClassMethodCacheTest extends AbstractTest {
    @Test
    public void testFindPrimitives() {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);
        for (PrimitiveType primitiveType : PrimitiveType.values()) {
            final IClass objectClass = classMethodCache.findClass(primitiveType.getObjectClass());
            final IClass primitiveClass = classMethodCache.findClass(primitiveType.getPrimitiveClass());
            log.trace("Primitive {} is present in cache as classes {} and {}", primitiveType, objectClass, primitiveClass);
        }
    }

    @Test(dataProvider = "testFindClassDataProvider")
    public void testFindClass(List<Class<?>> classesToAnalyse) {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classesToAnalyse, null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);
        for (Class<?> expectedClass : classesToAnalyse) {
            final IClass iClass = classMethodCache.findClass(new ClassWrapperImpl(expectedClass));
            log.trace("Class {} is present in cache as iClass {}", expectedClass, iClass);
        }
    }

    @Test(dataProvider = "testFindClassErrorDataProvider")
    public void testFindClassError(List<Class<?>> classesToAnalyse, List<Class<?>> missingClasses) {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classesToAnalyse, null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);
        for (Class<?> expectedClass : missingClasses) {
            try {
                final IClass iClass = classMethodCache.findClass(new ClassWrapperImpl(expectedClass));
                Assert.fail("Class should not be present in cache! " + iClass);
            } catch (Exception e) {
                log.trace("As expected! Failed to find class {} in cache", expectedClass);
            }
        }
    }

    @Test(dataProvider = "testFindMethodDataProvider")
    public void testFindMethod(List<Class<?>> classesToAnalyse, Class<?> testedClass, List<String> methodNames) {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classesToAnalyse, null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);
        for (String method : methodNames) {
            final IMethod iMethod = classMethodCache.findMethod(String.format("%s.%s()V", testedClass.getName(), method));
            log.trace("Method {} is present in cache as iMethod {}", method, iMethod);
        }
    }

    @Test(dataProvider = "testFindMethodErrorDataProvider")
    public void testFindMethodError(List<Class<?>> classesToAnalyse, Class<?> testedClass, List<String> missingMethodNames) {
        final IClassHierarchy classHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(classesToAnalyse, null);
        final ClassMethodCache classMethodCache = new ClassMethodCache(classHierarchy);
        for (String method : missingMethodNames) {
            try {
                final IMethod iMethod = classMethodCache.findMethod(String.format("%s.%s()V", testedClass.getName(), method));
                Assert.fail("Method should not be present in cache! " + iMethod);
            } catch (Exception e) {
                log.trace("As expected! Failed to find method {} for class {} in cache", method, testedClass);
            }
        }
    }

    @DataProvider
    private static Object[][] testFindClassDataProvider() {
        // unknown implemented interfaces also works well in wala
        return new Object[][] {
                // interfaces
                { Lists.newArrayList(ParentInterface.class), },
                { Lists.newArrayList(ParentInterface.class, ChildInterface1.class), },
                { Lists.newArrayList(ParentInterface.class, ChildInterface2.class) },
                { Lists.newArrayList(/*ParentInterface.class, */ ChildInterface1.class) },
                { Lists.newArrayList(/*ParentInterface.class, */ ChildInterface2.class) },

                // classes - static
                { Lists.newArrayList(StaticAbstractClass.class) },
                { Lists.newArrayList(StaticAbstractClass.class, StaticChildClass.class) },

                // classes - instance
                { Lists.newArrayList(InstanceAbstractClass.class) },
                { Lists.newArrayList(InstanceAbstractClass.class, InstanceChildClass.class) },

                // interfaces - outer
                { Lists.newArrayList(OuterParentInterface.class) },
                { Lists.newArrayList(OuterParentInterface.class, OuterChildInterface1.class) },
                { Lists.newArrayList(OuterParentInterface.class, OuterChildInterface2.class) },
                { Lists.newArrayList(/*OuterParentInterface.class,*/ OuterChildInterface1.class) },
                { Lists.newArrayList(/*OuterParentInterface.class,*/ OuterChildInterface2.class) },

                // classes - outer
                { Lists.newArrayList(AbstractOuterClass.class) },
                { Lists.newArrayList(AbstractOuterClass.class, OuterClass.class) },
                };
    }

    @DataProvider
    private static Object[][] testFindClassErrorDataProvider() {
        return new Object[][] {
                // missing StaticAbstractClass
                { Lists.newArrayList(StaticChildClass.class), Lists.newArrayList(StaticChildClass.class) },
                { Lists.newArrayList(ParentInterface.class, ChildInterface1.class, ChildInterface2.class, StaticChildClass.class),
                  Lists.newArrayList(StaticChildClass.class) },
                // missing InstanceAbstractClass
                { Lists.newArrayList(InstanceChildClass.class), Lists.newArrayList(InstanceChildClass.class) },
                { Lists.newArrayList(ParentInterface.class, ChildInterface1.class, ChildInterface2.class, InstanceChildClass.class),
                  Lists.newArrayList(InstanceChildClass.class) },
                // missing AbstractOuterClass
                { Lists.newArrayList(OuterClass.class), Lists.newArrayList(OuterClass.class) },
                { Lists.newArrayList(OuterParentInterface.class, OuterChildInterface1.class, OuterChildInterface2.class, OuterClass.class),
                  Lists.newArrayList(OuterClass.class) },
                };
    }

    @DataProvider
    private static Object[][] testFindMethodDataProvider() {
        return new Object[][] {
                { Lists.newArrayList(ParentInterface.class), ParentInterface.class, Lists.newArrayList("method") },
                { Lists.newArrayList(ParentInterface.class, ChildInterface1.class), ChildInterface1.class, Lists.newArrayList("method") },
                { Lists.newArrayList(ParentInterface.class, ChildInterface2.class), ChildInterface2.class, Lists.newArrayList("method", "method2") },
                { Lists.newArrayList(OuterParentInterface.class), OuterParentInterface.class, Lists.newArrayList("method") },
                { Lists.newArrayList(OuterParentInterface.class, OuterChildInterface1.class), OuterChildInterface1.class, Lists.newArrayList("method") },
                { Lists.newArrayList(OuterParentInterface.class, OuterChildInterface2.class), OuterChildInterface2.class,
                  Lists.newArrayList("method", "method2") },

                { Lists.newArrayList(StaticAbstractClass.class, ParentInterface.class), StaticAbstractClass.class, Lists.newArrayList("method") },
                { Lists.newArrayList(AbstractOuterClass.class, OuterParentInterface.class), AbstractOuterClass.class, Lists.newArrayList("method") },

                {
                        Lists.newArrayList(StaticAbstractClass.class,
                                StaticChildClass.class,
                                ParentInterface.class,
                                ChildInterface1.class,
                                ChildInterface2.class),
                        StaticChildClass.class,
                        Lists.newArrayList("method", "method2")
                },
                {
                        Lists.newArrayList(StaticAbstractClass.class, StaticChildClass.class, ChildInterface2.class),
                        StaticChildClass.class,
                        Lists.newArrayList("method", "method2")
                },
                // method2 is missing as ChildInterface2 was not analysed
                { Lists.newArrayList(StaticAbstractClass.class, StaticChildClass.class), StaticChildClass.class, Lists.newArrayList("method") },

                {
                        Lists.newArrayList(InstanceAbstractClass.class,
                                InstanceChildClass.class,
                                ParentInterface.class,
                                ChildInterface1.class,
                                ChildInterface2.class),
                        InstanceChildClass.class,
                        Lists.newArrayList("method", "method2")
                },
                {
                        Lists.newArrayList(InstanceAbstractClass.class, InstanceChildClass.class, ChildInterface2.class),
                        InstanceChildClass.class,
                        Lists.newArrayList("method", "method2")
                },
                // method2 is missing as ChildInterface2 was not analysed
                { Lists.newArrayList(InstanceAbstractClass.class, InstanceChildClass.class), InstanceChildClass.class, Lists.newArrayList("method") },

                {
                        Lists.newArrayList(AbstractOuterClass.class,
                                OuterClass.class,
                                OuterParentInterface.class,
                                OuterChildInterface1.class,
                                OuterChildInterface2.class),
                        OuterClass.class,
                        Lists.newArrayList("method", "method2")
                },
                {
                        Lists.newArrayList(AbstractOuterClass.class, OuterClass.class, OuterChildInterface2.class),
                        OuterClass.class,
                        Lists.newArrayList("method", "method2")
                },
                // method2 is missing as OuterChildInterface2 was not analysed
                { Lists.newArrayList(AbstractOuterClass.class, OuterClass.class), OuterClass.class, Lists.newArrayList("method") },
                };
    }

    @DataProvider
    private static Object[][] testFindMethodErrorDataProvider() {
        return new Object[][] {
                // method2 is missing as ChildInterface2 was not analysed
                { Lists.newArrayList(StaticAbstractClass.class, StaticChildClass.class), StaticChildClass.class, Lists.newArrayList("method2") },
                { Lists.newArrayList(InstanceAbstractClass.class, InstanceChildClass.class), InstanceChildClass.class, Lists.newArrayList("method2") },
                // method2 is missing as OuterChildInterface2 was not analysed
                { Lists.newArrayList(AbstractOuterClass.class, OuterClass.class), OuterClass.class, Lists.newArrayList("method2") },
                };
    }

    private interface ParentInterface {
        void method();
    }

    private interface ChildInterface1 extends ParentInterface {
        @Override
        void method();
    }

    private interface ChildInterface2 extends ParentInterface {
        default void method2() {
            // do nothing
        }
    }

    private static abstract class StaticAbstractClass implements ParentInterface {
    }

    private static class StaticChildClass extends StaticAbstractClass implements ChildInterface1, ChildInterface2 {
        @Override
        public void method() {
        }
    }

    private abstract class InstanceAbstractClass implements ParentInterface {
    }

    private class InstanceChildClass extends InstanceAbstractClass implements ChildInterface1, ChildInterface2 {
        @Override
        public void method() {
            log.trace("TEST");
        }
    }
}

interface OuterParentInterface {
    void method();
}

interface OuterChildInterface1 extends OuterParentInterface {
    @Override
    void method();
}

interface OuterChildInterface2 extends OuterParentInterface {
    default void method2() {
    }
}

abstract class AbstractOuterClass implements OuterParentInterface {
}

class OuterClass extends AbstractOuterClass implements OuterChildInterface1, OuterChildInterface2 {
    @Override
    public void method() {
    }
}
