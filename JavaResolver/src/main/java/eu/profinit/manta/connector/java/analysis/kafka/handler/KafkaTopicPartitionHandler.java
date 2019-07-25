package eu.profinit.manta.connector.java.analysis.kafka.handler;

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

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.TOPIC_PARTITION;

public class KafkaTopicPartitionHandler implements CallHandler {
    private final CallHandlers handlers;

    public KafkaTopicPartitionHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                TOPIC_PARTITION,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return iMethod.isInit() && WalaUtils.isMatchingArguments(
                                iMethod,
                                new ClassWrapperImpl(String.class),
                                new ClassWrapperImpl(int.class));
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
     * See org.apache.kafka.common.TopicPartition#TopicPartition(String, int)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleConstructor(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.newFlow();
        final ObjectFlowInfoBuilder flowBuilder = builder
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER);

        KafkaHandlerUtils.setTopic(flowBuilder, methodCallDescription.getArgumentAttributes(0));
        KafkaHandlerUtils.setPartition(flowBuilder, methodCallDescription.getArgumentAttributes(1));

        return builder;
    }

    /**
     * See org.apache.kafka.common.TopicPartition#topic()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleTopic(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getTopic(methodCallDescription);
    }

    /**
     * See org.apache.kafka.common.TopicPartition#partition()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handlePartition(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return KafkaHandlerUtils.getPartition(methodCallDescription);
    }
}
