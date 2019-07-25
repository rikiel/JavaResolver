package eu.profinit.manta.connector.java.analysis.jdbctemplate.handler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import javax.annotation.Nonnull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.WalaAnalysisTestUtils;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin.MethodEffectsDescription;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CALLABLE_STATEMENT_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CALLABLE_STATEMENT_CREATOR;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.CONNECTION_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.JDBC_TEMPLATE;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.PREPARED_STATEMENT_CALLBACK;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.PREPARED_STATEMENT_CREATOR;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.STATEMENT_CALLBACK;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class JdbcTemplateHandlerTest extends AbstractTest {
    private JdbcTemplateHandler handler;
    private ClassMethodCache classMethodCache;

    @BeforeClass
    public void init() {
        final IClassHierarchy iClassHierarchy = WalaAnalysisTestUtils.constructClassHierarchy(null, WalaAnalysisTestUtils.JDBC_TEMPLATE_SCOPE);

        classMethodCache = new ClassMethodCache(iClassHierarchy);
        handler = new JdbcTemplateHandler(classMethodCache);
    }

    @Test(dataProvider = "testHandleDataProvider")
    public void testHandle(MethodCallDescriptionImpl methodCallDescription, MethodEffectsDescription expectedResult) {
        MethodEffectsDescription actualResult = handler.handle(methodCallDescription).buildMethodEffectsDescription();
        assertReflectionEquals(expectedResult, actualResult);
    }

    @DataProvider
    private Object[][] testHandleDataProvider() {
        return new Object[][] {
                { createForExecuteConnectionCallback(), createForExecuteConnectionCallbackExpectedResult() },
                { createForExecuteConnectionCallbackWithCallbackComputed(), createForExecuteConnectionCallbackWithCallbackComputedExpectedResult() },
                { createForExecuteStatementCallback(), createForExecuteStatementCallbackExpectedResult() },
                { createForExecuteStatementCallbackWithCallbackComputed(), createForExecuteStatementCallbackWithCallbackComputedExpectedResult() },
                { createForExecutePreparedStatementCreatorWithPreparedStatementCallback(),
                  createForExecutePreparedStatementCreatorWithPreparedStatementCallbackExpectedResult() },
                { createForExecutePreparedStatementCreatorWithPreparedStatementCallbackWithCallbackComputed(),
                  createForExecutePreparedStatementCreatorWithPreparedStatementCallbackWithCallbackComputedExpectedResult() },
                { createForExecuteCallableStatementCreatorWithCallableStatementCallback(),
                  createForExecuteCallableStatementCreatorWithCallableStatementCallbackExpectedResult() },
                { createForExecuteCallableStatementCreatorWithCallableStatementCallbackWithCallbackComputed(),
                  createForExecuteCallableStatementCreatorWithCallableStatementCallbackWithCallbackComputedExpectedResult() },
                };
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteConnectionCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteConnectionCallback() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                CONNECTION_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(CONNECTION_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteConnectionCallbackExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CONNECTION_CALLBACK,
                        "doInConnection",
                        new ClassWrapperImpl(Object.class),
                        new ClassWrapperImpl(Connection.class)))
                .buildCallbacks()
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteConnectionCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteConnectionCallbackWithCallbackComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                CONNECTION_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(CONNECTION_CALLBACK),
                        ImmutableMap.of(Attributes.DATABASE_QUERY.getAttributeName(), Sets.newHashSet("SQL Statement")))
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        methodCallDescription.setComputedCallbacks(
                ImmutableMap.of(
                        WalaUtils.findMethod(classMethodCache,
                                CONNECTION_CALLBACK,
                                "doInConnection",
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(Connection.class)),
                        getCallbackResult())
        );
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteConnectionCallbackWithCallbackComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_QUERY, Sets.newHashSet("SQL Statement"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteStatementCallback() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(STATEMENT_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteStatementCallbackExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        STATEMENT_CALLBACK,
                        "doInStatement",
                        new ClassWrapperImpl(Object.class),
                        new ClassWrapperImpl(Statement.class)))
                .buildCallbacks()
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteStatementCallbackWithCallbackComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(STATEMENT_CALLBACK),
                        ImmutableMap.of(Attributes.DATABASE_QUERY.getAttributeName(), Sets.newHashSet("SQL Statement")))
        ));
        methodCallDescription.setComputedCallbacks(
                ImmutableMap.of(
                        WalaUtils.findMethod(classMethodCache,
                                STATEMENT_CALLBACK,
                                "doInStatement",
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(Statement.class)),
                        getCallbackResult())
        );
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteStatementCallbackWithCallbackComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .objectFlowInfoBuilder(TO_RESULT)
                .addAttribute(Attributes.DATABASE_QUERY, Sets.newHashSet("SQL Statement"))
                .buildFlowInfo()
                .buildEndpointFlowInfo()
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecutePreparedStatementCreatorWithPreparedStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecutePreparedStatementCreatorWithPreparedStatementCallback() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                PREPARED_STATEMENT_CREATOR,
                PREPARED_STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(PREPARED_STATEMENT_CREATOR),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(PREPARED_STATEMENT_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecutePreparedStatementCreatorWithPreparedStatementCallbackExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        PREPARED_STATEMENT_CREATOR,
                        "createPreparedStatement",
                        new ClassWrapperImpl(PreparedStatement.class),
                        new ClassWrapperImpl(Connection.class)))
                .buildCallbacks()
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecutePreparedStatementCreatorWithPreparedStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecutePreparedStatementCreatorWithPreparedStatementCallbackWithCallbackComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                PREPARED_STATEMENT_CREATOR,
                PREPARED_STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(PREPARED_STATEMENT_CREATOR),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(PREPARED_STATEMENT_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        methodCallDescription.setComputedCallbacks(
                ImmutableMap.of(
                        WalaUtils.findMethod(classMethodCache,
                                PREPARED_STATEMENT_CREATOR,
                                "createPreparedStatement",
                                new ClassWrapperImpl(PreparedStatement.class),
                                new ClassWrapperImpl(Connection.class)),
                        getCallbackResult(),

                        WalaUtils.findMethod(classMethodCache,
                                PREPARED_STATEMENT_CALLBACK,
                                "doInPreparedStatement",
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(PreparedStatement.class)),
                        getCallbackResult())
        );
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecutePreparedStatementCreatorWithPreparedStatementCallbackWithCallbackComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteCallableStatementCreatorWithCallableStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteCallableStatementCreatorWithCallableStatementCallback() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                CALLABLE_STATEMENT_CREATOR,
                CALLABLE_STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(CALLABLE_STATEMENT_CREATOR),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(CALLABLE_STATEMENT_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(
                classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteCallableStatementCreatorWithCallableStatementCallbackExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .callbacksBuilder()
                .addCallback(WalaUtils.findMethod(classMethodCache,
                        CALLABLE_STATEMENT_CREATOR,
                        "createCallableStatement",
                        new ClassWrapperImpl(CallableStatement.class),
                        new ClassWrapperImpl(Connection.class)))
                .buildCallbacks()
                .buildMethodEffectsDescription();
    }

    /**
     * @see JdbcTemplateHandler#handleExecuteCallableStatementCreatorWithCallableStatementCallback(MethodCallDescriptionImpl)
     */
    @Nonnull
    private MethodCallDescriptionImpl createForExecuteCallableStatementCreatorWithCallableStatementCallbackWithCallbackComputed() {
        final MethodCallDescriptionImpl methodCallDescription = new MethodCallDescriptionImpl();
        methodCallDescription.setMethod(WalaUtils.findMethod(classMethodCache,
                JDBC_TEMPLATE,
                "execute",
                new ClassWrapperImpl(Object.class),
                CALLABLE_STATEMENT_CREATOR,
                CALLABLE_STATEMENT_CALLBACK));
        methodCallDescription.setArgumentsAttributes(Lists.newArrayList(
                new ObjectAttributesInput(classMethodCache.findClass(CALLABLE_STATEMENT_CREATOR),
                        ImmutableMap.of()),
                new ObjectAttributesInput(classMethodCache.findClass(CALLABLE_STATEMENT_CALLBACK),
                        ImmutableMap.of())
        ));
        methodCallDescription.setReceiverAttributes(new ObjectAttributesInput(classMethodCache.findClass(new ClassWrapperImpl(Object.class)),
                ImmutableMap.of()));
        methodCallDescription.setComputedCallbacks(
                ImmutableMap.of(
                        WalaUtils.findMethod(classMethodCache,
                                CALLABLE_STATEMENT_CREATOR,
                                "createCallableStatement",
                                new ClassWrapperImpl(CallableStatement.class),
                                new ClassWrapperImpl(Connection.class)),
                        getCallbackResult(),

                        WalaUtils.findMethod(classMethodCache,
                                CALLABLE_STATEMENT_CALLBACK,
                                "doInCallableStatement",
                                new ClassWrapperImpl(Object.class),
                                new ClassWrapperImpl(CallableStatement.class)),
                        getCallbackResult())
        );
        return methodCallDescription;
    }

    @Nonnull
    private MethodEffectsDescription createForExecuteCallableStatementCreatorWithCallableStatementCallbackWithCallbackComputedExpectedResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .addResultFlowInfos(getCallbackResult())
                .buildMethodEffectsDescription();
    }

    @Nonnull
    private List<DataEndpointFlowInfo> getCallbackResult() {
        return MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql("Some SQL", DataEndpointFlowInfoImpl.QueryType.QUERY)
                .buildEndpointFlowInfo()
                .buildMethodEffectsDescription().resultFlowInfos;
    }
}