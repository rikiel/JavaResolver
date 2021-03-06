package eu.profinit.manta.connector.java.analysis.datasource.handler;

import javax.annotation.Nonnull;

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
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.datasource.handler.DataSourceClassWrappers.TERA_DATA_SOURCE;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class TeraDataSourceHandlerTest extends AbstractTest {
    private TeraDataSourceHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.DATASOURCE_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new TeraDataSourceHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForSetUser(), createForSetUserExpectedResult() },
                };
    }

    @Nonnull
    private MethodCallDescriptionImpl createForSetUser() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                TERA_DATA_SOURCE,
                "setuser",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(String.class)));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput());
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Username")))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSetUserExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(Attributes.CONSTANT_VALUE, Sets.newHashSet("Username"))
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, Sets.newHashSet("Username"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }
}
