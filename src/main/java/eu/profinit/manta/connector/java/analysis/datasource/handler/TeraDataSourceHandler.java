package eu.profinit.manta.connector.java.analysis.datasource.handler;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.datasource.handler.DataSourceClassWrappers.TERA_DATA_SOURCE;

public class TeraDataSourceHandler implements CallHandler {
    private final CallHandlers handlers;

    public TeraDataSourceHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                TERA_DATA_SOURCE,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "setuser")
                               && WalaUtils.isMatchingArguments(iMethod, new ClassWrapperImpl(String.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSetUser(methodCallDescription);
                    }
                });
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
     * See com.teradata.jdbc.TeraDataSourceBase#setuser(String)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSetUser(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }
}
