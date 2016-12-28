package io.github.azagniotov.stubby4j.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A set of utility methods return given raw non-generic collections as
 * generically typesafe collections.
 * <p>
 * If a given raw collection contains an element of the wrong type, it will
 * result in a {@link ClassCastException}. Assuming a collection contains no
 * incorrectly typed elements prior to the time a generic typesafe collection
 * is generated, it is <i>guaranteed</i> that the generated collection cannot
 * contain an incorrectly typed element, hence eliminates heap pollution.
 * <p>
 * The generics mechanism in the language provides compile-time
 * (static) type checking, but it is possible to defeat this mechanism
 * with unchecked casts.  Usually this is not a problem, as the compiler
 * issues warnings on all such unchecked operations.  There are, however,
 * times when static type checking alone is not sufficient.  For example,
 * suppose a collection is passed to a third-party library and it is
 * imperative that the library code not corrupt the collection by
 * inserting an element of the wrong type.
 * <p>
 * Since {@code null} is considered to be a value of any reference
 * type, a {@link IllegalArgumentException} will be thrown if a given
 * raw collection contains null elements.
 * <p>
 * Since the generating a new generically typesafe collection has a {@code O(N)}
 * running time, execute caution before deciding to convert large collections.
 */
public final class SafeGenericsUtils {

    private SafeGenericsUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    /**
     * Generates a typesafe {@link List} from a given raw non-generic {@code listObject} {@link Object}.
     *
     * @param listObject     the list object for which a new typesafe {@link List} is to be returned
     * @param valueClassType the type of element that {@code listObject} is permitted to hold
     * @param listImpl       the implementation of the returned {@code listImpl}
     * @param <T>            the class of the objects in the {@code listImpl}
     * @param <L>            the class of the returned {@link List}
     * @return a new typesafe {@link List}, which is a shallow copy of the given raw non-generic {@code listObject}
     * @throws ClassCastException       if the class of an element found in the
     *                                  given raw collection prevents it from being added to generated typesafe collection.
     * @throws IllegalArgumentException if the given {@code listImpl} is {@code null}.
     */
    public static <T, L extends List<T>> L asCheckedList(final Object listObject, final Class<T> valueClassType, final L listImpl) {
        if (listObject == null) {
            throw new IllegalArgumentException("Collection object instance is null");
        }
        final List<?> rawList = (List) listObject;
        for (int idx = 0; idx < rawList.size(); idx++) {
            listImpl.add(idx, as(valueClassType, rawList.get(idx)));
        }
        return listImpl;
    }

    /**
     * Generates a typesafe {@link Collection} from a given raw non-generic {@code collectionObject} {@link Object}.
     *
     * @param collectionObject the collection object for which a new typesafe {@link Collection} is to be returned
     * @param valueClassType   the type of element that {@code collectionObject} is permitted to hold
     * @param collectionImpl   the implementation of the returned {@code collectionImpl}
     * @param <T>              the class of the objects in the {@code collectionImpl}
     * @param <C>              the class of the returned {@link Collection}
     * @return a new typesafe {@link Collection}, which is a shallow copy of the given raw non-generic {@code collectionObject}
     * @throws ClassCastException       if the class of an element found in the
     *                                  given raw collection prevents it from being added to generated typesafe collection.
     * @throws IllegalArgumentException if the given {@code collectionImpl} is {@code null}.
     */
    public static <T, C extends Collection<T>> C asCheckedCollection(final Object collectionObject, final Class<T> valueClassType, final C collectionImpl) {
        if (collectionObject == null) {
            throw new IllegalArgumentException("Collection object instance is null");
        }
        final Collection<?> rawCollection = (Collection) collectionObject;
        for (final Object rawCollectionValue : rawCollection) {
            collectionImpl.add(as(valueClassType, rawCollectionValue));
        }
        return collectionImpl;
    }

    /**
     * Generates a typesafe {@link Set} from a given raw non-generic {@code setObject} {@link Object}.
     *
     * @param setObject      the set object for which a new typesafe {@link Set} is to be returned
     * @param valueClassType the type of element that {@code setObject} is permitted to hold
     * @param setImpl        the implementation of the returned {@code setImpl}
     * @param <T>            the class of the objects in the {@code setImpl}
     * @param <S>            the class of the returned {@link Set}
     * @return a new typesafe {@link Set}, which is a shallow copy of the given raw non-generic {@code setObject}
     * @throws ClassCastException       if the class of an element found in the
     *                                  given raw collection prevents it from being added to generated typesafe collection.
     * @throws IllegalArgumentException if the given {@code setImpl} is {@code null}.
     */
    public static <T, S extends Set<T>> S asCheckedSet(final Object setObject, final Class<T> valueClassType, final S setImpl) {
        if (setObject == null) {
            throw new IllegalArgumentException("Collection object instance is null");
        }
        final Set<?> rawSet = (Set) setObject;
        for (final Object rawSetValue : rawSet) {
            setImpl.add(as(valueClassType, rawSetValue));
        }
        return setImpl;
    }

