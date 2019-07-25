package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.ENVIRONMENT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.TRANSACTION_FACTORY;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class MyBatisEnvironmentHandlerTest extends AbstractTest {
    private MyBatisEnvironmentHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.MY_BATIS_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new MyBatisEnvironmentHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForEnvironmentConstructor(), createExpectedMethodEffectsDescription() },
                };
    }

    @Nonnull
    private MethodCallDescriptionImpl createForEnvironmentConstructor() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findConstructorMethod(classMethodCache,
                ENVIRONMENT,
                new ClassWrapperImpl(String.class),
                TRANSACTION_FACTORY,
                new ClassWrapperImpl(DataSource.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("env1", "env2"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(TRANSACTION_FACTORY),
                        ImmutableMap.of()),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(DataSource.class)),
                        ImmutableMap.of(Attributes.DATA_SOURCE.getAttributeName(), Sets.newHashSet("dataSource1", "dataSource2")))
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createExpectedMethodEffectsDescription() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(Attributes.DATA_SOURCE, Sets.newHashSet("dataSource1", "dataSource2"))
                .addAttribute(Attributes.CONSTANT_VALUE, Sets.newHashSet("env1", "env2"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(MyBatisAttributes.ENVIRONMENT_NAME, Sets.newHashSet("env1", "env2"))
                .addAttribute(Attributes.DATA_SOURCE, Sets.newHashSet("dataSource1", "dataSource2"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }
}