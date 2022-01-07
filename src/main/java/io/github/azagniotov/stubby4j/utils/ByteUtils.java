package io.github.azagniotov.stubby4j.utils;

import java.nio.ByteBuffer;

public final class ByteUtils {

    private ByteUtils() {

    }

    public static byte[] extractByteArrayFromByteBuffer(final ByteBuffer byteBuffer) {
        // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/ByteBuffer.html#hasArray()
        if (!byteBuffer.hasArray()) {
            final byte[] to = new byte[byteBuffer.remaining()];
            byteBuffer.slice().get(to);

            return to;
        } else {
            return byteBuffer.array();
        }
    }
}
