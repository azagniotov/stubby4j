package io.github.azagniotov.stubby4j.caching;


import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

class RegexPatternCache implements Cache<Integer, Pattern> {

    private final AtomicInteger cacheSize;
    private final UserManagedCache<Integer, Pattern> localCache;

    RegexPatternCache(final long cacheEntryLifetimeSeconds) {
        final Duration timeToLiveExpiration = Duration.ofSeconds(cacheEntryLifetimeSeconds);

        this.localCache = UserManagedCacheBuilder
                .newUserManagedCacheBuilder(Integer.class, Pattern.class)
                .withResourcePools(ResourcePoolsBuilder.heap(500L))
                .identifier(this.getClass().getSimpleName())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(timeToLiveExpiration))
                .build(true);

        this.cacheSize = new AtomicInteger(0);
    }

    @Override
    public UserManagedCache<Integer, Pattern> cache() {
        return localCache;
    }

    @Override
    public AtomicInteger size() {
        return cacheSize;
    }
}
