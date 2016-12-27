package io.github.azagniotov.stubby4j.utils;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of util methods that safely convert raw non-generic collections into generic collection types.
 */
public final class SafeGenericsUtils {

    private SafeGenericsUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static <T> List<T> asCheckedList(final Object listObject, final Class<?> valueClassType, final List<T> targetList) {
        final List<?> rawList = (List) listObject;
        for (int idx = 0; idx < rawList.size(); idx++) {
            targetList.add(idx, as(valueClassType, rawList.get(idx)));
        }
        return targetList;
    }

    public static <K, V> Map<K, V> asCheckedMap(final Object mapObject, final Class<K> keyClassType, final Class<?> valueClassType) {
        final Map<?, ?> rawMap = (Map) mapObject;
        final Map<K, V> map = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> rawEntry : rawMap.entrySet()) {
            map.put(as(keyClassType, rawEntry.getKey()), as(valueClassType, rawEntry.getValue()));
        }
        return map;
    }

    static <T> T as(final Class<?> targetClazz, final Object instance) {
        checkCast(targetClazz, instance);
        return (T) targetClazz.cast(instance);
    }

    static void checkCast(final Class<?> targetClazz, final Object instance) {
        if (!targetClazz.isInstance(instance)) {
            throw new ClassCastException("Expected: " + targetClazz.getCanonicalName() + ", instead got: " + instance.getClass().getCanonicalName() + " for instance: " + instance);
        }
    }
}
