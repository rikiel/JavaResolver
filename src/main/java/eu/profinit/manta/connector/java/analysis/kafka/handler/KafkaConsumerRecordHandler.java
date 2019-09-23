package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder.ObjectFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.CONSUMER_RECORD;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.HEADERS;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.TIMESTAMP_TYPE;

public class KafkaConsumerRecordHandler implements CallHandler {
    private final CallHandlers handlers;

    public KafkaConsumerRecordHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                CONSUMER_RECORD,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return iMethod.isInit() && WalaUtils.isMatchingArguments(
                                iMethod,
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
                                new ClassWrapperImpl(Optional.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleConstructor(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "topic")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleTopic(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "partition")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handlePartition(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "key")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleKey(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "value")
                               && WalaUtils.isMatchingArguments(iMethod);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleValue(methodCallDescription);
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

    /**
     * See org.apache.kafka.clients.consumer.ConsumerRecord#ConsumerRecord(String, int, long, long, org.apache.kafka.common.record.TimestampType, Long, int, int, Object, Object, org.apache.kafka.common.header.Headers, Optional)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleConstructor(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.newFlow();
        final ObjectFlowInfoBuilder flowBuilder = builder
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER);

        KafkaHandlerUtils.setTopic(flowBuilder, methodCallDescription.getArgumentAttributes(0));
        KafkaHandlerUtils.setPartition(flowBuilder, methodCallDescription.getArgumentAttributes(1));
        KafkaHandlerUtils.setKey(flowBuilder, methodCallDescription.getArgumentAttributes(8));
        KafkaHandlerUtils.setValue(flowBuilder, methodCallDescription.getArgumentAttributes(9));

        return builder;
    }

    /**
     * See org.apache.kafka.clients.consumer.ConsumerRecord#topic()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleTopic(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getTopic(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.ConsumerRecord#partition()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handlePartition(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getPartition(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.ConsumerRecord#key()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleKey(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getKey(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.consumer.ConsumerRecord#value()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleValue(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getValue(methodCallDescription);
    }
}
