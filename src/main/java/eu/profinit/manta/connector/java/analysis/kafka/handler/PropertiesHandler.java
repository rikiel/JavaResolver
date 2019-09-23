package eu.profinit.manta.connector.java.analysis.kafka.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ibm.wala.classLoader.IMethod;
import eu.profinit.manta.connector.java.analysis.common.*;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// workaround for Symbolic analysis library that fails to correctly handle Properties class
public class PropertiesHandler implements CallHandler{
    private final CallHandlers handlers;

    public PropertiesHandler() {
        this.handlers = new ClassCallHandlers(this,
                new ClassWrapperImpl(Properties.class),
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "load");
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleLoad(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "setProperty");
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSetProperty(methodCallDescription);
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
    private MethodEffectsDescriptionBuilder handleLoad(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER);
    }

   @Nonnull
    private MethodEffectsDescriptionBuilder handleSetProperty(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER)
                .clearAttribute(Attributes.CONSTANT_VALUE)
                .addAttribute(Attributes.MAP_KEYS_TO_VALUES, buildKeyToValueMap(
                        methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.CONSTANT_VALUE),
                        methodCallDescription.getArgumentAttributes(1).getAttribute(Attributes.CONSTANT_VALUE)))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @Nonnull
    private Map<String, Set<Object>> buildKeyToValueMap(Set<Object> keys, Set<Object> values) {
        final Map<String, Set<Object>> result = Maps.newHashMap();
        for (Object key : keys) {
            result.put(key.toString(), values);
        }
        return result;
    }
}
