package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_DATASOURCE;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CALLBACK;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_RECORD;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.RECORD_METADATA;

public class KafkaProducerHandler implements CallHandler {
    private final CallHandlers handlers;
    private final ClassMethodCache classMethodCache;

    private final Map<IMethod, Set<DataEndpointFlowInfo>> callbacksFlowInfo = Maps.newHashMap();

    public KafkaProducerHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.classMethodCache = classMethodCache;
        this.handlers = new ClassCallHandlers(this,
                PRODUCER,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "send")
                               && WalaUtils.isMatchingArguments(iMethod,
                                PRODUCER_RECORD);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSendProducerRecord(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "send")
                               && WalaUtils.isMatchingArguments(iMethod,
                                PRODUCER_RECORD,
                                CALLBACK);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSendProducerRecordCallback(methodCallDescription);
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
     * See org.apache.kafka.clients.producer.Producer#send(org.apache.kafka.clients.producer.ProducerRecord)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSendProducerRecord(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_BOTH_RESULT_AND_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .addSql("Producer#send", DataEndpointFlowInfoImpl.QueryType.UPDATE)
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.BACKEND_CALL, formatSendCall(methodCallDescription.getArgumentAttributes(0)))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.apache.kafka.clients.producer.Producer#send(org.apache.kafka.clients.producer.ProducerRecord, org.apache.kafka.clients.producer.Callback)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSendProducerRecordCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod onCompletion = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(1).getType(),
                "onCompletion",
                new ClassWrapperImpl(void.class),
                RECORD_METADATA,
                new ClassWrapperImpl(Exception.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_BOTH_RESULT_AND_RECEIVER);

        handleCallback(builder, methodCallDescription, onCompletion);

        return builder;
    }

    private void handleCallback(@Nonnull final MethodEffectsDescriptionBuilder builder,
                                @Nonnull final MethodCallDescriptionImpl methodCallDescription,
                                @Nonnull final IMethod iMethod) {
        // handle callback
        final List<DataEndpointFlowInfo> callbackResults = methodCallDescription.getComputedCallbacks().getOrDefault(iMethod, ImmutableList.of());
        if (callbackResults.isEmpty()) {
            builder
                    .callbacksBuilder()
                    .addCallback(iMethod);
        } else {
            builder
                    .dataEndpointFlowInfoBuilder()
                    .addSql("Producer#send", DataEndpointFlowInfoImpl.QueryType.UPDATE)
                    .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                    .addAttribute(KafkaAttributes.BACKEND_CALL, formatSendCall(methodCallDescription.getArgumentAttributes(0)))
                    .buildFlowInfo()
                    .buildEndpointFlowInfo()
                    .addResultFlowInfos(callbackResults);
        }

        // store flow for callback
        callbacksFlowInfo.computeIfAbsent(iMethod, method -> Sets.newHashSet())
                .addAll(MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_DATASOURCE).buildMethodEffectsDescription().resultFlowInfos);
    }

    @Nonnull
    private String formatSendCall(@Nonnull final ObjectAttributesInput producerRecordAttribute) {
        return String.format("Producer#send(topic=%s; key=%s; value=%s)",
                producerRecordAttribute.getAttribute(KafkaAttributes.TOPIC),
                producerRecordAttribute.getAttribute(KafkaAttributes.KEY),
                producerRecordAttribute.getAttribute(KafkaAttributes.VALUE));
    }
}
