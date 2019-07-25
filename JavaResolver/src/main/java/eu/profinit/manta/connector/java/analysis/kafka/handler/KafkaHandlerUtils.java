package eu.profinit.manta.connector.java.analysis.kafka.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder.ObjectFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAttributes;
import eu.profinit.manta.connector.java.analysis.utils.CollectionToString;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.model.flowgraph.AttributeValueConstants;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.propagateFlow;

class KafkaHandlerUtils {
    private static final Logger log = LoggerFactory.getLogger(KafkaHandlerUtils.class);

    static void setTopic(@Nonnull final ObjectFlowInfoBuilder flowBuilder,
                         @Nonnull final ObjectAttributesInput argumentAttributes) {
        flowBuilder.addAttribute(KafkaAttributes.TOPIC,
                argumentAttributes.getAttribute(Attributes.CONSTANT_VALUE),
                AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT);
    }

    static void setPartition(@Nonnull final ObjectFlowInfoBuilder flowBuilder,
                             @Nonnull final ObjectAttributesInput argumentAttributes) {
        flowBuilder.addAttribute(KafkaAttributes.PARTITION,
                argumentAttributes.getAttribute(Attributes.CONSTANT_VALUE),
                AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT);
    }

    static void setKey(@Nonnull final ObjectFlowInfoBuilder flowBuilder,
                       @Nonnull final ObjectAttributesInput argumentAttributes) {
        flowBuilder.addAttribute(KafkaAttributes.KEY,
                argumentAttributes.getAttribute(Attributes.CONSTANT_VALUE),
                AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT);
    }

    static void setValue(@Nonnull final ObjectFlowInfoBuilder flowBuilder,
                         @Nonnull final ObjectAttributesInput argumentAttributes) {
        flowBuilder.addAttribute(KafkaAttributes.VALUE,
                argumentAttributes.getAttribute(Attributes.CONSTANT_VALUE),
                AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT);
    }

    @Nonnull
    static MethodEffectsDescriptionBuilder getTopic(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return propagateFlow(methodCallDescription, FlowPropagationType.TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .setAttribute(Attributes.CONSTANT_VALUE,
                        methodCallDescription.getReceiverAttributes().getAttribute(KafkaAttributes.TOPIC),
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @Nonnull
    static MethodEffectsDescriptionBuilder getPartition(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return propagateFlow(methodCallDescription, FlowPropagationType.TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .setAttribute(Attributes.CONSTANT_VALUE,
                        methodCallDescription.getReceiverAttributes().getAttribute(KafkaAttributes.PARTITION),
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @Nonnull
    static MethodEffectsDescriptionBuilder getKey(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return propagateFlow(methodCallDescription, FlowPropagationType.TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .setAttribute(Attributes.CONSTANT_VALUE,
                        methodCallDescription.getReceiverAttributes().getAttribute(KafkaAttributes.KEY),
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @Nonnull
    static MethodEffectsDescriptionBuilder getValue(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return propagateFlow(methodCallDescription, FlowPropagationType.TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(FlowPropagationType.TO_RESULT)
                .setAttribute(Attributes.CONSTANT_VALUE,
                        methodCallDescription.getReceiverAttributes().getAttribute(KafkaAttributes.VALUE),
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT)
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    static Set<Object> getServerNames(@Nonnull final ObjectAttributesInput propertiesAttributes, @Nonnull final FileContentReader reader) {
        // using the Properties.setProperty(property, value)
        final Set<Object> propertiesFromJavaCode = propertiesAttributes.getAttribute(Attributes.MAP_KEYS_TO_VALUES).stream()
                .flatMap(object -> ((Map<String, Set<Object>>) object).getOrDefault(KafkaConstants.BOOTSTRAP_SERVERS_CONFIG, ImmutableSet.of()).stream())
                .collect(Collectors.toSet());

        // using Properties.load(filename)
        final Set<Object> propertiesFromExternalFiles = propertiesAttributes.getAttribute(Attributes.FILE_NAME).stream()
                .map(file -> {
                    try {
                        final String fileContent = reader.readFile((String) file);
                        log.trace("Parsing property file {} with content:\n{}", file, fileContent);
                        final Properties properties = new Properties();
                        properties.load(new StringReader(fileContent));
                        return properties.getProperty(KafkaConstants.BOOTSTRAP_SERVERS_CONFIG, null);
                    } catch (IOException e) {
                        log.trace("Failed to read property file {}", file, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.trace("Using servers from Java code: {} and from external files {}",
                new CollectionToString<>(propertiesFromJavaCode),
                new CollectionToString<>(propertiesFromExternalFiles));

        return Sets.union(propertiesFromJavaCode, propertiesFromExternalFiles);
    }
}
