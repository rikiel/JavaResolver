package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.kafka.clients.CommonClientConfigs;
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
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_CONFIG;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaProducerConfigHandlerTest extends AbstractTest {
    private KafkaProducerConfigHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaProducerConfigHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForConstructor(), createForConstructorExpectedResult() },
                };
    }

    /**
     * @see KafkaProducerConfigHandler#handleConstructor(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForConstructor() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findConstructorMethod(classMethodCache,
                PRODUCER_CONFIG,
                new ClassWrapperImpl(Map.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Map.class)),
                        ImmutableMap.of(Attributes.MAP_KEYS_TO_VALUES.getAttributeName(), ImmutableSet.of(
                                ImmutableMap.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, ImmutableSet.of("ServerName"))
                        )))
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForConstructorExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.SERVER, "ServerName")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }
}