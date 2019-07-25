package eu.profinit.manta.connector.java.analysis.common;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

/**
 * Collection for a call handlers
 */
public class CallHandlers implements CallHandler {
    private static final Logger log = LoggerFactory.getLogger(CallHandlers.class);

    private final List<CallHandler> handlers;
    private final String callerName;

    public CallHandlers(@Nonnull final Object callerObject,
                        @Nonnull final CallHandler... handlers) {
        Validate.notNullAll(callerObject, handlers);
        this.handlers = ImmutableList.copyOf(handlers);
        this.callerName = callerObject.getClass().getSimpleName();
    }

    @Override
    public boolean canHandle(@Nonnull final IMethod iMethod) {
        return handlers.stream()
                .anyMatch(handler -> handler.canHandle(iMethod));
    }

    @Nonnull
    @Override
    public MethodEffectsDescriptionBuilder handle(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        Validate.notNull(methodCallDescription);

        final Handler handler = getHandler(methodCallDescription);

        logUseHandler(handler);

        final MethodEffectsDescriptionBuilder result = handler.getCallHandler().handle(methodCallDescription);

        logResult(methodCallDescription, result, handler);

        return result;
    }

    @Nonnull
    @Override
    public Set<DataEndpointFlowInfo> getArgumentFlow(@Nonnull final IMethod iMethod, final int argumentIndex) {
        return handlers.stream()
                .map(handler -> handler.getArgumentFlow(iMethod, argumentIndex))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    protected void logUseHandler(@Nonnull final Handler handler) {
        log.debug("Using handler {}#{} to compute result", callerName, handler.getId());
    }

    protected void logResult(@Nonnull final MethodCallDescriptionImpl methodCallDescription,
                             @Nonnull final MethodEffectsDescriptionBuilder result,
                             @Nonnull final Handler handler) {
        log.debug("Method call {} was processed by handler {}#{}:", methodCallDescription, callerName, handler.getId());
        log.debug("Result was computed {}", new ReflectionToStringBuilder(result.buildMethodEffectsDescription()));
    }

    @Nonnull
    private Handler getHandler(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final List<Integer> supportedHandlersIndices = IntStream.range(0, handlers.size())
                .filter(i -> handlers.get(i).canHandle(methodCallDescription.getMethod()))
                .boxed()
                .collect(Collectors.toList());

        Validate.validState(supportedHandlersIndices.size() == 1, "Should find 1 handler for invoking %s. Found handlers from %s with indices %s",
                methodCallDescription,
                callerName,
                supportedHandlersIndices);

        final int handlerIndex = supportedHandlersIndices.get(0);

        return new Handler(handlers, handlerIndex);
    }

    protected static class Handler {
        private final CallHandler callHandler;
        private final Object id;

        public Handler(final List<CallHandler> callHandlers, final int handlerIndex) {
            this.callHandler = callHandlers.get(handlerIndex);
            this.id = callHandler.getClass().isAnonymousClass()
                    ? handlerIndex
                    : callHandler.getClass().getSimpleName();
        }

        public Object getId() {
            return id;
        }

        public CallHandler getCallHandler() {
            return callHandler;
        }
    }
}
