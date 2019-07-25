package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.CONFIGURATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SQL_SESSION_FACTORY;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SQL_SESSION_FACTORY_BUILDER;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MyBatisSqlSessionFactoryBuilderHandlerTest extends AbstractTest {
    private MyBatisSqlSessionFactoryBuilderHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.MY_BATIS_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new MyBatisSqlSessionFactoryBuilderHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForReaderStringProperties(), createForReaderStringPropertiesExpectedResult() },
                { createForInputStreamStringProperties(), createForInputStreamStringPropertiesExpectedResult() },
                { createForConfiguration(), createForConfigurationExpectedResult() },
                };
    }

    @Nonnull
    private MethodCallDescriptionImpl createForReaderStringProperties() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(
                classMethodCache,
                SQL_SESSION_FACTORY_BUILDER,
                "build",
                SQL_SESSION_FACTORY,
                new ClassWrapperImpl(Reader.class),
                new ClassWrapperImpl(String.class),
                new ClassWrapperImpl(Properties.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Reader.class)),
                        ImmutableMap.of(Attributes.FILE_NAME.getAttributeName(),
                                ImmutableSet.of("config/MyBatisSqlSessionFactoryBuilderHandlerTest/MyBatisConfiguration.xml"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                        // use default environment ("development")
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Properties.class)),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodCallDescriptionImpl createForInputStreamStringProperties() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(
                classMethodCache,
                SQL_SESSION_FACTORY_BUILDER,
                "build",
                SQL_SESSION_FACTORY,
                new ClassWrapperImpl(InputStream.class),
                new ClassWrapperImpl(String.class),
                new ClassWrapperImpl(Properties.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(InputStream.class)),
                        ImmutableMap.of(Attributes.FILE_NAME.getAttributeName(),
                                ImmutableSet.of("config/MyBatisSqlSessionFactoryBuilderHandlerTest/MyBatisConfiguration.xml"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), ImmutableSet.of("development"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Properties.class)),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodCallDescriptionImpl createForConfiguration() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(
                classMethodCache,
                SQL_SESSION_FACTORY_BUILDER,
                "build",
                SQL_SESSION_FACTORY,
                CONFIGURATION));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(CONFIGURATION),
                        ImmutableMap.of(Attributes.DATA_SOURCE.getAttributeName(),
                                ImmutableSet.of(ImmutableMap.of(
                                        Attributes.DATABASE_CONNECTION_TYPE.getAttributeName(), "someDriver",
                                        Attributes.DATABASE_CONNECTION_USER_NAME.getAttributeName(), "someUsername",
                                        Attributes.DATABASE_CONNECTION_URL.getAttributeName(), "someUrl")))
                )
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForReaderStringPropertiesExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.FILE_NAME, "config/MyBatisSqlSessionFactoryBuilderHandlerTest/MyBatisConfiguration.xml")
                .buildFlowInfo()
                .buildEndpointFlowInfo()

                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_TYPE, "someDriver")
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, "someUsername")
                .addAttribute(Attributes.DATABASE_CONNECTION_URL, "someUrl")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private MethodEffectsDescription createForInputStreamStringPropertiesExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.FILE_NAME, "config/MyBatisSqlSessionFactoryBuilderHandlerTest/MyBatisConfiguration.xml")
                .addAttribute(Attributes.CONSTANT_VALUE, "development")
                .buildFlowInfo()
                .buildEndpointFlowInfo()

                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_TYPE, "someDriver")
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, "someUsername")
                .addAttribute(Attributes.DATABASE_CONNECTION_URL, "someUrl")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private MethodEffectsDescription createForConfigurationExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATA_SOURCE, ImmutableMap.of(
                        Attributes.DATABASE_CONNECTION_TYPE.getAttributeName(), "someDriver",
                        Attributes.DATABASE_CONNECTION_USER_NAME.getAttributeName(), "someUsername",
                        Attributes.DATABASE_CONNECTION_URL.getAttributeName(), "someUrl"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()

                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_TYPE, "someDriver")
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, "someUsername")
                .addAttribute(Attributes.DATABASE_CONNECTION_URL, "someUrl")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }
}