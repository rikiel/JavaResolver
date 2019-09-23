package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import eu.profinit.manta.connector.java.analysis.common.StrictHashMap;

/**
 * Flow info for an object
 */
public class ObjectFlowInfo {
    private final ObjectAttributes attributes = new ObjectAttributes();
    private final Map<String, Set<Object>> fieldsMapping = new StrictHashMap<>();
    private final Map<Integer, ObjectAttributes> argumentAttributes = new StrictHashMap<>();
    private final Map<Integer, Map<String, Set<Object>>> argumentFieldsMapping = new StrictHashMap<>();

    public ObjectAttributes getAttributes() {
        return attributes;
    }

    public ObjectAttributes getArgumentAttributes(int index) {
        return argumentAttributes.computeIfAbsent(index, field -> new ObjectAttributes());
    }

    public Map<String, Set<Object>> getFieldsMapping() {
        return fieldsMapping;
    }

    public Map<Integer, Map<String, Set<Object>>> getArgumentFieldsMapping() {
        return argumentFieldsMapping;
    }

    public void addFieldMapping(@Nonnull final String fieldName, @Nullable final Object value) {
        if (value != null) {
            fieldsMapping.computeIfAbsent(fieldName, field -> Sets.newHashSet()).add(value);
        }
    }

    public void addArgumentFieldMapping(final int argumentIndex, @Nonnull final String fieldName, @Nullable final Object value) {
        if (value != null) {
            final Map<String, Set<Object>> argumentMapping = argumentFieldsMapping.computeIfAbsent(argumentIndex, index -> new StrictHashMap<>());
            argumentMapping.computeIfAbsent(fieldName, field -> Sets.newHashSet()).add(value);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("attributes", attributes)
                .add("argumentAttributes", argumentAttributes)
                .add("fieldsMapping", fieldsMapping)
                .add("argumentFieldsMapping", argumentFieldsMapping)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ObjectFlowInfo)) {
            return false;
        }

        final ObjectFlowInfo that = (ObjectFlowInfo) o;

        return new EqualsBuilder()
                .append(attributes, that.attributes)
                .append(fieldsMapping, that.fieldsMapping)
                .append(argumentAttributes, that.argumentAttributes)
                .append(argumentFieldsMapping, that.argumentFieldsMapping)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(attributes)
                .append(fieldsMapping)
                .append(argumentAttributes)
                .append(argumentFieldsMapping)
                .toHashCode();
    }
}
