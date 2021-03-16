package io.github.azagniotov.stubby4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PotentiallyFlaky {
    /**
     * The optional reason why the test is considered flaky.
     */
    String value() default "";
}
