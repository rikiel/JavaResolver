package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class SqlMapping {
    /**
     * Mapping: Property -> Column
     */
    private final Map<String, String> mapping = Maps.newLinkedHashMap();
    /**
     * Name of parameter for this mapping
     */
    private final String parameterName;

    public SqlMapping() {
        this("return object");
    }

    public SqlMapping(@Nonnull final String parameterName) {
        Validate.notNull(parameterName);
        this.parameterName = parameterName;
    }

    @Nonnull
    public SqlMapping addPropertyToColumn(@Nonnull final String propertyName,
                                          @Nonnull final String columnName) {
        Validate.isTrue(!mapping.containsKey(propertyName), "Mapping already contains property " + propertyName);
        mapping.put(propertyName, columnName);
        return this;
    }

    @Nonnull
    public SqlMapping withMapping(@Nonnull final Map<String, String> mapping) {
        Validate.notNull(mapping);
        this.mapping.putAll(mapping);
        return this;
    }

    @Nonnull
    public String getParameterName() {
        return parameterName;
    }

    @Nonnull
    public Map<String, String> getPropertyColumnMapping() {
        return Collections.unmodifiableMap(mapping);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("parameterName", parameterName)
                .add("mapping", mapping)
                .toString();
    }
}
