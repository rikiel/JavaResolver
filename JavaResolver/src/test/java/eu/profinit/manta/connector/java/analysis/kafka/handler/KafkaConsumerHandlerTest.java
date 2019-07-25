package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.TestAttributeName;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl.QueryType;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes.BACKEND_CALL;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_REBALANCE_LISTENER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_RECORDS;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class KafkaConsumerHandlerTest extends AbstractTest {
    private KafkaConsumerHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.KAFKA_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new KafkaConsumerHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForSubscribeCollectionConsumerRebalancerListener(), createForSubscribeCollectionConsumerRebalancerListenerExpectedResult() },
                { createForSubscribeCollectionConsumerRebalancerListenerWithCallbacksComputed(),
                  createForSubscribeCollectionConsumerRebalancerListenerWithCallbacksComputedExpectedResult() },
                { createForSubscribePatternConsumerRebalancerListener(), createForSubscribePatternConsumerRebalancerListenerExpectedResult() },
                { createForSubscribePatternConsumerRebalancerListenerWithCallbacksComputed(),
                  createForSubscribePatternConsumerRebalancerListenerWithCallbacksComputedExpectedResult() },
                { createForSubscription(), createForSubscriptionExpectedResult() },
                { createForAssign(), createForAssignExpectedResult() },
                { createForUnsubscribe(), createForUnsubscribeExpectedResult() },
                { createForPollLong(), createForPollLongExpectedResult() },
                { createForPollDuration(), createForPollDurationExpectedResult() },
                };
    }

    /**
     * @see KafkaConsumerHandler#handleSubscribeCollectionConsumerRebalanceListener(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSubscribeCollectionConsumerRebalancerListener() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "subscribe",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Collection.class),
                CONSUMER_REBALANCE_LISTENER));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Collection.class)),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(CONSUMER_REBALANCE_LISTENER),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSubscribeCollectionConsumerRebalancerListenerExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsRevoked",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)))
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsAssigned",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)))
                .buildCallbacks()
                .dataEndpointFlowInfoBuilder()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleSubscribeCollectionConsumerRebalanceListener(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSubscribeCollectionConsumerRebalancerListenerWithCallbacksComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "subscribe",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Collection.class),
                CONSUMER_REBALANCE_LISTENER));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Collection.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Topic"))),
                new ObjectAttributesInput(classMethodCache.findClass(CONSUMER_REBALANCE_LISTENER),
                        ImmutableMap.of())
        ));
        methodCallDescription.setComputedCallbacks(ImmutableMap.of(
                WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsRevoked",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)),
                getCallbackResult(),

                WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsAssigned",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)),
                getCallbackResult()
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSubscribeCollectionConsumerRebalancerListenerWithCallbacksComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(Attributes.CONSTANT_VALUE, "Topic")
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .addResultFlowInfos(getCallbackResult())
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleSubscribePatternConsumerRebalanceListener(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSubscribePatternConsumerRebalancerListener() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "subscribe",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Pattern.class),
                CONSUMER_REBALANCE_LISTENER));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Pattern.class)),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(CONSUMER_REBALANCE_LISTENER),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSubscribePatternConsumerRebalancerListenerExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsRevoked",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)))
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsAssigned",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)))
                .buildCallbacks()
                .dataEndpointFlowInfoBuilder()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleSubscribePatternConsumerRebalanceListener(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSubscribePatternConsumerRebalancerListenerWithCallbacksComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "subscribe",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Pattern.class),
                CONSUMER_REBALANCE_LISTENER));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Pattern.class)),
                        ImmutableMap.of(Attributes.CONSTANT_VALUE.getAttributeName(), Sets.newHashSet("Topic"))),
                new ObjectAttributesInput(
                        classMethodCache.findClass(CONSUMER_REBALANCE_LISTENER),
                        ImmutableMap.of())
        ));
        methodCallDescription.setComputedCallbacks(ImmutableMap.of(
                WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsRevoked",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)),
                getCallbackResult(),

                WalaUtils.findMethod(classMethodCache,
                        CONSUMER_REBALANCE_LISTENER,
                        "onPartitionsAssigned",
                        new ClassWrapperImpl(void.class),
                        new ClassWrapperImpl(Collection.class)),
                getCallbackResult()
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSubscribePatternConsumerRebalancerListenerWithCallbacksComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(Attributes.CONSTANT_VALUE, "Topic")
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .addResultFlowInfos(getCallbackResult())
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleSubscription(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForSubscription() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "subscription",
                new ClassWrapperImpl(Set.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of(
                        KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("Topic"),
                        "OtherAttribute", Sets.newHashSet("SomeValue"))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForSubscriptionExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, "Topic")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleAssign(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForAssign() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "assign",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Collection.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(
                        classMethodCache.findClass(new ClassWrapperImpl(Collection.class)),
                        ImmutableMap.of(
                                KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("Topic"),
                                KafkaAttributes.PARTITION.getAttributeName(), Sets.newHashSet(1, 2)))
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of(
                        KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("OldTopic"),
                        "OtherAttribute", Sets.newHashSet("SomeValue"))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForAssignExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .addAttribute(KafkaAttributes.PARTITION, Sets.newHashSet(1, 2))
                .addAttribute(new TestAttributeName("OtherAttribute"), "SomeValue")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handleUnsubscribe(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForUnsubscribe() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "unsubscribe",
                new ClassWrapperImpl(void.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of(
                        KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("Topic"),
                        "OtherAttribute", Sets.newHashSet("SomeValue"))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForUnsubscribeExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(new TestAttributeName("OtherAttribute"), "SomeValue")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handlePollLong(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForPollLong() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "poll",
                CONSUMER_RECORDS,
                new ClassWrapperImpl(long.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of(KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("Topic"))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForPollLongExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Consumer#poll", QueryType.QUERY)
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(BACKEND_CALL, "Consumer#poll(topic=[Topic])")
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    /**
     * @see KafkaConsumerHandler#handlePollDuration(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForPollDuration() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                CONSUMER,
                "poll",
                CONSUMER_RECORDS,
                new ClassWrapperImpl(Duration.class)));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList());
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of(KafkaAttributes.TOPIC.getAttributeName(), Sets.newHashSet("Topic"))));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForPollDurationExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Consumer#poll", QueryType.QUERY)
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(BACKEND_CALL, "Consumer#poll(topic=[Topic])")
                .addAttribute(KafkaAttributes.TOPIC, "Topic")
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private List<DataEndpointFlowInfo> getCallbackResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Some SQL", QueryType.QUERY)
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription().resultFlowInfos;
    }
}