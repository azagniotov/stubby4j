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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StubbyResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headerFields;

    public StubbyResponse(final int statusCode, final String body, final Map<String, List<String>> headerFields) {
        this.statusCode = statusCode;
        this.body = body;
        this.headerFields = headerFields;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public Map<String, List<String>> headers() {
        return new HashMap<>(headerFields);
    }
}
