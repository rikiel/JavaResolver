package eu.profinit.manta.connector.java.analysis.common.plugin.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodCallDescription;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

public class MethodCallDescriptionTransformer {
    private final ClassMethodCache classMethodCache;

    public MethodCallDescriptionTransformer(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);
        this.classMethodCache = classMethodCache;
    }

    @Nonnull
    public MethodCallDescriptionImpl transform(@Nonnull final MethodCallDescription input) {
        validate(input);

        final MethodCallDescriptionImpl result = new MethodCallDescriptionImpl();
        result.setMethod(input.methodObj);
        result.setReceiverAttributes(transformAttributes(input.receiverObjTypeName, input.receiverAttrs));
        result.setArgumentsAttributes(IntStream.range(0, input.argumentTypeNames.size())
                .mapToObj(i -> transformAttributes(input.argumentTypeNames.get(i), input.argumentAttrs.get(i)))
                .collect(Collectors.toList()));
        result.setReturnType(classMethodCache.findClass(new ClassWrapperImpl(input.returnTypeName)));
        result.setComputedCallbacks(input.callbackMthSig2FlowInfo.entrySet().stream()
                .collect(Collectors.toMap(entry -> classMethodCache.findMethod(entry.getKey()), Map.Entry::getValue)));

        return result;
    }

    @SuppressWarnings("RedundantTypeArguments")
    private void validate(@Nonnull final MethodCallDescription input) {
        Validate.notNull(input);
        Validate.notNull(input.methodObj);

        input.receiverAttrs = defaultIfNull(input.receiverAttrs, Collections.<String, Set<Object>>emptyMap());
        input.argumentTypeNames = defaultIfNull(input.argumentTypeNames, Collections.<String>emptyList());
        input.argumentAttrs = defaultIfNull(input.argumentAttrs, Collections.<Map<String, Set<Object>>>emptyList());
        input.callbackMthSig2FlowInfo = defaultIfNull(input.callbackMthSig2FlowInfo, Collections.<String, List<DataEndpointFlowInfo>>emptyMap());

        Validate.equals(input.argumentTypeNames.size(), input.argumentAttrs.size(),
                "Argument types/attributes list size does not match!");
        Validate.equals(input.argumentTypeNames.size(), WalaUtils.getArguments(input.methodObj).size(),
                "Argument types/attributes list size does not match!");
    }

    @Nonnull
    private <T> T defaultIfNull(@Nullable final T value, @Nonnull final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    @Nonnull
    private ObjectAttributesInput transformAttributes(String typeName, Map<String, Set<Object>> attributes) {
        return new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(typeName)), attributes);
    }
}
