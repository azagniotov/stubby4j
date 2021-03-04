package io.github.azagniotov.stubby4j;

import static io.github.azagniotov.stubby4j.SpringSocketUtils.PORT_RANGE_MAX;
import static io.github.azagniotov.stubby4j.SpringSocketUtils.PORT_RANGE_MIN;

final class PortTestUtils {

    private PortTestUtils() {

    }

    static int findAvailableTcpPort() {
        return SpringSocketUtils.findAvailableTcpPort(PORT_RANGE_MIN, PORT_RANGE_MAX);
    }
}
