package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.annotations.Annotation;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.ObjectAttributesInput;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.model.flowgraph.Attributes;

import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.PARAM_ANNOTATION;

public class MyBatisUtils {
    private static final Logger log = LoggerFactory.getLogger(MyBatisUtils.class);

    private static final Pattern VARIABLE_TAG_PATTERN = Pattern.compile(
            ""
            // variable tag start
            + "#\\{"
            // variable name
            + "([^\\s,}]+)"
            + "(?:"
            // result map name (if present)
            + "(?:resultMap\\s*=\\s*[\"']?([^\\s'\",}]+)[\"']?)"
            // mode = IN/OUT/INOUT
            + "|(?:mode\\s*=\\s*[\"']?([^\\s'\",}]+)[\"']?)"
            // or anything (except end of tag '}')
            + "|[^}])*"
            // variable tag end
            + "}");
    private static final Pattern VARIABLE_NAME_WITH_INDEX_PATTERN = Pattern.compile("^(?:arg|param)(\\d+)$");

    private MyBatisUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param sql SQL statement
     * @return Parsed arguments used in sql. Format of arguments is {@code #{<name>[, parameters...]}}.
     * Parameters that are parsed are {@code mode} and {@code resultMap}
     */
    @Nonnull
    public static List<MyBatisSqlVariable> getArgumentsFromSql(@Nonnull final String sql) {
        Validate.notNull(sql);

        final List<MyBatisSqlVariable> result = Lists.newArrayList();

        final Matcher matcher = VARIABLE_TAG_PATTERN.matcher(sql);
        while (matcher.find()) {
            result.add(new MyBatisSqlVariable(matcher.group(), matcher.group(1), matcher.group(3), matcher.group(2)));
        }
        return result;
    }

    /**
     * @param parameterName Name of parameter used in XML file
     * @param iMethod       Calling method
     * @return Argument index for {@code parameterName}.
     * <br>
     * Note, that if there is only single argument, index {@code 0} is returned.
     * <br>
     * There are three types of supported names:
     * <ul>
     * <li><b>arg[index]</b> where index is numbered [0, ... ]</li>
     * <li><b>param[index]</b> where index is numbered [1, ... ]</li>
     * <li><b>name</b> when {@code org.apache.ibatis.annotations.Param} annotation is used</li>
     * </ul>
     */
    public static int getArgumentIndex(@Nonnull final String parameterName,
                                       @Nonnull final IMethod iMethod) {
        final int result;
        final int argumentsCount = WalaUtils.getArguments(iMethod).size();
        if (argumentsCount == 1) {
            result = 0;
        } else {
            final String plainName = parameterName.replaceAll("\\..*", "");
            final Matcher matcher = VARIABLE_NAME_WITH_INDEX_PATTERN.matcher(plainName);
            if (matcher.find()) {
                final int number = Integer.parseInt(matcher.group(1));
                if (plainName.startsWith("arg")) {
                    result = number;
                } else {
                    result = number - 1;
                }
            } else {
                result = getAnnotationArgumentIndex(plainName, ((IBytecodeMethod<?>) iMethod).getParameterAnnotations());
            }
        }
        Validate.isTrue(result < argumentsCount, "Argument index is out of bounds");
        return result;
    }

    @Nonnull
    public static String getSqlWithSubstitutedArguments(@Nonnull final String sql,
                                                        @Nonnull final IMethod iMethod,
                                                        @Nonnull final List<ObjectAttributesInput> argumentsAttributes) {
        Validate.notNullAll(sql, iMethod, argumentsAttributes);

        final StringBuffer resultSql = new StringBuffer();

        final Matcher matcher = VARIABLE_TAG_PATTERN.matcher(sql);
        while (matcher.find()) {
            final String argumentName = matcher.group(1);
            final ObjectAttributesInput argumentAttributes = argumentsAttributes.get(getArgumentIndex(argumentName, iMethod));
            final String newValue = getAttributeSubstitution(argumentAttributes, argumentName);
            log.trace("Making substitution '{}' to '{}'", matcher.group(), newValue);
            matcher.appendReplacement(resultSql, newValue);
        }
        matcher.appendTail(resultSql);

        return resultSql.toString();
    }

    @Nonnull
    private static String getAttributeSubstitution(@Nonnull final ObjectAttributesInput argumentAttributes,
                                                   @Nonnull final String argumentName) {
        if (WalaUtils.isSupertype(argumentAttributes.getType(), new ClassWrapperImpl(Map.class))) {
            final String plainName = argumentName.replaceAll(".*\\.", "");
            final List<Object> attributeValues = argumentAttributes.getAttribute(Attributes.MAP_KEYS_TO_VALUES).stream()
                    .flatMap(object -> ((Map<String, Set<Object>>) object).getOrDefault(plainName, ImmutableSet.of()).stream())
                    .collect(Collectors.toList());
            if (attributeValues.size() == 1) {
                return attributeValues.get(0).toString();
            }
        } else if (WalaUtils.isSupertype(argumentAttributes.getType(), new ClassWrapperImpl(String.class))
                   || WalaUtils.isSupertype(argumentAttributes.getType(), new ClassWrapperImpl(Number.class))) {
            final Set<Object> constantValues = argumentAttributes.getAttribute(Attributes.CONSTANT_VALUE);
            if (constantValues.size() == 1) {
                return Lists.newLinkedList(constantValues).get(0).toString();
            }
        }
        return "?";
    }

    private static int getAnnotationArgumentIndex(@Nonnull final String parameterName,
                                                  @Nonnull final Collection<Annotation>[] parameterAnnotations) {
        for (int i = 0; i < parameterAnnotations.length; ++i) {
            for (Annotation annotation : parameterAnnotations[i]) {
                final String annotationParameterValue = WalaUtils.getParameterValue(annotation, PARAM_ANNOTATION, "value");
                if (Objects.equal(annotationParameterValue, parameterName)) {
                    return i;
                }
            }
        }
        return Validate.fail("No parameter annotated @Param(%s) found. Found %s", parameterName, Arrays.toString(parameterAnnotations));
    }

    public static class MyBatisSqlVariable {
        @Nonnull
        private final String fullVariableTag;
        @Nonnull
        private final String variableName;
        @Nonnull
        private final Mode mode;
        @Nullable
        private final String resultMapName;

        public MyBatisSqlVariable(@Nonnull final String fullVariableTag,
                                  @Nonnull final String variableName,
                                  @Nullable final String mode,
                                  @Nullable final String resultMapName) {
            this.fullVariableTag = fullVariableTag;
            this.variableName = variableName;
            this.mode = mode == null ? Mode.IN : Mode.valueOf(mode);
            this.resultMapName = resultMapName;

            log.trace("Parsed variable tag: {}", this);
        }

        @Nonnull
        public String getVariableName() {
            return variableName;
        }

        @Nonnull
        public Mode getMode() {
            return mode;
        }

        @Nullable
        public String getResultMapName() {
            return resultMapName;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("fullVariableTag", fullVariableTag)
                    .add("variableName", variableName)
                    .add("mode", mode)
                    .add("resultMapName", resultMapName)
                    .toString();
        }

        public enum Mode {
            IN,
            OUT,
            INOUT
        }
    }
}
