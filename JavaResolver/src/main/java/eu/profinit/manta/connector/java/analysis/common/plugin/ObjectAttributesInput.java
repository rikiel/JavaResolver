package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.ibm.wala.classLoader.IClass;

import eu.profinit.manta.connector.java.analysis.utils.Validate;

/**
 * Input attributes
 */
public class ObjectAttributesInput extends ObjectAttributes {
    private final IClass type;

    public ObjectAttributesInput(@Nonnull final IClass type, @Nonnull final Map<String, Set<Object>> attributes) {
        Validate.notNullAll(type, attributes);
        this.type = type;
        getAttributes().putAll(attributes);
    }

    public ObjectAttributesInput() {
        this.type = null;
    }

    @Nonnull
    public IClass getType() {
        Validate.notNull(type);
        return type;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("type", type)
                .add("attributes", getAttributes())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
