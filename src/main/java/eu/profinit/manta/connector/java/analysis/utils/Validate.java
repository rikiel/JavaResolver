package eu.profinit.manta.connector.java.analysis.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

public class Validate {
    private Validate() {
        throw new UnsupportedOperationException();
    }

    public static void isTrue(boolean condition) {
        isTrue(condition, "The validated expression is false");
    }

    public static void isTrue(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(msg, args));
        }
    }

    public static void isFalse(boolean condition) {
        isFalse(condition, "The validated expression is false");
    }

    public static void isFalse(boolean condition, String msg, Object... args) {
        if (condition) {
            throw new IllegalArgumentException(String.format(msg, args));
        }
    }

    public static void validState(boolean condition) {
        validState(condition, "Invalid state");
    }

    public static void validState(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalStateException(String.format(msg, args));
        }
    }

    public static <T> T notNull(T object) {
        return notNull(object, "The validated object is null");
    }

    public static <T> T notNull(T object, String msg, Object... args) {
        if (object == null) {
            throw new NullPointerException(String.format(msg, args));
        }
        return object;
    }

    public static void notNullAll(Object... args) {
        notNull((Object) args);
        notNullAll(Arrays.asList(args));
    }

    public static void notNullAll(Iterable<?> args) {
        notNull((Object) args);
        int i = 0;
        for (Object arg : args) {
            notNull(arg, "Argument #%s is null", i);
            ++i;
        }
    }

    public static void equals(Object o1, Object o2) {
        equals(o1, o2, "Object %s is not equal to %s", o1, o2);
    }

    public static void equals(Object o1, Object o2, String msg, Object... args) {
        isTrue(Objects.equals(o1, o2), msg, args);
    }

    public static void notEquals(Object o1, Object o2) {
        notEquals(o1, o2, "Object %s is not equal to %s", o1, o2);
    }

    public static void notEquals(Object o1, Object o2, String msg, Object... args) {
        isFalse(Objects.equals(o1, o2), msg, args);
    }

    public static <T> T fail(Exception e) {
        throw new IllegalStateException(e);
    }

    public static <T> T fail(String msg) {
        throw new IllegalStateException(msg);
    }

    public static <T> T fail(String msg, Object... args) {
        throw new IllegalStateException(String.format(msg, args));
    }

    public static <T> T fail(String msg, Exception e, Object... args) {
        throw new IllegalStateException(String.format(msg, args), e);
    }

    public static void isNull(Object arg, String msg) {
        if (arg != null) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void isNull(Object arg) {
        isNull(arg, "Argument should be null");
    }

    @Nonnull
    public static <T> T isPresent(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<T> value) {
        notNull(value);
        return value.orElseThrow(() -> new IllegalArgumentException("Value is not present!"));
    }
}
