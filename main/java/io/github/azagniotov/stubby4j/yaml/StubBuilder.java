package io.github.azagniotov.stubby4j.yaml;

/**
 * @author: Alexander Zagniotov
 * Created: 8/12/13 5:08 PM
 */
interface StubBuilder<T> {
   T build() throws Exception;

   void store(final String fieldName, final Object fieldValue);
}
