package io.github.azagniotov.stubby4j.caching;


import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.ehcache.UserManagedCache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public interface Cache<K, V> {

    static Cache<String, StubHttpLifecycle> noOpStubHttpLifecycleCache() {
        return new NoOpStubHttpLifecycleCache();
    }

    static Cache<String, StubHttpLifecycle> stubHttpLifecycleCache(final long cacheEntryLifetimeSeconds) {
        return new StubHttpLifecycleCache(cacheEntryLifetimeSeconds);
    }

    static Cache<Integer, Pattern> regexPatternCache(final long cacheEntryLifetimeSeconds) {
        return new RegexPatternCache(cacheEntryLifetimeSeconds);
    }

    default Optional<V> get(final K key) {
        return Optional.ofNullable(cache().get(key));
    }

    default void putIfAbsent(final K key, final V value) {
        if (!cache().containsKey(key)) {
            cache().put(key, value);
            size().incrementAndGet();
        }
    }

    default boolean clearByKey(final K key) {
        if (cache().containsKey(key)) {
            cache().remove(key);
            size().decrementAndGet();

            return true;
        }

        return false;
    }

    default void clear() {
        cache().clear();
        size().set(0);
    }

    UserManagedCache<K, V> cache();

    AtomicInteger size();
}
