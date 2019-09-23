package eu.profinit.manta.connector.java.analysis.common;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.transformer.MethodCallDescriptionTransformer;
import eu.profinit.manta.connector.java.analysis.utils.CollectionToString;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;

/**
 * Common ancestor for plugins
 */
public abstract class AbstractAnalysisPlugin implements FrameworkAnalysisPlugin {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static ClassMethodCache classMethodCache;

    private MethodCallDescriptionTransformer inputTransformer;
    private CallHandlers callHandlers;
    private boolean enabled;

    /**
     * @param classMethodCache Cache
     * @return Returns handlers for plugin
     */
    @Nonnull
    protected abstract CallHandlers getCallHandlers(@Nonnull final ClassMethodCache classMethodCache);

    /**
     * @return Returns methods that symbolic analysis library should analyse from framework
     * @throws NoSuchMethodException when methods were not found in {@link #classMethodCache}.
     */
    @Nonnull
    protected abstract List<IMethod> getMethodsToAnalyse() throws NoSuchMethodException;

    @Override
    public final void initialize(IClassHierarchy iClassHierarchy, CallGraph callGraph) {
        Validate.notNullAll(iClassHierarchy, callGraph);

        log.info("Initializing plugin");

        synchronized (this) {
            if (classMethodCache == null || classMethodCache.getClassHierarchy() != iClassHierarchy) {
                classMethodCache = new ClassMethodCache(iClassHierarchy);
            }
        }
        callHandlers = getCallHandlers(classMethodCache);
        inputTransformer = new MethodCallDescriptionTransformer(classMethodCache);
        enabled = true;
    }

    @Override
    public final void analyzeProgram() {
        // nothing to do
    }

    @Override
    public final boolean canHandleMethod(IMethod iMethod) {
        try {
            return enabled && callHandlers.canHandle(iMethod);
        } catch (RuntimeException e) {
            log.error("Plugin throws exception in canHandleMethod(). Returning false", e);
            return false;
        }
    }

    @Override
    public final MethodEffectsDescription processMethodCall(MethodCallDescription methodCallDescription) {
        final MethodCallDescriptionImpl transformed = inputTransformer.transform(methodCallDescription);
        try {
            return callHandlers.handle(transformed).buildMethodEffectsDescription();
        } catch (RuntimeException e) {
            log.error("Plugin throws exception in processMethodCall(). Returning identity", e);
            return MethodEffectsDescriptionBuilder.propagateFlow(transformed, TO_BOTH_RESULT_AND_RECEIVER).buildMethodEffectsDescription();
        }
    }

    @Override
    public final Set<String> getAdditionalMethodsRequiredForAnalysis() {
        try {
            final List<IMethod> methodsToAnalyse = getMethodsToAnalyse();
            final List<IMethod> handledMethods = getMethodsToAnalyse().stream()
                    .filter(this::canHandleMethod)
                    .collect(Collectors.toList());
            methodsToAnalyse.removeAll(handledMethods);

            log.debug("Methods to analyze: {}", new CollectionToString<>(methodsToAnalyse, IMethod::getSignature));
            log.debug("Filtered out methods that can be handled by plugin: {}", new CollectionToString<>(handledMethods, IMethod::getSignature));

            return methodsToAnalyse.stream()
                    .map(IMethod::getSignature)
                    .collect(Collectors.toSet());
        } catch (NoSuchMethodException | RuntimeException e) {
            log.debug("Disabling plugin {} as some framework classes were not found.", getClass().getSimpleName());
            enabled = false;
            return ImmutableSet.of();
        }
    }

    @Override
    public final boolean canProvideArgument(@Nonnull final IMethod iMethod, final int argIndex) {
        Validate.notNull(iMethod);

        try {
            return !callHandlers.getArgumentFlow(iMethod, argIndex).isEmpty();
        } catch (RuntimeException e) {
            log.error("Plugin throws exception in canProvideArgument(). Returning false", e);
            return false;
        }
    }

    @Override
    public List<DataEndpointFlowInfo> getArgumentFlowInformation(@Nonnull final IMethod iMethod, final int argIndex) {
        Validate.notNull(iMethod);

        final Set<DataEndpointFlowInfo> argumentFlow = callHandlers.getArgumentFlow(iMethod, argIndex);
        Validate.isFalse(argumentFlow.isEmpty(), "Argument flow does not exist!");

        log.info("Argument flow information for argument {} of method {} -> {}", argIndex, iMethod, argumentFlow);
        return ImmutableList.copyOf(argumentFlow);
    }

    @Override
    public final void destroy() {
        log.info("Destroying plugin");

        inputTransformer = null;
        classMethodCache = null;
        callHandlers = null;
        enabled = false;
    }
}
