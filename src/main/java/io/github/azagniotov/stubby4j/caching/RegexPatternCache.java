package io.github.azagniotov.stubby4j.caching;


import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

class RegexPatternCache implements Cache<Integer, Pattern> {

   private final AtomicInteger size;
   private final UserManagedCache<Integer, Pattern> cache;

   RegexPatternCache(final long cacheEntryLifetimeSeconds) {
      final Duration timeToLiveExpiration = Duration.ofSeconds(cacheEntryLifetimeSeconds);

      this.cache = UserManagedCacheBuilder
         .newUserManagedCacheBuilder(Integer.class, Pattern.class)
         .withResourcePools(ResourcePoolsBuilder.heap(500L))
         .identifier(this.getClass().getSimpleName())
         .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(timeToLiveExpiration))
         .build(true);

      this.size = new AtomicInteger(0);
   }

   @Override
   public UserManagedCache<Integer, Pattern> cache() {
      return cache;
   }

   @Override
   public AtomicInteger size() {
      return size;
   }
}
