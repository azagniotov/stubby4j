package io.github.azagniotov.stubby4j.common;

/**
 * Custom class to support http methods which are not supported currently by org.eclipse.jetty.http.HttpMethod.
 */

public final class CustomHttpMethods {

    /**
     * HTTP PATCH method.
     */
    public static final String PATCH = "PATCH";

    private CustomHttpMethods() {
    }
}