package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

@SuppressWarnings("UnusedReturnValue")
public class MethodEffectsDescriptionBuilder {
    private final MethodEffectsDescription methodEffectsDescription;

    private MethodEffectsDescriptionBuilder() {
        methodEffectsDescription = new MethodEffectsDescription();
        methodEffectsDescription.resultFlowInfos = Lists.newArrayList();
        methodEffectsDescription.callbackMethodsToAnalyze = Lists.newArrayList();
    }

    /**
     * @param methodCallDescription Input flow that should be forwarded to output
     * @param flowPropagationType   Propagation type
     * @return Creates builder with flow from previous calls
     */
    @Nonnull
    public static MethodEffectsDescriptionBuilder propagateFlow(@Nonnull final MethodCallDescriptionImpl methodCallDescription,
                                                                @Nonnull final FlowPropagationType flowPropagationType) {
        if (methodCallDescription.hasEmptyAttributes()) {
            return newFlow();
        } else {
            return newFlow()
                    .dataEndpointFlowInfoBuilder()
                    .objectFlowInfoBuilder(flowPropagationType)
                    .addAttributes(methodCallDescription.getArgumentsAttributes())
                    .addAttributes(methodCallDescription.getReceiverAttributes())
                    .buildFlowInfo()
                    .buildEndpointFlowInfo();
        }
    }

    /**
     * @return Creates builder with empty flow
     */
    @Nonnull
    public static MethodEffectsDescriptionBuilder newFlow() {
        return new MethodEffectsDescriptionBuilder();
    }

    @Nonnull
    public MethodEffectsDescription buildMethodEffectsDescription() {
        return methodEffectsDescription;
    }

    @Nonnull
    public CallbacksBuilder callbacksBuilder() {
        return new CallbacksBuilder();
    }

    @Nonnull
    public DataEndpointFlowInfoBuilder dataEndpointFlowInfoBuilder() {
        return new DataEndpointFlowInfoBuilder();
    }

    @Nonnull
    public MethodEffectsDescriptionBuilder addResultFlowInfos(@Nonnull final List<DataEndpointFlowInfo> values) {
        Validate.notNullAll(values);
        methodEffectsDescription.resultFlowInfos.addAll(values);
        return this;
    }

    public enum FlowPropagationType {
        TO_RECEIVER,
        TO_RESULT,
        TO_BOTH_RESULT_AND_RECEIVER,
        TO_DATASOURCE,
    }

    public class DataEndpointFlowInfoBuilder {
        private final DataEndpointFlowInfoImpl dataEndpointFlowInfo;

        private DataEndpointFlowInfoBuilder() {
            Validate.isTrue(methodEffectsDescription.resultFlowInfos.size() <= 1, "Does not support more flows!");
            if (methodEffectsDescription.resultFlowInfos.isEmpty()) {
                dataEndpointFlowInfo = new DataEndpointFlowInfoImpl();
                methodEffectsDescription.resultFlowInfos.add(dataEndpointFlowInfo);
            } else {
                dataEndpointFlowInfo = (DataEndpointFlowInfoImpl) methodEffectsDescription.resultFlowInfos.get(0);
            }
        }

        @Nonnull
        public DataEndpointFlowInfoBuilder addSql(@Nonnull final String sql, @Nonnull final DataEndpointFlowInfoImpl.QueryType queryType) {
            dataEndpointFlowInfo.setSql(sql, queryType);
            return this;
        }

        @Nonnull
        public ObjectFlowInfoBuilder objectFlowInfoBuilder(@Nonnull final FlowPropagationType flowPropagationType) {
            final ObjectFlowInfoBuilder builder;
            switch (flowPropagationType) {
                case TO_RECEIVER:
                    builder = new ObjectFlowInfoBuilder(dataEndpointFlowInfo.getReceiverFlowInfo());
                    break;
                case TO_RESULT:
                    builder = new ObjectFlowInfoBuilder(dataEndpointFlowInfo.getReturnObjectFlowInfo());
                    break;
                case TO_BOTH_RESULT_AND_RECEIVER:
                    builder = new ObjectFlowInfoBuilder(dataEndpointFlowInfo.getReceiverFlowInfo(),
                            dataEndpointFlowInfo.getReturnObjectFlowInfo());
                    break;
                case TO_DATASOURCE:
                    builder = new ObjectFlowInfoBuilder(dataEndpointFlowInfo.getDataSourceFlowInfo());
                    break;
                default:
                    return Validate.fail("Not known propagation type %s", flowPropagationType);
            }
            return builder;
        }

        @Nonnull
        public MethodEffectsDescriptionBuilder buildEndpointFlowInfo() {
            return MethodEffectsDescriptionBuilder.this;
        }

        public class ObjectFlowInfoBuilder {
            private final List<ObjectFlowInfo> objectFlowInfos;

            private ObjectFlowInfoBuilder(@Nonnull final ObjectFlowInfo... objectFlowInfos) {
                this.objectFlowInfos = Arrays.asList(objectFlowInfos);
            }

