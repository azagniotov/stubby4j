package io.github.azagniotov.stubby4j.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for exclusion of class from JaCoCo code coverage instrumentation.
 * <p>
 * Required by JaCoCo starting from v0.83
 * https://github.com/jacoco/jacoco/pull/822
 * https://www.jacoco.org/jacoco/trunk/doc/changes.html (Release 0.8.3 (2019/01/23))
 * https://github.com/jacoco/jacoco/blob/f72c2c865fa7c975debb1b3156120501843f5c74/org.jacoco.core/src/org/jacoco/core/internal/analysis/filter/AnnotationGeneratedFilter.java#L51
 * By default, annotation retention policy is CLASS, which does not make your annotation available to reflection
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface GeneratedCodeClassCoverageExclusion {

}
