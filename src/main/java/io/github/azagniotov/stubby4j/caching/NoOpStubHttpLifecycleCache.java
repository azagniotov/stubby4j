package io.github.azagniotov.stubby4j.caching;


import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.ehcache.UserManagedCache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class NoOpStubHttpLifecycleCache implements Cache<String, StubHttpLifecycle> {

    private static final AtomicInteger ATOMIC_INTEGER_ZERO = new AtomicInteger();

    NoOpStubHttpLifecycleCache() {

    }

    @Override
    public Optional<StubHttpLifecycle> get(final String key) {
        return Optional.empty();
    }

    @Override
    public void putIfAbsent(final String key, final StubHttpLifecycle value) {
        // NO-OP
    }

    @Override
    public boolean clearByKey(final String key) {
        return true;
    }

    @Override
    public void clear() {
        // NO-OP
    }

    @Override
    public UserManagedCache<String, StubHttpLifecycle> cache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AtomicInteger size() {
        return ATOMIC_INTEGER_ZERO;
    }
}