    /**
     * Generates a typesafe {@link Map} from a given raw non-generic {@code mapObject} {@link Object}.
     *
     * @param mapObject      the map object for which a new typesafe {@link Map} is to be returned
     * @param valueClassType the type of element that {@code mapObject} is permitted to hold
     * @param mapImpl        the implementation of the returned {@code mapImpl}
     * @param <K>            the class of the key objects in the {@code mapImpl}
     * @param <V>            the class of the value objects in the {@code mapImpl}
     * @param <M>            the class of the returned {@link Map}
     * @return a new typesafe {@link Map}, which is a shallow copy of the given raw non-generic {@code mapObject}
     * @throws ClassCastException       if the class of an element found in the
     *                                  given raw collection prevents it from being added to generated typesafe collection.
     * @throws IllegalArgumentException if the given {@code mapImpl} is {@code null}.
     */
    public static <K, V, M extends Map<K, V>> M asCheckedMap(final Object mapObject, final Class<K> keyClassType, final Class<V> valueClassType, final M mapImpl) {
        if (mapObject == null) {
            throw new IllegalArgumentException("Collection object instance is null");
        }
        final Map<?, ?> rawMap = (Map) mapObject;
        for (final Map.Entry<?, ?> rawEntry : rawMap.entrySet()) {
            mapImpl.put(as(keyClassType, rawEntry.getKey()), as(valueClassType, rawEntry.getValue()));
        }
        return mapImpl;
    }

    /**
     * Casts a given {@link Object} {@code instance} to a given {@link Class} type {@code targetClazz}
     *
     * @param targetClazz the type that the {@code instance} should be casted to
     * @param instance    an object instance to cast
     * @param <T>         the type class tha the {@code instance} should be casted to
     * @return a typed object instance
     * @throws ClassCastException       if the given {@code instance} cannot be cast to {@code targetClazz}.
     * @throws IllegalArgumentException if the given {@code instance} is {@code null}.
     */
    public static <T> T as(final Class<T> targetClazz, final Object instance) {
        checkCast(targetClazz, instance);
        return targetClazz.cast(instance);
    }

    static <T> void checkCast(final Class<T> targetClazz, final Object instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Object instance is null");
        }
        if (!targetClazz.isInstance(instance)) {
            throw new ClassCastException("Expected: " + targetClazz.getCanonicalName() + ", instead got: " + instance.getClass().getCanonicalName() + " for instance: " + instance);
        }
    }

    /**
     * @see #asCheckedList(Object, Class, List)
     */
    public static <T> List<T> asCheckedArrayList(final Object listObject, final Class<T> valueClassType) {
        return asCheckedList(listObject, valueClassType, new ArrayList<>());
    }

    /**
     * @see #asCheckedList(Object, Class, List)
     */
    public static <T> List<T> asCheckedLinkedList(final Object listObject, final Class<T> valueClassType) {
        return asCheckedList(listObject, valueClassType, new LinkedList<>());
    }

    /**
     * @see #asCheckedSet(Object, Class, Set)
     */
    public static <T> Set<T> asCheckedHashSet(final Object setObject, final Class<T> valueClassType) {
        return asCheckedSet(setObject, valueClassType, new HashSet<>());
    }

    /**
     * @see #asCheckedSet(Object, Class, Set)
     */
    public static <T> Set<T> asCheckedLinkedHashSet(final Object setObject, final Class<T> valueClassType) {
        return asCheckedSet(setObject, valueClassType, new LinkedHashSet<>());
    }

    /**
     * @see #asCheckedMap(Object, Class, Class, Map)
     */
    public static <K, V> Map<K, V> asCheckedHashMap(final Object mapObject, final Class<K> keyClassType, final Class<V> valueClassType) {
        return asCheckedMap(mapObject, keyClassType, valueClassType, new HashMap<>());
    }

    /**
     * @see #asCheckedMap(Object, Class, Class, Map)
     */
    public static <K, V> Map<K, V> asCheckedLinkedHashMap(final Object mapObject, final Class<K> keyClassType, final Class<V> valueClassType) {
        return asCheckedMap(mapObject, keyClassType, valueClassType, new LinkedHashMap<>());
    }
}
