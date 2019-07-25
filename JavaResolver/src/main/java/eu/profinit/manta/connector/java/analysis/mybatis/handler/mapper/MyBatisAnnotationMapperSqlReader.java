package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeCT.AnnotationsReader;
import com.ibm.wala.shrikeCT.AnnotationsReader.ArrayElementValue;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.annotations.Annotation;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.TagByNameXmlHandler;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlMapping;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.XmlUtils;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;
import eu.profinit.manta.connector.java.model.flowgraph.AttributeValueConstants;

import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.CONSTRUCTOR_ARGS_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.DELETE_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.INSERT_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.RESULTS_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SELECT_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.UPDATE_ANNOTATION;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.CHOOSE_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.COMMENT_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.FOREACH_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.IF_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.RAW_TEXT_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.SET_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.TRIM_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.WHERE_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType.DELETE;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType.INSERT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType.SELECT;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType.UPDATE;

class MyBatisAnnotationMapperSqlReader extends MyBatisMapperSqlReader {
    private static final XmlTagHandlers SCRIPT_XML_TAG_HANDLERS = new XmlTagHandlers(
            IF_HANDLER,
            FOREACH_HANDLER,
            CHOOSE_HANDLER,
            TRIM_HANDLER,
            WHERE_HANDLER,
            SET_HANDLER,
            RAW_TEXT_HANDLER,
            COMMENT_HANDLER,
            new TagByNameXmlHandler("script"));

    private static final Map<TypeName, CommandType> MAPPER_COMMANDS = ImmutableMap.<TypeName, CommandType>builder()
            .put(SELECT_ANNOTATION.getWalaTypeName(), SELECT)
            .put(INSERT_ANNOTATION.getWalaTypeName(), INSERT)
            .put(UPDATE_ANNOTATION.getWalaTypeName(), UPDATE)
            .put(DELETE_ANNOTATION.getWalaTypeName(), DELETE)
            .build();

    private final Annotation commandAnnotation;
    private final Annotation resultMappingAnnotation;
    private final Annotation constructorArgsMappingAnnotation;

    MyBatisAnnotationMapperSqlReader(@Nonnull final IMethod iMethod) {
        super(iMethod);
        final List<Annotation> allAnnotations = Lists.newArrayList(iMethod.getAnnotations());
        final List<Annotation> commandAnnotations = allAnnotations.stream()
                .filter(annotation -> MAPPER_COMMANDS.keySet().contains(annotation.getType().getName()))
                .collect(Collectors.toList());
        Validate.isTrue(commandAnnotations.size() <= 1, "Found more myBatis commandAnnotations than expected: " + commandAnnotations);
        commandAnnotation = Iterables.getFirst(commandAnnotations, null);
        resultMappingAnnotation = allAnnotations.stream()
                .filter(annotation -> Objects.equals(annotation.getType().getName(), RESULTS_ANNOTATION.getWalaTypeName()))
                .findAny()
                .orElse(null);
        constructorArgsMappingAnnotation = allAnnotations.stream()
                .filter(annotation -> Objects.equals(annotation.getType().getName(), CONSTRUCTOR_ARGS_ANNOTATION.getWalaTypeName()))
                .findAny()
                .orElse(null);
    }

    boolean accepts() {
        return commandAnnotation != null;
    }

    @Nonnull
    @Override
    protected CommandType getCommandType() {
        return Validate.notNull(MAPPER_COMMANDS.get(commandAnnotation.getType().getName()));
    }

    @Nonnull
    @Override
    protected String getSql() {
        final ArrayElementValue sqlCommand = (ArrayElementValue) commandAnnotation.getNamedArguments().get("value");
        final String sql = Arrays.stream(sqlCommand.vals)
                .map(this::getText)
                .collect(Collectors.joining("\n"));
        if (sql.contains("<script>")) {
            try {
                final Document document = XmlUtils.readDocument(String.format("Script for method %s", iMethod.getSignature()), sql);

                return SCRIPT_XML_TAG_HANDLERS.handle(document.getChildNodes()).collect(Collectors.joining(""));
            } catch (XmlException e) {
                log.error("Failed to read script for mapper method {}", iMethod, e);
                return AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT;
            }
        } else {
            return sql;
        }
    }

    @Nonnull
    @Override
    protected SqlMapping getResultMapping() {
        final SqlMapping resultMapping = new SqlMapping();
        if (resultMappingAnnotation != null) {
            final ArrayElementValue annotationMapping = (ArrayElementValue) resultMappingAnnotation.getNamedArguments().get("value");
            Arrays.stream(annotationMapping.vals)
                    .map(resultAnnotation -> ((AnnotationsReader.AnnotationAttribute) resultAnnotation).elementValues)
                    .forEach(mapping -> resultMapping.addPropertyToColumn(getText(mapping.get("property")), getText(mapping.get("column"))));
        }
        if (constructorArgsMappingAnnotation != null) {
            final ArrayElementValue annotationMapping = (ArrayElementValue) constructorArgsMappingAnnotation.getNamedArguments().get("value");
            Arrays.stream(annotationMapping.vals)
                    .map(resultAnnotation -> ((AnnotationsReader.AnnotationAttribute) resultAnnotation).elementValues)
                    // we do not know name of a property...
                    .forEach(mapping -> resultMapping.addPropertyToColumn(AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT,
                            getText(mapping.get("column"))));
        }
        return resultMapping;
    }

    @Nonnull
    @Override
    protected List<SqlMapping> getArgumentsMapping() {
        return Collections.emptyList();
    }

    @Nonnull
    private String getText(@Nullable final AnnotationsReader.ElementValue elementValue) {
        return elementValue == null ? "" : ((AnnotationsReader.ConstantElementValue) elementValue).val.toString();
    }
}
