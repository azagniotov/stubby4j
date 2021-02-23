package io.github.azagniotov.stubby4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates that the visibility of a type or member has been relaxed to make the code testable.
 * Inspired by @VisibleForTesting from Google's Guava library
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
public @interface VisibleForTesting {
}
