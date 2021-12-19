package io.github.azagniotov.stubby4j.stubs.websocket;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

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
                builder.withBody("OK")
                        .build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("OK"));
    }

    @Test
    public void returnsBodyAsExpectedBytesWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withFile(tempFileFromString("Apple"))
                        .build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("Apple"));
    }

    @Test
    public void returnsBodyAsExpectedFileBytesWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK")
                        .withFile(tempFileFromString("Banana"))
                        .build();
        assertThat(socketClientRequest.getBodyAsBytes()).isEqualTo(getBytesUtf8("Banana"));
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyBodyStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK")
                        .build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("OK");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenOnlyFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withFile(tempFileFromString("Apple"))
                        .build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("Apple");
    }

    @Test
    public void returnsBodyAsExpectedStringWhenBothBodyAndFileStubbed() throws Exception {
        final StubWebSocketClientRequest socketClientRequest =
                builder.withBody("OK")
                        .withFile(tempFileFromString("Banana"))
                        .build();
        assertThat(socketClientRequest.getBodyAsString()).isEqualTo("Banana");
    }
}