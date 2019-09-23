package eu.profinit.manta.connector.java.analysis.kafka.handler;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder.ObjectFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_RECORD;

public class KafkaProducerRecordHandler implements CallHandler {
    private final CallHandlers handlers;

    public KafkaProducerRecordHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                PRODUCER_RECORD,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return iMethod.isInit() && WalaUtils.isMatchingArguments(
                                iMethod,
                                new ClassWrapperImpl(String.class),
                                new ClassWrapperImpl(Integer.class),
                                new ClassWrapperImpl(Long.class),
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(Iterable.class));
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
     * See org.apache.kafka.clients.producer.ProducerRecord#ProducerRecord(String, Integer, Long, Object, Object, Iterable)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleConstructor(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription,
                FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER);
        final ObjectFlowInfoBuilder flowBuilder = builder
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER);

        KafkaHandlerUtils.setTopic(flowBuilder, methodCallDescription.getArgumentAttributes(0));
        KafkaHandlerUtils.setPartition(flowBuilder, methodCallDescription.getArgumentAttributes(1));
        KafkaHandlerUtils.setKey(flowBuilder, methodCallDescription.getArgumentAttributes(3));
        KafkaHandlerUtils.setValue(flowBuilder, methodCallDescription.getArgumentAttributes(4));

        return builder;
    }

    /**
     * See org.apache.kafka.clients.producer.ProducerRecord#topic()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleTopic(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getTopic(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.producer.ProducerRecord#key()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleKey(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getKey(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.producer.ProducerRecord#value()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleValue(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getValue(methodCallDescription);
    }

    /**
     * See org.apache.kafka.clients.producer.ProducerRecord#partition()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handlePartition(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getPartition(methodCallDescription);
    }
}
