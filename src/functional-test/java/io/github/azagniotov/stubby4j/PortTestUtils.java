package io.github.azagniotov.stubby4j;

public final class PortTestUtils {

    private PortTestUtils() {

    }

    public static int findAvailableTcpPort() {
        return SpringSocketUtils.findAvailableTcpPort(9152, 65535);
    }
}