            @Nonnull
            public ObjectFlowInfoBuilder addFieldMapping(@Nonnull final String fieldName,
                                                         @Nullable final Object mappingObject) {
                Validate.notNull(fieldName);
                foreach(objectFlowInfo -> objectFlowInfo.addFieldMapping(fieldName, mappingObject));
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addArgumentFieldMapping(final int argumentIndex,
                                                                 @Nonnull final String fieldName,
                                                                 @Nullable final Object mappingObject) {
                Validate.notNull(fieldName);
                foreach(objectFlowInfo -> objectFlowInfo.addArgumentFieldMapping(argumentIndex, fieldName, mappingObject));
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttribute(@Nonnull final IAttributeName attributeName,
                                                      @Nullable final Collection<?> value) {
                Validate.notNull(attributeName);
                if (value != null && !value.isEmpty()) {
                    foreach(objectFlowInfo -> objectFlowInfo.getAttributes().addAll(attributeName, value));
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttribute(@Nonnull final IAttributeName attributeName,
                                                      @Nullable final Collection<?> value,
                                                      @Nullable final Object defaultValue) {
                Validate.notNull(attributeName);
                if (value != null) {
                    addAttribute(attributeName, value);
                } else if (defaultValue != null) {
                    addAttribute(attributeName, defaultValue);
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttribute(@Nonnull final IAttributeName attributeName,
                                                      @Nonnull final Object value) {
                Validate.notNullAll(attributeName, value);
                addAttribute(attributeName, Sets.newHashSet(value));
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttributes(@Nonnull final List<? extends ObjectAttributes> attributeList) {
                Validate.notNull(attributeList);
                for (ObjectAttributes attributes : attributeList) {
                    addAttributes(attributes);
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttributes(@Nullable final ObjectAttributes attributes) {
                if (attributes != null) {
                    foreach(objectFlowInfo -> objectFlowInfo.getAttributes().addAll(attributes));
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttributesExcept(@Nonnull final Collection<? extends ObjectAttributes> attributeList,
                                                             @Nonnull final IAttributeName filterOutAttribute) {
                Validate.notNullAll(attributeList, filterOutAttribute);
                for (ObjectAttributes attributes : attributeList) {
                    addAttributesExcept(attributes, filterOutAttribute);
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addAttributesExcept(@Nullable final ObjectAttributes attributes,
                                                             @Nonnull final IAttributeName filterOutAttribute) {
                Validate.notNull(filterOutAttribute);
                if (attributes != null) {
                    final Set<String> attributeName = Collections.singleton(filterOutAttribute.getAttributeName());
                    foreach(objectFlowInfo -> objectFlowInfo.getAttributes().addAllExcept(attributes, attributeName));
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder addArgumentsAttribute(final int argumentIndex,
                                                               @Nonnull final IAttributeName attributeName,
                                                               @Nullable final Object value) {
                Validate.notNull(attributeName);
                if (value != null) {
                    foreach(objectFlowInfo -> objectFlowInfo.getArgumentAttributes(argumentIndex).addAll(attributeName, Sets.newHashSet(value)));
                }
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder clearAttribute(@Nonnull final IAttributeName attributeName) {
                Validate.notNullAll(attributeName);
                foreach(objectFlowInfo -> objectFlowInfo.getAttributes().getAttributes().compute(
                        attributeName.getAttributeName(),
                        (k, v) -> Sets.newHashSet()));
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder setAttribute(@Nonnull final IAttributeName attributeName,
                                                      @Nonnull final Object value) {
                Validate.notNullAll(attributeName, value);
                foreach(objectFlowInfo -> objectFlowInfo.getAttributes().getAttributes().compute(
                        attributeName.getAttributeName(),
                        (k, v) -> Sets.newHashSet(value)));
                return this;
            }

            @Nonnull
            public ObjectFlowInfoBuilder setAttribute(@Nonnull final IAttributeName attributeName,
                                                      @Nullable final Collection<?> value,
                                                      @Nonnull final Object defaultValue) {
                Validate.notNullAll(attributeName, defaultValue);
                Collection<?> toAdd;
                if (value != null && !value.isEmpty()) {
                    toAdd = value;
                } else {
                    toAdd = Sets.newHashSet(defaultValue);
                }
                foreach(objectFlowInfo -> objectFlowInfo.getAttributes().getAttributes().compute(
                        attributeName.getAttributeName(),
                        (k, v) -> Sets.newHashSet(toAdd)));
                return this;
            }

            @Nonnull
            public DataEndpointFlowInfoBuilder buildFlowInfo() {
                return DataEndpointFlowInfoBuilder.this;
            }

            private void foreach(Consumer<ObjectFlowInfo> consumer) {
                objectFlowInfos.forEach(consumer);
            }
        }
    }

    public class CallbacksBuilder {
        @Nonnull
        public CallbacksBuilder addCallback(@Nonnull final IMethod iMethod) {
            Validate.notNull(iMethod);
            methodEffectsDescription.callbackMethodsToAnalyze.add(iMethod.getSignature());
            return this;
        }

        @Nonnull
        public MethodEffectsDescriptionBuilder buildCallbacks() {
            return MethodEffectsDescriptionBuilder.this;
        }
    }
}
