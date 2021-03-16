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
    public static final String HEADER_X_STUBBY_PROXIED_REQUEST = "x-stubby4j-proxied-request";
    public static final String HEADER_X_STUBBY_PROXIED_RESPONSE = "x-stubby4j-proxied-response";

    private Common() {

    }
}
