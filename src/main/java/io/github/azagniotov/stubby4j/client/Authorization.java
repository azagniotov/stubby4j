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

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeMethodCoverageExclusion;

public final class Authorization {

    private final AuthorizationType authorizationType;
    private final String value;

    public Authorization(final AuthorizationType authorizationType, final String value) {
        this.authorizationType = authorizationType;
        this.value = value;
    }

    public String asFullValue() {
        if (authorizationType == AuthorizationType.CUSTOM) {
            return value;
        } else {
            return String.format("%s %s", authorizationType.asString(), value);
        }
    }

    @Override
    @GeneratedCodeMethodCoverageExclusion
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Authorization");
        sb.append("{type=").append(authorizationType);
        sb.append(", value=").append(value);
        sb.append('}');

        return sb.toString();
    }

    enum AuthorizationType {
        BASIC("Basic"),
        BEARER("Bearer"),
        CUSTOM("Custom");
        private final String type;

        AuthorizationType(final String type) {
            this.type = type;
        }

        public String asString() {
            return type;
        }
    }
}
