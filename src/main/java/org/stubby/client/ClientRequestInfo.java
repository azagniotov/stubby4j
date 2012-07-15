package org.stubby.client;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 12:07 PM
 */
public final class ClientRequestInfo {

   private final String method;
   private final String uri;
   private final String host;
   private final String postBody;
   private final String base64encodedCredentials;
   private final int clientPort;

   public ClientRequestInfo(final String method,
                            final String uri,
                            final String host,
                            final int clientPort) {
      this(method, uri, host, clientPort, null, null);
   }

   public ClientRequestInfo(final String method,
                            final String uri,
                            final String host,
                            final int clientPort,
                            final String postBody) {
      this(method, uri, host, clientPort, postBody, null);
   }

   public ClientRequestInfo(final String method,
                            final String uri,
                            final String host,
                            final int clientPort,
                            final String postBody,
                            final String base64encodedCredentials) {
      this.method = method;
      this.uri = uri;
      this.host = host;
      this.clientPort = clientPort;
      this.postBody = postBody;
      this.base64encodedCredentials = base64encodedCredentials;
   }

   public String getMethod() {
      return method;
   }

   public String getUri() {
      return uri;
   }

   public String getHost() {
      return host;
   }

   public String getPostBody() {
      return postBody;
   }

   public String getBase64encodedCredentials() {
      return base64encodedCredentials;
   }

   public int getClientPort() {
      return clientPort;
   }
}
