package io.github.azagniotov.stubby4j.caching;


import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

class StubHttpLifecycleCache implements Cache<String, StubHttpLifecycle> {

    private final AtomicInteger cacheSize;
    private final UserManagedCache<String, StubHttpLifecycle> localCache;

    StubHttpLifecycleCache(final long cacheEntryLifetimeSeconds) {
        final Duration timeToLiveExpiration = Duration.ofSeconds(cacheEntryLifetimeSeconds);

        this.localCache = UserManagedCacheBuilder
                .newUserManagedCacheBuilder(String.class, StubHttpLifecycle.class)
                .withResourcePools(ResourcePoolsBuilder.heap(500L))
                .identifier(this.getClass().getSimpleName())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(timeToLiveExpiration))
                .build(true);

        this.cacheSize = new AtomicInteger(0);
    }

    @Override
    public UserManagedCache<String, StubHttpLifecycle> cache() {
        return localCache;
    }

    @Override
    public AtomicInteger size() {
        return cacheSize;
    }
}
