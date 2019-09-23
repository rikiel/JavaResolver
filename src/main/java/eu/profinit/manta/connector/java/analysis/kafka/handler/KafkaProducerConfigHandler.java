package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.util.Set;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.Validate;

import static eu.profinit.manta.connector.java.analysis.kafka.handler.KafkaClassWrappers.PRODUCER_CONFIG;

public class KafkaProducerConfigHandler implements CallHandler {
    private final CallHandlers handlers;
    private final ClassMethodCache classMethodCache;

    public KafkaProducerConfigHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.classMethodCache = classMethodCache;
        this.handlers = new ClassCallHandlers(this,
                PRODUCER_CONFIG,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return iMethod.isInit();
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleConstructor(methodCallDescription);
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
     * See org.apache.kafka.clients.producer.ProducerConfig#ProducerConfig(Properties)
     * See org.apache.kafka.clients.producer.ProducerConfig#ProducerConfig(Map)
     * See org.apache.kafka.clients.producer.ProducerConfig#ProducerConfig(Map, boolean)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleConstructor(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final Set<Object> servers = KafkaHandlerUtils.getServerNames(methodCallDescription.getArgumentAttributes(0),
                new FileContentReader(classMethodCache.getJarFiles()));
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(KafkaAttributes.SERVER, servers)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }
}
