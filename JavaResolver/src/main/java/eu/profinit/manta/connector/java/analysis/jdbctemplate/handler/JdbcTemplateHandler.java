package eu.profinit.manta.connector.java.analysis.jdbctemplate.handler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassCallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_DATASOURCE;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CALLABLE_STATEMENT_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CALLABLE_STATEMENT_CREATOR;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CONNECTION_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.JDBC_TEMPLATE;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.PREPARED_STATEMENT_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.PREPARED_STATEMENT_CREATOR;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.ROW_CALLBACK_HANDLER;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.ROW_MAPPER;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.STATEMENT_CALLBACK;

public class JdbcTemplateHandler implements CallHandler {
    private final ClassMethodCache classMethodCache;
    private final CallHandlers handlers;

    private final Map<IMethod, Set<DataEndpointFlowInfo>> callbacksFlowInfo = Maps.newHashMap();

    public JdbcTemplateHandler(@Nonnull final ClassMethodCache classMethodCache) {
        this.classMethodCache = classMethodCache;
        this.handlers = new ClassCallHandlers(this,
                JDBC_TEMPLATE,
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "query") && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(String.class),
                                ROW_MAPPER);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleQueryWithRowMapper(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "query") && WalaUtils.isMatchingArguments(iMethod,
                                new ClassWrapperImpl(String.class),
                                ROW_CALLBACK_HANDLER);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleQueryWithRowCallbackHandler(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "execute") && WalaUtils.isMatchingArguments(iMethod,
                                CONNECTION_CALLBACK);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleExecuteConnectionCallback(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "execute") && WalaUtils.isMatchingArguments(iMethod,
                                STATEMENT_CALLBACK);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleExecuteStatementCallback(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "execute") && WalaUtils.isMatchingArguments(iMethod,
                                PREPARED_STATEMENT_CREATOR,
                                PREPARED_STATEMENT_CALLBACK);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleExecutePreparedStatementCreatorWithPreparedStatementCallback(methodCallDescription);
                    }
                },
                new CallHandler() {
                    @Override
                    public boolean canHandle(@Nonnull IMethod iMethod) {
                        return WalaUtils.isNamed(iMethod, "execute") && WalaUtils.isMatchingArguments(iMethod,
                                CALLABLE_STATEMENT_CREATOR,
                                CALLABLE_STATEMENT_CALLBACK);
                    }

                    @Nonnull
                    @Override
                    public MethodEffectsDescriptionBuilder handle(@Nonnull MethodCallDescriptionImpl methodCallDescription) {
                        return handleExecuteCallableStatementCreatorWithCallableStatementCallback(methodCallDescription);
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

    @Nonnull
    @Override
    public Set<DataEndpointFlowInfo> getArgumentFlow(@Nonnull final IMethod iMethod, final int argumentIndex) {
        return callbacksFlowInfo.getOrDefault(iMethod, ImmutableSet.of());
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.ConnectionCallback)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleExecuteConnectionCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod connectionCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(0).getType(),
                "doInConnection",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Connection.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallback(builder, methodCallDescription, connectionCallbackMethod);

        return builder;
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.StatementCallback)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleExecuteStatementCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod statementCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(0).getType(),
                "doInStatement",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(Statement.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallback(builder, methodCallDescription, statementCallbackMethod);

        return builder;
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.PreparedStatementCreator, org.springframework.jdbc.core.PreparedStatementCallback)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleExecutePreparedStatementCreatorWithPreparedStatementCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod preparedStatementCreatorCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(0).getType(),
                "createPreparedStatement",
                new ClassWrapperImpl(PreparedStatement.class),
                new ClassWrapperImpl(Connection.class));

        final IMethod preparedStatementCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(1).getType(),
                "doInPreparedStatement",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(PreparedStatement.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallbacks(builder, methodCallDescription, preparedStatementCreatorCallbackMethod, preparedStatementCallbackMethod);

        return builder;
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.CallableStatementCreator, org.springframework.jdbc.core.CallableStatementCallback)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleExecuteCallableStatementCreatorWithCallableStatementCallback(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod callableStatementCreatorCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(0).getType(),
                "createCallableStatement",
                new ClassWrapperImpl(CallableStatement.class),
                new ClassWrapperImpl(Connection.class));

        final IMethod callableStatementCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(1).getType(),
                "doInCallableStatement",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(CallableStatement.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallbacks(builder, methodCallDescription, callableStatementCreatorCallbackMethod, callableStatementCallbackMethod);

        return builder;
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#query(String, org.springframework.jdbc.core.RowMapper)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleQueryWithRowMapper(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod mapRowCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(1).getType(),
                "mapRow",
                new ClassWrapperImpl(Object.class),
                new ClassWrapperImpl(ResultSet.class),
                new ClassWrapperImpl(int.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallback(builder, methodCallDescription, mapRowCallbackMethod);

        return builder;
    }

    /**
     * See org.springframework.jdbc.core.JdbcTemplate#query(String, org.springframework.jdbc.core.RowCallbackHandler)
     */
    @Nonnull
    private MethodEffectsDescriptionBuilder handleQueryWithRowCallbackHandler(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final IMethod processRowCallbackMethod = WalaUtils.findMethod(classMethodCache,
                methodCallDescription.getArgumentAttributes(1).getType(),
                "processRow",
                new ClassWrapperImpl(void.class),
                new ClassWrapperImpl(ResultSet.class));

        final MethodEffectsDescriptionBuilder builder = MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_RESULT);

        handleCallback(builder, methodCallDescription, processRowCallbackMethod);

        return builder;
    }

    private void handleCallback(@Nonnull final MethodEffectsDescriptionBuilder builder,
                                @Nonnull final MethodCallDescriptionImpl methodCallDescription,
                                @Nonnull final IMethod iMethod) {
        // handle callback
        final List<DataEndpointFlowInfo> callbackResults = methodCallDescription.getComputedCallbacks().getOrDefault(iMethod, ImmutableList.of());
        if (callbackResults.isEmpty()) {
            builder
                    .callbacksBuilder()
                    .addCallback(iMethod);
        } else {
            builder
                    .addResultFlowInfos(callbackResults);
        }

        // store flow for callback
        callbacksFlowInfo.computeIfAbsent(iMethod, method -> Sets.newHashSet())
                .addAll(MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_DATASOURCE).buildMethodEffectsDescription().resultFlowInfos);
    }

    private void handleCallbacks(@Nonnull final MethodEffectsDescriptionBuilder builder,
                                 @Nonnull final MethodCallDescriptionImpl methodCallDescription,
                                 @Nonnull final IMethod callback1,
                                 @Nonnull final IMethod callback2) {
        final List<DataEndpointFlowInfo> callback1Results = methodCallDescription.getComputedCallbacks().getOrDefault(callback1, ImmutableList.of());
        final List<DataEndpointFlowInfo> callback2Results = methodCallDescription.getComputedCallbacks().getOrDefault(callback2, ImmutableList.of());

        // handle callbacks
        if (callback1Results.isEmpty()) {
            builder.callbacksBuilder()
                    .addCallback(callback1);
        } else if (callback2Results.isEmpty()) {
            builder.callbacksBuilder()
                    .addCallback(callback1)
                    .addCallback(callback2);
        } else {
            builder
                    .addResultFlowInfos(callback2Results);
        }

        // store flow for callbacks
        callbacksFlowInfo.computeIfAbsent(callback1, method -> Sets.newHashSet())
                .addAll(MethodEffectsDescriptionBuilder.propagateFlow(methodCallDescription, TO_DATASOURCE).buildMethodEffectsDescription().resultFlowInfos);
        callbacksFlowInfo.computeIfAbsent(callback2, method -> Sets.newHashSet())
                .addAll(callback1Results);
    }
}
