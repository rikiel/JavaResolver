package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAttributes;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.ENVIRONMENT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.TRANSACTION_FACTORY;

public class MyBatisEnvironmentHandler implements CallHandler {
    private final CallHandlers handlers;

    public MyBatisEnvironmentHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                ENVIRONMENT,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return iMethod.isInit()
                               && WalaUtils.isMatchingArguments(
                                iMethod,
                                new ClassWrapperImpl(String.class),
                                TRANSACTION_FACTORY,
                                new ClassWrapperImpl(DataSource.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleConstructor(methodCallDescription);
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
     * See org.apache.ibatis.mapping.Environment#Environment(String, org.apache.ibatis.transaction.TransactionFactory, DataSource)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleConstructor(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RECEIVER)
                .addAttribute(MyBatisAttributes.ENVIRONMENT_NAME, methodCallDescription.getArgumentsAttributes().get(0).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }
}
