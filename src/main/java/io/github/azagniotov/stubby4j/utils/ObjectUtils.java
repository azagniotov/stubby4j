package io.github.azagniotov.stubby4j.utils;


public final class ObjectUtils {

    private ObjectUtils() {

    }

    public static boolean isNull(final Object instance) {
        return instance == null;
    }

    public static boolean isNotNull(final Object instance) {
        return !isNull(instance);
    }
}
