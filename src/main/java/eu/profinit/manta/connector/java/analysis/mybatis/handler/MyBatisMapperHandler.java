package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.ClassLoaderReference;

import eu.profinit.manta.connector.java.analysis.common.CallHandler;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.StrictHashMap;
import eu.profinit.manta.connector.java.analysis.common.plugin.DataEndpointFlowInfoImpl.QueryType;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.DataEndpointFlowInfoBuilder.ObjectFlowInfoBuilder;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAttributes;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisClassAnalyzer;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils.MyBatisSqlVariable;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlMapping;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;
import eu.profinit.manta.connector.java.resolver.TaskExecutor;

import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_BOTH_RESULT_AND_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RECEIVER;
import static eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder.FlowPropagationType.TO_RESULT;

public class MyBatisMapperHandler implements CallHandler {
    private static final Map<CommandType, QueryType> QUERY_TYPE = new StrictHashMap<>(ImmutableMap.<CommandType, QueryType>builder()
            .put(CommandType.SELECT, QueryType.QUERY)
            .put(CommandType.DELETE, QueryType.UPDATE)
            .put(CommandType.INSERT, QueryType.UPDATE)
            .put(CommandType.UPDATE, QueryType.UPDATE)
            .build());

    private final Map<IMethod, SqlCommand> handlingMethodsResult;

    public MyBatisMapperHandler(@Nonnull final ClassMethodCache classMethodCache) {
        Validate.notNull(classMethodCache);
        this.handlingMethodsResult = TaskExecutor.compute("Analyse MyBatis handling methods", () -> computeHandlingMethods(classMethodCache));
    }

    @Override
    public boolean canHandle(@Nonnull final IMethod iMethod) {
        return handlingMethodsResult.containsKey(iMethod);
    }

    @Nonnull
    @Override
    public MethodEffectsDescriptionBuilder handle(@Nonnull final MethodCallDescriptionImpl methodCallDescription) {
        final SqlCommand sqlCommand = handlingMethodsResult.get(methodCallDescription.getMethod());
        Validate.notNull(sqlCommand, "Cannot handle method call %s!", methodCallDescription);

        final String sqlWithSubstitutedArguments = MyBatisUtils.getSqlWithSubstitutedArguments(sqlCommand.getPlainSql(),
                methodCallDescription.getMethod(),
                methodCallDescription.getArgumentsAttributes());
        final DataEndpointFlowInfoBuilder builder = MethodEffectsDescriptionBuilder.newFlow()
                .dataEndpointFlowInfoBuilder()
                .addSql(sqlWithSubstitutedArguments, QUERY_TYPE.get(sqlCommand.getCommandType()))
                .objectFlowInfoBuilder(TO_BOTH_RESULT_AND_RECEIVER)
                .addAttributesExcept(methodCallDescription.getArgumentsAttributes(), Attributes.DATABASE_QUERY)
                .addAttributesExcept(methodCallDescription.getReceiverAttributes(), Attributes.DATABASE_QUERY)
                .addAttribute(Attributes.DATABASE_QUERY, sqlWithSubstitutedArguments)
                .addAttribute(Attributes.VAR_ID, methodCallDescription.getAttributesVariableIds())
                .setAttribute(MyBatisAttributes.MAPPER_CLASS,
                        new ClassWrapperImpl(methodCallDescription.getMethod().getDeclaringClass().getName()).getJavaClassName())
                .setAttribute(MyBatisAttributes.MAPPER_METHOD,
                        methodCallDescription.getMethod().getName().toString() + methodCallDescription.getMethod().getDescriptor())
                .buildFlowInfo();

        // add variables indexes
        int variableIndex = 0;
        for (MyBatisSqlVariable variable : sqlCommand.getVariables()) {
            // indexes are counted from 1...
            ++variableIndex;
            if (Objects.equals(MyBatisSqlVariable.Mode.OUT, variable.getMode())) {
                continue;
            }
            if (Objects.equals(QUERY_TYPE.get(sqlCommand.getCommandType()), QueryType.QUERY)) {
                builder.objectFlowInfoBuilder(TO_RESULT).addFieldMapping(variable.getVariableName(), variableIndex);
            } else {
                final int argumentIndex = MyBatisUtils.getArgumentIndex(variable.getVariableName(), methodCallDescription.getMethod());
                builder.objectFlowInfoBuilder(TO_RECEIVER).addArgumentFieldMapping(argumentIndex, variable.getVariableName(), variableIndex);
            }
        }

        // add mapping for return object
        final ObjectFlowInfoBuilder returnObjectFlowBuilder = builder.objectFlowInfoBuilder(TO_RESULT);
        for (Map.Entry<String, String> entry : sqlCommand.getResultMapping().getPropertyColumnMapping().entrySet()) {
            returnObjectFlowBuilder.addFieldMapping(entry.getKey(), entry.getValue());
        }

        // add mappings for argument objects
        final ObjectFlowInfoBuilder argumentsObjectFlowBuilder = builder.objectFlowInfoBuilder(TO_RECEIVER);
        for (SqlMapping sqlMapping : sqlCommand.getArgumentsMapping()) {
            final int argumentIndex = MyBatisUtils.getArgumentIndex(sqlMapping.getParameterName(), methodCallDescription.getMethod());
            for (Map.Entry<String, String> entry : sqlMapping.getPropertyColumnMapping().entrySet()) {
                argumentsObjectFlowBuilder.addArgumentFieldMapping(argumentIndex, entry.getKey(), entry.getValue());
            }
            argumentsObjectFlowBuilder.addArgumentsAttribute(argumentIndex,
                    MyBatisAttributes.DATASOURCE_MAP_KEY,
                    ImmutableMap.of(sqlMapping.getParameterName(), sqlMapping.getPropertyColumnMapping().keySet()));
        }

        return builder
                .buildEndpointFlowInfo();
    }

    @Nonnull
    private static Map<IMethod, SqlCommand> computeHandlingMethods(@Nonnull final ClassMethodCache classMethodCache) {
        final MyBatisClassAnalyzer analyzer = new MyBatisClassAnalyzer(new FileContentReader(classMethodCache.getJarFiles()));

        final ImmutableMap.Builder<IMethod, SqlCommand> handlingMethodsResultBuilder = ImmutableMap.builder();
        classMethodCache.getAllClasses().stream()
                .parallel()
                // we search only application methods for mappers
                .filter(iClass -> Objects.equals(iClass.getClassLoader().getReference(), ClassLoaderReference.Application))
                .map(analyzer::analyse)
                .flatMap(Collection::stream)
                .forEach(sqlCommand -> {
                    synchronized (handlingMethodsResultBuilder) {
                        handlingMethodsResultBuilder.put(sqlCommand.getMethod(), sqlCommand);
                    }
                });

        return handlingMethodsResultBuilder.build();
    }
}
