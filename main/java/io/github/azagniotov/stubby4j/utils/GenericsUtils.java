package io.github.azagniotov.stubby4j.utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class GenericsUtils {

    private GenericsUtils() {

    }

    public static <T> LinkedList<T> toCheckedLinkedList(final Object collectionObject) {
        final List<?> rawCollection = (List<?>) collectionObject;
        return new LinkedList<T>() {{
            for (int idx = 0; idx < rawCollection.size(); idx++) {
                add(idx, (T) rawCollection.get(idx));
            }
        }};
    }

    public static <T> ArrayList<T> toCheckedArrayList(final Object collectionObject) {
        final List<?> rawCollection = (List<?>) collectionObject;
        return new ArrayList<T>() {{
            for (int idx = 0; idx < rawCollection.size(); idx++) {
                add(idx, (T) rawCollection.get(idx));
            }
        }};
    }

    public static <K, V> LinkedHashMap<K, V> toCheckedLinkedHashMap(final Object mapObject) {
        final LinkedHashMap<?, ?> rawMap = (LinkedHashMap<?, ?>) mapObject;
        return new LinkedHashMap<K, V>() {{
            for (final Map.Entry<?, ?> entry : rawMap.entrySet()) {
                put((K) entry.getKey(), (V) entry.getValue());
            }
        }};
    }
}
