package eu.profinit.manta.connector.java.analysis.kafka.handler;

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
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_RECORD;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_RECORD;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaProducerRecordHandlerTest extends AbstractTest {
    private KafkaProducerRecordHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaProducerRecordHandler(classMethodCache);
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
                { createForTopic(), createForTopicExpectedResult() },
                { createForKey(), createForKeyExpectedResult() },
                { createForValue(), createForValueExpectedResult() },
                { createForPartition(), createForPartitionExpectedResult() },
                };
    }

    /**
     * @see KafkaProducerRecordHandler#handleConstructor(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForConstructor() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findConstructorMethod(classMethodCache,
                PRODUCER_RECORD,
                new ClassWrapperImpl(String.class),
                new ClassWrapperImpl(Integer.class),
                new ClassWrapperImpl(Long.class),
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Iterable.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Topic1", "Topic2"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Integer.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet(1))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Long.class)),
                        ImmutableMap.of()),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Key"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Value"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Iterable.class)),
                        ImmutableMap.of())
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
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.TOPIC, Sets.newHashSet("Topic1", "Topic2"))
                .addAttribute(KafkaAttributes.PARTITION, Sets.newHashSet(1))
                .addAttribute(KafkaAttributes.KEY, Sets.newHashSet("Key"))
                .addAttribute(KafkaAttributes.VALUE, Sets.newHashSet("Value"))
                .addAttribute(Attributes.CONSTANT_VALUE, Sets.newHashSet(1, "Topic1", "Topic2", "Value", "Key"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaProducerRecordHandler#handleTopic(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForTopic() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER_RECORD,
                "topic",
                new ClassWrapperImpl(String.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(getReceiverAttributes());
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForTopicExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, "topic")
                .addAttribute(KafkaAttributes.TOPIC, "topic")
                .addAttribute(KafkaAttributes.PARTITION, 1)
                .addAttribute(KafkaAttributes.KEY, "key")
                .addAttribute(KafkaAttributes.VALUE, "value")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaProducerRecordHandler#handleKey(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForKey() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER_RECORD,
                "key",
                new ClassWrapperImpl(Object.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(getReceiverAttributes());
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForKeyExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, "key")
                .addAttribute(KafkaAttributes.TOPIC, "topic")
                .addAttribute(KafkaAttributes.PARTITION, 1)
                .addAttribute(KafkaAttributes.KEY, "key")
                .addAttribute(KafkaAttributes.VALUE, "value")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaProducerRecordHandler#handleValue(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForValue() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER_RECORD,
                "value",
                new ClassWrapperImpl(Object.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(getReceiverAttributes());
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForValueExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, "value")
                .addAttribute(KafkaAttributes.TOPIC, "topic")
                .addAttribute(KafkaAttributes.PARTITION, 1)
                .addAttribute(KafkaAttributes.KEY, "key")
                .addAttribute(KafkaAttributes.VALUE, "value")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaProducerRecordHandler#handleTopic(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForPartition() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                PRODUCER_RECORD,
                "partition",
                new ClassWrapperImpl(Integer.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(getReceiverAttributes());
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForPartitionExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, 1)
                .addAttribute(KafkaAttributes.TOPIC, "topic")
                .addAttribute(KafkaAttributes.PARTITION, 1)
                .addAttribute(KafkaAttributes.KEY, "key")
                .addAttribute(KafkaAttributes.VALUE, "value")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private ObjectAttributesInput getReceiverAttributes() {
        return new ObjectAttributesInput(classMethodCache.findClass(CONSUMER_RECORD),
                ImmutableMap.of(
                        KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("topic"),
                        KafkaAttributes.PARTITION.getAttributeName(), Sets.newHashSet(1),
                        KafkaAttributes.KEY.getAttributeName(), Sets.newHashSet("key"),
                        KafkaAttributes.VALUE.getAttributeName(), Sets.newHashSet("value")
                ));
    }
}