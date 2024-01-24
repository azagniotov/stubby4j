/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.util.Locale;

final class StubbyRequest {

    private static final String URL_TEMPLATE = "%s://%s:%s%s";

    private final String scheme;
    private final String method;
    private final String uri;
    private final String host;
    private final String post;
    private final Authorization authorization;
    private final int clientPort;

    StubbyRequest(
            final String scheme,
            final String method,
            final String uri,
            final String host,
            final int port,
            final Authorization authorization) {
        this(scheme, method, uri, host, port, authorization, null);
    }

    StubbyRequest(
            final String scheme,
            final String method,
            final String uri,
            final String host,
            final int clientPort,
            final Authorization authorization,
            final String post) {
        this.scheme = scheme;
        this.method = method;
        this.uri = uri;
        this.host = host;
        this.clientPort = clientPort;
        this.post = post;
        this.authorization = authorization;
    }

    String getMethod() {
        return method;
    }

    String getPost() {
        return StringUtils.isSet(post) ? post : "";
    }

    Authorization getAuthorization() {
        return authorization;
    }

    String constructFullUrl() {
        return String.format(
                URL_TEMPLATE, scheme.toLowerCase(Locale.US), host, clientPort, StringUtils.isSet(uri) ? uri : "");
    }

    int calculatePostLength() {
        return StringUtils.calculateStringLength(post);
    }
}
