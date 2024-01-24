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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class StubWebSocketServerResponsePolicyTest {

    @Test
    public void returnsTrueOnKnownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("once")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("push")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("fragmentation"))
                .isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("ping")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("disconnect"))
                .isFalse();
    }

    @Test
    public void returnsFalseOnUnknownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("apple")).isTrue();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("")).isTrue();
    }
}
