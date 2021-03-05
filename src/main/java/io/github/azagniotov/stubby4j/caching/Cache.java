package io.github.azagniotov.stubby4j.caching;


import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.ehcache.UserManagedCache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public interface Cache<K, V> {

    long CACHE_ENTRY_LIFETIME_SECONDS = 3600L;  // 3600 secs => 60 minutes

    static Cache<String, StubHttpLifecycle> stubHttpLifecycleCache(final boolean buildNoOpCache) {
        if (buildNoOpCache) {
            return new NoOpStubHttpLifecycleCache();
        } else {
            return new StubHttpLifecycleCache(CACHE_ENTRY_LIFETIME_SECONDS);
        }
    }

    static Cache<Integer, Pattern> regexPatternCache() {
        return new RegexPatternCache(CACHE_ENTRY_LIFETIME_SECONDS);
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
