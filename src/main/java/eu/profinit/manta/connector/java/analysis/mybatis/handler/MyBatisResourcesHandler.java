package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import java.util.stream.Stream;

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

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.RESOURCES;

public class MyBatisResourcesHandler implements CallHandler {
    private final CallHandlers handlers;

    public MyBatisResourcesHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.handlers = new ClassCallHandlers(this,
                RESOURCES,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return Stream.of(
                                "getResourceURL",
                                "getResourceAsStream",
                                "getResourceAsProperties",
                                "getResourceAsReader",
                                "getResourceAsFile",
                                "getUrlAsStream",
                                "getUrlAsReader",
                                "getUrlAsProperties").anyMatch(methodName -> WalaUtils.isNamed(iMethod, methodName))
                               && WalaUtils.isMatchingArguments(
                                iMethod,
                                new ClassWrapperImpl(String.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleMethodsWithFileName(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return Stream.of(
                                "getResourceURL",
                                "getResourceAsStream",
                                "getResourceAsProperties",
                                "getResourceAsReader",
                                "getResourceAsFile").anyMatch(methodName -> WalaUtils.isNamed(iMethod, methodName))
                               && WalaUtils.isMatchingArguments(
                                iMethod,
                                new ClassWrapperImpl(ClassLoader.class),
                                new ClassWrapperImpl(String.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleMethodsWithClassLoaderAndFileName(methodCallDescription);
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
     * See org.apache.ibatis.io.Resources#getResourceURL(String)
     * See org.apache.ibatis.io.Resources#getResourceAsStream(String)
     * See org.apache.ibatis.io.Resources#getResourceAsProperties(String)
     * See org.apache.ibatis.io.Resources#getResourceAsReader(String)
     * See org.apache.ibatis.io.Resources#getResourceAsFile(String)
     * See org.apache.ibatis.io.Resources#getUrlAsStream(String)
     * See org.apache.ibatis.io.Resources#getUrlAsReader(String)
     * See org.apache.ibatis.io.Resources#getUrlAsProperties(String)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleMethodsWithFileName(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.FILE_NAME, methodCallDescription.getArgumentsAttributes().get(0).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.apache.ibatis.io.Resources#getResourceURL(ClassLoader, String)
     * See org.apache.ibatis.io.Resources#getResourceAsStream(ClassLoader, String)
     * See org.apache.ibatis.io.Resources#getResourceAsProperties(ClassLoader, String)
     * See org.apache.ibatis.io.Resources#getResourceAsReader(ClassLoader, String)
     * See org.apache.ibatis.io.Resources#getResourceAsFile(ClassLoader, String)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleMethodsWithClassLoaderAndFileName(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.FILE_NAME, methodCallDescription.getArgumentsAttributes().get(1).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }
}
