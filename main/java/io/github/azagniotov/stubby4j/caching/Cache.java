package io.github.azagniotov.stubby4j.caching;


import org.ehcache.UserManagedCache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public interface Cache<K, V> {

    static Cache stubHttpLifecycleCache(final long cacheEntryLifetimeSeconds) {
        return new StubHttpLifecycleCache(cacheEntryLifetimeSeconds);
    }

    static Cache regexPatternCache(final long cacheEntryLifetimeSeconds) {
        return new RegexPatternCache(cacheEntryLifetimeSeconds);
    }

    default Optional<V> get(final K key) {
        return Optional.<V>ofNullable(cache().get(key));
    }

    default void putIfAbsent(final K key, final V value) {
        if (!cache().containsKey(key)) {
            cache().put(key, value);
            size().incrementAndGet();
        }
    }

    default void clearByKey(final K key) {
        if (cache().containsKey(key)) {
            cache().remove(key);
            size().decrementAndGet();
        }
    }

    default void clear() {
        cache().clear();
        size().set(0);
    }

    default void close() {
        cache().close();
    }

    UserManagedCache<K, V> cache();

    AtomicInteger size();
}
