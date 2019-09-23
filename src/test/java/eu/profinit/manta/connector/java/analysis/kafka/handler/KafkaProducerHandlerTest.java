package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.List;
import java.util.concurrent.Future;

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
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CALLBACK;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_RECORD;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.RECORD_METADATA;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaProducerHandlerTest extends AbstractTest {
    private KafkaProducerHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaProducerHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForSend(), createForSendExpectedResult() },
                { createForSendWithCallbackComputed(), createForSendWithCallbackComputedExpectedResult() }
        };
    }

    /**
     * @see KafkaProducerHandler#handleSendProducerRecordCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSend() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER,
                "send",
                new ClassWrapperImpl(Future.class),
                PRODUCER_RECORD,
                CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(PRODUCER_RECORD),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSendExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CALLBACK,
                        "onCompletion",
                        new ClassWrapperImpl(void.class),
                        RECORD_METADATA,
                        new ClassWrapperImpl(Exception.class)))
                .buildCallbacks()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaProducerHandler#handleSendProducerRecordCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSendWithCallbackComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER,
                "send",
                new ClassWrapperImpl(Future.class),
                PRODUCER_RECORD,
                CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(PRODUCER_RECORD),
                        ImmutableMap.of(
                                KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("TOPIC"),
                                KafkaAttributes.KEY.getAttributeName(), Sets.newHashSet("KEY"),
                                KafkaAttributes.VALUE.getAttributeName(), Sets.newHashSet("VALUE")
                        )),
                new ObjectAttributesInput(
                        classMethodCache.findClass(CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        methodCallDescription.setComputedCallbacks(ImmutableMap.of(
                WalaUtils.findMethod(classMethodCache,
                        CALLBACK,
                        "onCompletion",
                        new ClassWrapperImpl(void.class),
                        RECORD_METADATA,
                        new ClassWrapperImpl(Exception.class)),
                getCallbackResult()
        ));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSendWithCallbackComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Producer#send", DataEndpointFlowInfoImpl.QueryType.UPDATE)
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.BACKEND_CALL, "Producer#send(topic=[TOPIC]; key=[KEY]; value=[VALUE])")
                .addAttribute(KafkaAttributes.TOPIC, "TOPIC")
                .addAttribute(KafkaAttributes.VALUE, "VALUE")
                .addAttribute(KafkaAttributes.KEY, "KEY")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private List<DataEndpointFlowInfo> getCallbackResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Some SQL", DataEndpointFlowInfoImpl.QueryType.QUERY)
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription().resultFlowInfos;
    }
}