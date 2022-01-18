package io.github.azagniotov.stubby4j.utils;


import static io.github.azagniotov.stubby4j.utils.SpringSocketUtils.PORT_RANGE_MAX;
import static io.github.azagniotov.stubby4j.utils.SpringSocketUtils.PORT_RANGE_MIN;

public final class NetworkPortUtils {

    private NetworkPortUtils() {

    }

    public static int findAvailableTcpPort() {
        return SpringSocketUtils.findAvailableTcpPort(PORT_RANGE_MIN, PORT_RANGE_MAX);
    }
}
