package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

/**
 * Attributes wrapper
 */
public class ObjectAttributes {
    @Nonnull
    private final Map<String, Set<Object>> attributes = Maps.newHashMap();

    @Nonnull
    public <T> Set<T> getAttribute(@Nonnull final IAttributeName attributeName) {
        return getAttribute(attributeName.getAttributeName());
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> Set<T> getAttribute(@Nonnull final String attributeName) {
        final Set<Object> result = attributes.get(attributeName);
        return result == null ? ImmutableSet.of() : (Set<T>) result;
    }

    public void addAll(@Nonnull final String attributeName, @Nullable final Collection<?> values) {
        Validate.notNull(attributeName);
        if (values != null) {
            attributes.computeIfAbsent(attributeName, a -> Sets.newHashSet()).addAll(values);
        }
    }

    public void addAll(@Nonnull final IAttributeName attributeName, @Nullable final Collection<?> values) {
        addAll(attributeName.getAttributeName(), values);
    }

    public void addAll(@Nonnull final ObjectAttributes other) {
        Validate.notNull(other);
        for (Map.Entry<String, Set<Object>> entry : other.getAttributes().entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    public void addAllExcept(@Nonnull final ObjectAttributes other,
                             @Nonnull final Set<String> attributesToFilterOut) {
        Validate.notNullAll(other, attributesToFilterOut);
        for (Map.Entry<String, Set<Object>> entry : other.getAttributes().entrySet()) {
            if (!attributesToFilterOut.contains(entry.getKey())) {
                addAll(entry.getKey(), entry.getValue());
            }
        }
    }

    @Nonnull
    public Map<String, Set<Object>> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("attributes", attributes)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ObjectAttributes)) {
            return false;
        }

        final ObjectAttributes that = (ObjectAttributes) o;

        return new EqualsBuilder()
                .append(attributes, that.attributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(attributes)
                .toHashCode();
    }
}
