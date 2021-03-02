package io.github.azagniotov.stubby4j.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for exclusion of method or class from code coverage instrumentation.
 * <p>
 * Required by JaCoCo starting from v0.83
 * https://github.com/jacoco/jacoco/pull/822
 * https://www.jacoco.org/jacoco/trunk/doc/changes.html (Release 0.8.3 (2019/01/23))
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface GeneratedCodeCoverageExclusion {

}
