package io.github.azagniotov.stubby4j.common;

import io.github.azagniotov.stubby4j.http.HttpMethodExtended;
import org.eclipse.jetty.http.HttpMethod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Common {

    public static final Set<String> POSTING_METHODS = Collections.unmodifiableSet(new HashSet<String>() {{
        add(HttpMethod.PUT.asString());
        add(HttpMethod.POST.asString());
        // PATCH is not a part of org.eclipse.jetty.http.HttpMethod
        add(HttpMethodExtended.PATCH.asString());
    }});

    public static final String HEADER_APPLICATION_JSON = "application/json";
    public static final String HEADER_APPLICATION_XML = "application/xml";
    public static final String HEADER_X_STUBBY_RESOURCE_ID = "x-stubby-resource-id";
    public static final String HEADER_X_STUBBY_PROXY_CONFIG = "x-stubby4j-proxy-config-uuid";
    public static final String HEADER_X_STUBBY_PROXY_REQUEST = "x-stubby4j-proxy-request-uuid";
    public static final String HEADER_X_STUBBY_PROXY_RESPONSE = "x-stubby4j-proxy-response-uuid";
    public static final String HEADER_X_STUBBY_HTTP_ERROR_REAL_REASON = "x-stubby4j-http-error-real-reason";

    private Common() {

    }
}
