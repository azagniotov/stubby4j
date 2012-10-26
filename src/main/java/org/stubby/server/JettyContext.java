package org.stubby.server;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 8:54 AM
 */
public final class JettyContext {

   private final String host;
   private final boolean sslEnabled;
   private final int stubsPort;
   private final int adminPort;

   public JettyContext(final String host, final boolean sslEnabled, final int stubsPort, final int adminPort) {
      this.host = host;
      this.sslEnabled = sslEnabled;
      this.stubsPort = stubsPort;
      this.adminPort = adminPort;
   }

   public boolean isSslEnabled() throws Exception {
      return sslEnabled;
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
