package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
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
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.HEADERS;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.TIMESTAMP_TYPE;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaConsumerRecordHandlerTest extends AbstractTest {
    private KafkaConsumerRecordHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaConsumerRecordHandler(classMethodCache);
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
                { createForPartition(), createForPartitionExpectedResult() },
                { createForKey(), createForKeyExpectedResult() },
                { createForValue(), createForValueExpectedResult() },
                };
    }

    /**
     * @see KafkaConsumerRecordHandler#handleConstructor(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForConstructor() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findConstructorMethod(classMethodCache,
                CONSUMER_RECORD,
                new ClassWrapperImpl(String.class),
                new ClassWrapperImpl(int.class),
                new ClassWrapperImpl(long.class),
                new ClassWrapperImpl(long.class),
                TIMESTAMP_TYPE,
                new ClassWrapperImpl(Long.class),
                new ClassWrapperImpl(int.class),
                new ClassWrapperImpl(int.class),
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Object.class),
                HEADERS,
                new ClassWrapperImpl(Optional.class)));
        final IClass objectClass = classMethodCache.findClass(new ClassWrapperImpl(Object.class));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(objectClass,
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Topic"))),
                new ObjectAttributesInput(objectClass,
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet(1))),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass,
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Key"))),
                new ObjectAttributesInput(objectClass,
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Value"))),
                new ObjectAttributesInput(objectClass, ImmutableMap.of()),
                new ObjectAttributesInput(objectClass, ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(objectClass, ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForConstructorExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .addAttribute(KafkaAttributes.PARTITION, 1)
                .addAttribute(KafkaAttributes.KEY, "Key")
                .addAttribute(KafkaAttributes.VALUE, "Value")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerRecordHandler#handleTopic(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForTopic() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER_RECORD,
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
     * @see KafkaConsumerRecordHandler#handlePartition(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForPartition() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER_RECORD,
                "partition",
                new ClassWrapperImpl(int.class)));
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

    /**
     * @see KafkaConsumerRecordHandler#handleKey(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForKey() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER_RECORD,
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
     * @see KafkaConsumerRecordHandler#handleValue(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForValue() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER_RECORD,
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