package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.utils.CollectionToString;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

/**
 * Model for storing data for invocation of some {@link #iMethod}.
 * {@link #receiverAttributes} are attributes of called class
 * {@link #argumentsAttributes} are attributes of all arguments
 * {@link #returnType} is the result type
 * <p>
 * For instance for calling {@link MethodCallDescriptionImpl#setMethod(IMethod)}, fields will be filled as:
 * <ul>
 * <li>{@link #iMethod} = {@link MethodCallDescriptionImpl#setMethod(IMethod)}</li>
 * <li>{@link #receiverAttributes} = attributes of object {@link MethodCallDescriptionImpl}</li>
 * <li>{@link #argumentsAttributes} = attributes for argument {@link IMethod} (receiver attribute, if present is skipped)</li>
 * <li>{@link #returnType} = {@link MethodCallDescriptionImpl}</li>
 * </ul>
 *
 * @see eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodCallDescription original class
 */
public class MethodCallDescriptionImpl {
    private IMethod iMethod;
    private ObjectAttributesInput receiverAttributes;
    private List<ObjectAttributesInput> argumentsAttributes = Lists.newArrayList();
    private IClass returnType;
    private Map<IMethod, List<DataEndpointFlowInfo>> computedCallbacks = Maps.newHashMap();

    @Nonnull
    public IMethod getMethod() {
        return iMethod;
    }

    public void setMethod(IMethod iMethod) {
        this.iMethod = iMethod;
    }

    @Nonnull
    public ObjectAttributesInput getReceiverAttributes() {
        return receiverAttributes;
    }

    public void setReceiverAttributes(ObjectAttributesInput receiverAttributes) {
        this.receiverAttributes = receiverAttributes;
    }

    @Nonnull
    public List<ObjectAttributesInput> getArgumentsAttributes() {
        return argumentsAttributes;
    }

    public void setArgumentsAttributes(List<ObjectAttributesInput> argumentsAttributes) {
        this.argumentsAttributes = argumentsAttributes;
    }

    @Nonnull
    public ObjectAttributesInput getArgumentAttributes(int index) {
        return argumentsAttributes.get(index);
    }

    @Nonnull
    public IClass getReturnType() {
        return returnType;
    }

    public void setReturnType(IClass returnType) {
        this.returnType = returnType;
    }

    @Nonnull
    public Map<IMethod, List<DataEndpointFlowInfo>> getComputedCallbacks() {
        return computedCallbacks;
    }

    public void setComputedCallbacks(Map<IMethod, List<DataEndpointFlowInfo>> computedCallbacks) {
        this.computedCallbacks = ImmutableMap.copyOf(computedCallbacks);
    }

    public boolean hasEmptyAttributes() {
        return receiverAttributes.getAttributes().isEmpty()
               && argumentsAttributes.stream().allMatch(attribute -> attribute.getAttributes().isEmpty());
    }

    @Nonnull
    public Set<Integer> getAttributesVariableIds() {
        return IntStream.range(0, getArgumentsAttributes().size()).boxed()
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("iMethod", iMethod)
                .add("receiverAttributes", receiverAttributes)
                .add("argumentsAttributes", argumentsAttributes)
                .add("returnType", returnType)
                .add("computedCallbacks", new CollectionToString<>(
                        computedCallbacks.entrySet(),
                        entry -> entry.getKey() + "=" + entry.getValue().stream().map(ReflectionToStringBuilder::new).collect(Collectors.toList())))
                .toString();
    }
}
