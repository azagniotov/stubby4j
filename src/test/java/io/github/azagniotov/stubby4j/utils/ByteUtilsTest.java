package io.github.azagniotov.stubby4j.utils;


import org.junit.Test;

import java.nio.ByteBuffer;

import static com.google.common.truth.Truth.assertThat;

public class ByteUtilsTest {
    private static final String TANUKI = "The Japanese raccoon dog is known as the tanuki.";
    private static final byte[] ORIGINAL_STRING_BYTES = StringUtils.getBytesUtf8(TANUKI);

    @Test
    public void extractArrayFromByteBuffer() throws Exception {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(ORIGINAL_STRING_BYTES);
        final byte[] bytes = ByteUtils.extractByteArrayFromByteBuffer(byteBuffer);

        assertThat(StringUtils.newStringUtf8(bytes)).isEqualTo(TANUKI);
    }

    @Test
    public void extractArrayFromReadOnlyByteBuffer() throws Exception {
        final ByteBuffer readOnlyBuffer = ByteBuffer.wrap(ORIGINAL_STRING_BYTES).asReadOnlyBuffer();
        final byte[] bytes = ByteUtils.extractByteArrayFromByteBuffer(readOnlyBuffer);

        assertThat(StringUtils.newStringUtf8(bytes)).isEqualTo(TANUKI);
    }
}