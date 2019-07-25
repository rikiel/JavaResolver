package eu.profinit.manta.connector.java.analysis.datasource.handler;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.datasource.DataSourceAttributes;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.datasource.handler.DataSourceClassWrappers.EMBEDDED_DATABASE_BUILDER;

public class EmbeddedDatabaseBuilderHandler implements CallHandler {
    private final CallHandlers handlers;

    public EmbeddedDatabaseBuilderHandler() {
        this.handlers = new ClassCallHandlers(this,
                EMBEDDED_DATABASE_BUILDER,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "setName");
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleSetName(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "build");
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleBuild(methodCallDescription);
                    }
                });
    }

    @Override
    public boolean canHandle(@Nonnull IMethod iMethod) {
        return handlers.canHandle(iMethod);
    }

    @Nonnull
    @Override
    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
        return handlers.handle(methodCallDescription);
    }

    /**
     * See org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder#setName(String)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleSetName(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_BOTH_RESULT_AND_RECEIVER)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttribute(DataSourceAttributes.DATABASE_NAME, methodCallDescription.getArgumentAttributes(0).getAttribute(Attributes.CONSTANT_VALUE))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * @param methodCallDescription method call description
     * @return method effects description builder
     * See org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder#build()
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleBuild(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
        return MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT)
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_CONNECTION_TYPE, DatabaseType.getClassNames())
                .addAttribute(Attributes.DATABASE_CONNECTION_USER_NAME, DatabaseType.getUsernames())
                .addAttribute(Attributes.DATABASE_CONNECTION_URL,
                        methodCallDescription.getReceiverAttributes().<String>getAttribute(DataSourceAttributes.DATABASE_NAME).stream()
                                .map(DatabaseType::getUrls)
                                .flatMap(Set::stream)
                                .collect(Collectors.toSet()))
                .buildFlowInfo()
                .buildEndpointFlowInfo();
    }

    /**
     * See org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
     */
    public enum DatabaseType {
        HSQL("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:%s", "sa"),
        H2("org.h2.Driver", "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false", "sa"),
        DERBY("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:memory:%s;create=true", "sa");

        private final String className;
        private final String urlPattern;
        private final String username;

        DatabaseType(String className, String urlPattern, String username) {
            this.className = className;
            this.urlPattern = urlPattern;
            this.username = username;
        }

        @Nonnull
        public static Set<String> getClassNames() {
            return Arrays.stream(values())
                    .map(type -> type.className)
                    .collect(Collectors.toSet());
        }

        @Nonnull
        public static Set<String> getUrls(String databaseName) {
            return Arrays.stream(values())
                    .map(type -> String.format(type.urlPattern, databaseName == null ? "" : databaseName))
                    .collect(Collectors.toSet());
        }

        @Nonnull
        public static Set<String> getUsernames() {
            return Arrays.stream(values())
                    .map(type -> type.username)
                    .collect(Collectors.toSet());
        }
    }
}
