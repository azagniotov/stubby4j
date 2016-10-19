package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;

public final class YamlProperties {

    public static final String BODY = "body";
    public static final String FILE = "file";
    public static final String HEADERS = "headers";
    public static final String HTTPLIFECYCLE = "httplifecycle";
    public static final String METHOD = "method";
    public static final String POST = "post";
    public static final String QUERY = "query";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String STATUS = "status";
    public static final String URL = "url";

    @CoberturaIgnore
    private YamlProperties() {

    }
}
