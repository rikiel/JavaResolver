package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributes;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.configuration.ConnectionConfiguration;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.configuration.MyBatisConfigurationReader;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;
import eu.profinit.manta.connector.java.model.flowgraph.AttributeValueConstants;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.CONFIGURATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SQL_SESSION_FACTORY_BUILDER;

public class MyBatisSqlSessionFactoryBuilderHandler implements CallHandler {
    private static final Logger log = LoggerFactory.getLogger(MyBatisSqlSessionFactoryBuilderHandler.class);
    private final CallHandlers handlers;
    private final FileContentReader reader;

    public MyBatisSqlSessionFactoryBuilderHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);

        this.reader = new FileContentReader(classMethodCache.getJarFiles());
        this.handlers = new ClassCallHandlers(this,
                SQL_SESSION_FACTORY_BUILDER,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "build")
                               && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(Reader.class),
                                new ClassWrapperImpl(String.class),
                                new ClassWrapperImpl(Properties.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleBuildForReader(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "build") && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(InputStream.class),
                                new ClassWrapperImpl(String.class),
                                new ClassWrapperImpl(Properties.class));
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleBuildForInputStream(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "build")
                               && WalaUtils.isMatchingArguments(iMethod,
                                CONFIGURATION);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleBuildForConfiguration(methodCallDescription);
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
     * See org.apache.ibatis.session.SqlSessionFactoryBuilder#build(Reader, String, Properties)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleBuildForReader(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final Set<String> fileNames = methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.FILE_NAME);

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);
        if (!fileNames.isEmpty()) {
            addConfigurationFileValues(builder, fileNames, getActualEnvironmentAttributes(methodCallDescription.getArgumentAttributes(1)));
        } else {
            addUnknownConfiguration(builder);
        }
        return builder;
    }

    /**
     * See org.apache.ibatis.session.SqlSessionFactoryBuilder#build(InputStream, String, Properties)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleBuildForInputStream(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final Set<String> fileNames = methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.FILE_NAME);

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);
        if (!fileNames.isEmpty()) {
            addConfigurationFileValues(builder, fileNames, getActualEnvironmentAttributes(methodCallDescription.getArgumentAttributes(1)));
        } else {
            addUnknownConfiguration(builder);
        }
        return builder;
    }

    /**
     * See org.apache.ibatis.session.SqlSessionFactoryBuilder#build(org.apache.ibatis.session.Configuration)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleBuildForConfiguration(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final Set<Map<String, String>> dataSourceObject = methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.DATA_SOURCE);

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);
        if (!dataSourceObject.isEmpty()) {
            final DataEndpointFlowInfoBuilder flowInfoBuilder = builder.dataEndpointFlowInfoBuilder();
            dataSourceObject.stream()
                    .map(dataSourceValues -> new ConnectionConfiguration(
                            dataSourceValues.get(Attributes.DATABASE_CONNECTION_TYPE.getAttributeName()),
                            dataSourceValues.get(Attributes.DATABASE_CONNECTION_URL.getAttributeName()),
                            dataSourceValues.get(Attributes.DATABASE_CONNECTION_USER_NAME.getAttributeName())))
                    .forEach(connectionConfiguration -> addConfigurationFileValues(flowInfoBuilder, connectionConfiguration));
        } else {
            addUnknownConfiguration(builder);
        }
        return builder;
    }

    @Nonnull
    private Set<String> getActualEnvironmentAttributes(@Nonnull final ObjectAttributes actualEnvironmentAttributes) {
        final Set<String> environmentAttributes = Sets.newHashSet(MyBatisConfigurationReader.DEFAULT_ENVIRONMENT_NAME);
        environmentAttributes.addAll(actualEnvironmentAttributes.getAttribute(Attributes.CONSTANT_VALUE));
        return environmentAttributes;
    }

    private void addConfigurationFileValues(@Nonnull final MethodEffectsDescriptionBuilder builder,
                                            @Nonnull final Set<String> fileNames,
                                            @Nonnull final Set<String> environmentAttributes) {
        final DataEndpointFlowInfoBuilder flowInfoBuilder = builder.dataEndpointFlowInfoBuilder();
        for (String fileName : fileNames) {
            final Map<String, ConnectionConfiguration> connectionConfigurationMap;
            try {
                connectionConfigurationMap = new MyBatisConfigurationReader(fileName, reader).parseConfigFile();
            } catch (XmlException e) {
                log.error("Failed to read configuration file {}", fileName, e);
                continue;
            }

            for (String environmentName : environmentAttributes) {
                final ConnectionConfiguration usedConfiguration = connectionConfigurationMap.get(environmentName);
                if (usedConfiguration != null) {
                    addConfigurationFileValues(flowInfoBuilder, usedConfiguration);
                }
            }
        }
    }

    private void addConfigurationFileValues(@Nonnull final DataEndpointFlowInfoBuilder builder,
                                            @Nonnull final ConnectionConfiguration connectionConfiguration) {
        builder.objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_TYPE, connectionConfiguration.getType())
                .addAttribute(Attributes.DATABASE_CONNECTION_URL, connectionConfiguration.getConnectionUrl())
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, connectionConfiguration.getUserName());
    }

    private void addUnknownConfiguration(@Nonnull final MethodEffectsDescriptionBuilder builder) {
        builder.dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_URL, AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT);
    }
}
