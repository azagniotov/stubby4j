package io.github.azagniotov.stubby4j.server;


public final class JettyContext {

    private final String host;
    private final int stubsSslPort;
    private final int stubsPort;
    private final int adminPort;

    public JettyContext(final String host, final int stubsPort, final int stubsSslPort, final int adminPort) {
        this.host = host;
        this.stubsSslPort = stubsSslPort;
        this.stubsPort = stubsPort;
        this.adminPort = adminPort;
    }

    public int getStubsTlsPort() {
        return stubsSslPort;
    }

    public int getStubsPort() {
        return stubsPort;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public String getHost() {
        return host;
    }
}
