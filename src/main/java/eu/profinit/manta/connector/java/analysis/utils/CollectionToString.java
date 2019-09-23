package eu.profinit.manta.connector.java.analysis.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterables;

public class CollectionToString<T> {
    @Nonnull
    private final Collection<T> values;
    @Nonnull
    private final Function<T, String> toStringFunction;

    public CollectionToString(@Nonnull final Collection<T> values) {
        this(values, Objects::toString);
    }

    public CollectionToString(@Nonnull final Collection<T> values,
                              @Nonnull final Function<T, String> toStringFunction) {
        Validate.notNullAll(values, toStringFunction);
        this.values = values;
        this.toStringFunction = toStringFunction;
    }

    public CollectionToString(@Nonnull final T[] values) {
        this(Arrays.asList(values));
    }

    @Override
    public String toString() {
        if (values.isEmpty()) {
            return "[ ]";
        } else if (values.size() == 1) {
            return String.format("[%s]", Iterables.getOnlyElement(values));
        }
        return values.stream()
                .map(toStringFunction)
                .collect(Collectors.joining(",\n\t", "[\n\t", "]"));
    }
}
