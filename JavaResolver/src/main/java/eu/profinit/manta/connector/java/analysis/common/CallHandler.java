package eu.profinit.manta.connector.java.analysis.common;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

/**
 * Handler for a call
 */
public interface CallHandler {
    boolean canHandle(@Nonnull final IMethod iMethod);

    @Nonnull
    MethodEffectsDescriptionBuilder handle(@Nonnull final MethodCallDescriptionImpl methodCallDescription);

    @Nonnull
    default Set<DataEndpointFlowInfo> getArgumentFlow(@Nonnull final IMethod iMethod, int argumentIndex) {
        return ImmutableSet.of();
    }
}
