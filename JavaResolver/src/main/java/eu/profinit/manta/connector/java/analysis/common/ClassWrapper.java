package eu.profinit.manta.connector.java.analysis.common;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.ibm.wala.types.TypeName;

import eu.profinit.manta.connector.java.analysis.utils.Validate;

/**
 * Class identification (both in WALA and Java)
 */
public interface ClassWrapper {
    @Nonnull
    String getJavaClassName();

    @Nonnull
    TypeName getWalaTypeName();

    static boolean isSame(@Nonnull final ClassWrapper clazz1, @Nonnull final ClassWrapper clazz2) {
        Validate.notNullAll(clazz1, clazz2);
        return Objects.equals(clazz1.getWalaTypeName(), clazz2.getWalaTypeName());
    }
}

