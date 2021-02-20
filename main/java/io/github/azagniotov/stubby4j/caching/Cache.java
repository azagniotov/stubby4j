package io.github.azagniotov.stubby4j.caching;


import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.ehcache.UserManagedCache;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Cache<K, V> {

   static Cache<String, StubHttpLifecycle> stubHttpLifecycleCache(final long cacheEntryLifetimeSeconds) {
      return new StubHttpLifecycleCache(cacheEntryLifetimeSeconds);
   }

   static Cache<Integer, Pattern> regexPatternCache(final long cacheEntryLifetimeSeconds) {
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

   default boolean clearByKey(final K key) {
      if (cache().containsKey(key)) {
         cache().remove(key);
         size().decrementAndGet();

         return true;
      }

      return false;
   }

   default void clearByRegexKey(final String regex) {

      // TODO: If this will cause performance hit, use local map as cache for compiled Patterns
      final Pattern keyPattern = Pattern.compile(regex);

      final Set<K> removalCandidates = new HashSet<>();
      for (final org.ehcache.Cache.Entry<K, V> entry : cache()) {
         final String key = (String) entry.getKey();

         final Matcher matcher = keyPattern.matcher(key);
         if (matcher.find()) {
            removalCandidates.add(entry.getKey());
         }
      }

      for (final K toRemove : removalCandidates) {
         clearByKey(toRemove);
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
