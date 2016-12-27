package io.github.azagniotov.stubby4j.utils;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A set of util methods that safely convert raw non-generic collections into generic collection types.
 * <p>
 * 1. The conversion guarantees type safety of the values, which eliminates heap pollution.
 * 2. These utils methods eliminate the need for {@code @SuppressWarnings("unchecked")} annotation.
 * 3. Since the conversion has a {@code O(N)} runtime, execute caution before deciding to convert large collections.
 */
public final class SafeGenericsUtils {

    private SafeGenericsUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static <T> Collection<T> asCheckedCollection(final Collection<?> rawCollection, final Class<?> valueClassType, final Supplier<Collection<T>> collectionFactory) {
        final Collection<T> collection = collectionFactory.get();
        final Iterator<?> rawIterator = rawCollection.iterator();
        while (rawIterator.hasNext()) {
            collection.add(as(valueClassType, rawIterator.next()));
        }
        return collection;
    }

    public static <T> List<T> asCheckedList(final Object listObject, final Class<?> valueClassType, final Supplier<List<T>> listFactory) {
        final List<?> rawList = (List) listObject;
        final List<T> list = listFactory.get();
        for (int idx = 0; idx < rawList.size(); idx++) {
            list.add(idx, as(valueClassType, rawList.get(idx)));
        }
        return list;
    }

    public static <K, V> Map<K, V> asCheckedMap(final Object mapObject, final Class<?> keyClassType, final Class<?> valueClassType, final Supplier<Map<K, V>> mapFactory) {
        final Map<?, ?> rawMap = (Map) mapObject;
        final Map<K, V> map = mapFactory.get();
        for (final Map.Entry<?, ?> rawEntry : rawMap.entrySet()) {
            map.put(as(keyClassType, rawEntry.getKey()), as(valueClassType, rawEntry.getValue()));
        }
        return map;
    }

    public static <T> Set<T> asCheckedSet(final Object setObject, final Class<?> valueClassType, final Supplier<Set<T>> setFactory) {
        final Set<?> rawSet = (Set) setObject;
        final Set<T> set = setFactory.get();
        final Iterator<?> rawIterator = rawSet.iterator();
        while (rawIterator.hasNext()) {
            set.add(as(valueClassType, rawIterator.next()));
        }
        return set;
    }

    public static <T> T as(final Class<?> targetClazz, final Object instance) {
        checkCast(targetClazz, instance);
        return (T) targetClazz.cast(instance);
    }

    public static void checkCast(final Class<?> targetClazz, final Object instance) {
        if (!targetClazz.isInstance(instance)) {
            throw new ClassCastException("Expected: " + targetClazz.getCanonicalName() + ", instead got: " + instance.getClass().getCanonicalName() + " for instance: " + instance);
        }
    }
}
