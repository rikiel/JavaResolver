package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl.QueryType;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_DATASOURCE;
import static eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes.BACKEND_CALL;
import static eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes.TOPIC;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_REBALANCE_LISTENER;

public class KafkaConsumerHandler implements CallHandler {
    private final CallHandlers handlers;
    private final ClassMethodCache classMethodCache;

    private final Map<IMethod, Set<DataEndpointFlowInfo>> callbacksFlowInfo = Maps.newHashMap();

    public KafkaConsumerHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.classMethodCache = classMethodCache;
        this.handlers = new ClassCallHandlers(this,
                CONSUMER,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "subscription")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSubscription(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "subscribe")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Collection.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSubscribeCollection(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "subscribe")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Pattern.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSubscribePattern(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "subscribe")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Collection.class),
                                CONSUMER_REBALANCE_LISTENER);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSubscribeCollectionConsumerRebalanceListener(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "subscribe")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Pattern.class),
                                CONSUMER_REBALANCE_LISTENER);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSubscribePatternConsumerRebalanceListener(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "unsubscribe")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleUnsubscribe(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "assign")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Collection.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleAssign(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "poll")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(long.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handlePollLong(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "poll")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Duration.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handlePollDuration(methodCallDescription);
                    }
                }
        );
    }

    @Override
    public boolean canHandle(@Nonnull final IMethod iMethod) {
        return handlers.canHandle(iMethod);
    }

    @Nonnull
    @Override
    public MethodEffectsDescriptionBuilder handle(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handlers.handle(methodCallDescription);
    }

    @Nonnull
    @Override
    public Set<DataEndpointFlowInfo> getArgumentFlow(@Nonnull final IMethod iMethod, final int argumentIndex) {
        return callbacksFlowInfo.getOrDefault(iMethod, ImmutableSet.of());
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#subscription()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscription(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .addAttribute(Attributes.CONSTANT_VALUE, methodCallDescription.getReceiverAttributes().getAttribute(KafkaAttributes.TOPIC))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#subscribe(Collection)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribeCollection(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handleSubscribe(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#subscribe(Pattern)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribePattern(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handleSubscribe(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#subscribe(Collection, org.apache.kafka.clients.consumer.ConsumerRebalanceListener)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribeCollectionConsumerRebalanceListener(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handleSubscribeWithCallback(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#subscribe(Pattern, org.apache.kafka.clients.consumer.ConsumerRebalanceListener)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribePatternConsumerRebalanceListener(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handleSubscribeWithCallback(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#assign(Collection)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleAssign(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RECEIVER)
                .addAttributesExcept(methodCallDescription.getReceiverAttributes(), KafkaAttributes.TOPIC)
                .addAttributes(methodCallDescription.getArgumentAttributes(0))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#unsubscribe()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleUnsubscribe(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RECEIVER)
                .addAttributesExcept(methodCallDescription.getReceiverAttributes(), KafkaAttributes.TOPIC)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#poll(long)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handlePollLong(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handlePoll(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.Consumer#poll(Duration)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handlePollDuration(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return handlePoll(methodCallDescription);
    }

    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribe(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_BOTH_RESULT_AND_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(TOPIC, methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @Nonnull
    private MethodEffectsDescriptionBuilder handleSubscribeWithCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IClass listenerType = methodCallDescription.getArgumentAttributes(1).getType();
        final IMethod onPartitionsRevoked = WalaUtils.findMethod(classMethodCache,
                listenerType,
                "onPartitionsRevoked",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Collection.class));
        final IMethod onPartitionsAssigned = WalaUtils.findMethod(classMethodCache,
                listenerType,
                "onPartitionsAssigned",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(Collection.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_BOTH_RESULT_AND_RECEIVER);

        builder
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(TOPIC, methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.CONSTANT_VALUE));

        handleCallbacks(builder, methodCallDescription, onPartitionsRevoked, onPartitionsAssigned);

        return builder;
    }

    @Nonnull
    private MethodEffectsDescriptionBuilder handlePoll(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, FlowPropagationType.TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .addSql("Consumer#poll", QueryType.QUERY)
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .addAttribute(BACKEND_CALL, String.format("Consumer#poll(topic=%s)", methodCallDescription.getReceiverAttributes().getAttribute(TOPIC)))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    private void handleCallbacks(@Nonnull final MethodEffectsDescriptionBuilder builder,
                                 @Nonnull final MethodCallDescriptionImpl methodCallDescription,
                                 @Nonnull final IMethod callback1,
                                 @Nonnull final IMethod callback2) {
        // handle callback
        final List<DataEndpointFlowInfo> callback1Results = methodCallDescription.getComputedCallbacks().getOrDefault(callback1, ImmutableList.of());
        final List<DataEndpointFlowInfo> callback2Results = methodCallDescription.getComputedCallbacks().getOrDefault(callback2, ImmutableList.of());
        if (callback1Results.isEmpty() || callback2Results.isEmpty()) {
            builder
                    .callbacksBuilder()
                    .addCallback(callback1)
                    .addCallback(callback2);
        } else {
            builder
                    .addResultFlowInfos(callback1Results)
                    .addResultFlowInfos(callback2Results);
        }

        // store flow for callback
        callbacksFlowInfo.computeIfAbsent(callback1, method -> Sets.newHashSet())
                .addAll(MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_DATASOURCE).buildMethodEffectsDescription().resultFlowInfos);
        callbacksFlowInfo.computeIfAbsent(callback2, method -> Sets.newHashSet())
                .addAll(MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_DATASOURCE).buildMethodEffectsDescription().resultFlowInfos);
    }
}
