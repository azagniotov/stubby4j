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
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubWebSocketClientRequestTest {

    private StubWebSocketClientRequest.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubWebSocketClientRequest.Builder();
    }

    @Test
    public void returnsBodyAsExpectedBytesWhenOnlyBodyStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK").build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("OK"));
    }

    @Test
    public void returnsBodyAsExpectedBytesWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withFile(tempFileFromString("Apple")).build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("Apple"));
    }

    @Test
    public void returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK").withFile(tempFileFromString("Banana")).build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("Banana"));
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyBodyStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK").build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("OK");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withFile(tempFileFromString("Apple")).build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("Apple");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK").withFile(tempFileFromString("Banana")).build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("Banana");
    }
}
