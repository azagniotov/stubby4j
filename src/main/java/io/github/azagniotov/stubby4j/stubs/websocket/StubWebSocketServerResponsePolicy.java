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

package io.github.azagniotov.stubby4j.stubs.websocket;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum StubWebSocketServerResponsePolicy {
    ONCE("once"),
    PUSH("push"),
    FRAGMENTATION("fragmentation"),
    PING("ping"),
    DISCONNECT("disconnect");

    private static final Map<String, StubWebSocketServerResponsePolicy> PROPERTY_NAME_TO_ENUM_MEMBER;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final StubWebSocketServerResponsePolicy enumMember :
                EnumSet.allOf(StubWebSocketServerResponsePolicy.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    private final String value;

    StubWebSocketServerResponsePolicy(final String value) {
        this.value = value;
    }

    public static Optional<StubWebSocketServerResponsePolicy> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty)));
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    @Override
    public String toString() {
        return toLower(this.value);
    }
}
