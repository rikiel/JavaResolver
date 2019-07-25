package eu.profinit.manta.connector.java.analysis.common;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.utils.Validate;

public class ClassWrapperImpl implements ClassWrapper {
    private final String javaClassName;
    private final TypeName walaTypeName;

    public ClassWrapperImpl(@Nonnull final String javaClassName) {
        Validate.notNull(javaClassName);

        this.javaClassName = javaClassName;
        this.walaTypeName = TypeName.findOrCreate(StringStuff.deployment2CanonicalTypeString(javaClassName));
    }

    public ClassWrapperImpl(@Nonnull final TypeName typeName) {
        Validate.notNull(typeName);

        this.javaClassName = StringStuff.jvmToBinaryName(typeName.toString());
        this.walaTypeName = typeName;
    }

    public ClassWrapperImpl(@Nonnull final Class<?> clazz) {
        this(clazz.getName());
    }

    @Nonnull
    @Override
    public String getJavaClassName() {
        return javaClassName;
    }

    @Nonnull
    @Override
    public TypeName getWalaTypeName() {
        return walaTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClassWrapper)) {
            return false;
        }

        final ClassWrapper that = (ClassWrapper) o;

        return new EqualsBuilder()
                .append(walaTypeName, that.getWalaTypeName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(walaTypeName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("javaClassName", javaClassName)
                .add("walaTypeName", walaTypeName)
                .toString();
    }
}
