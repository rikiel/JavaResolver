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
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.TOPIC_PARTITION;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaTopicPartitionHandlerTest extends AbstractTest {
    private KafkaTopicPartitionHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaTopicPartitionHandler(classMethodCache);
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
                };
    }

    /**
     * @see KafkaTopicPartitionHandler#handleConstructor(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForConstructor() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findConstructorMethod(classMethodCache,
                TOPIC_PARTITION,
                new ClassWrapperImpl(String.class),
                new ClassWrapperImpl(Integer.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(String.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Topic1", "Topic2"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Integer.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet(1, 2)))
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
                .addAttribute(KafkaAttributes.PARTITION, Sets.newHashSet(1, 2))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaTopicPartitionHandler#handleTopic(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForTopic() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                TOPIC_PARTITION,
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
     * @see KafkaTopicPartitionHandler#handleTopic(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForPartition() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                TOPIC_PARTITION,
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