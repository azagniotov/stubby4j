/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.client.http;

import by.stub.utils.StringUtils;

final class ClientHttpRequest {

   private final String scheme;
   private final String method;
   private final String uri;
   private final String host;
   private final String post;
   private final String base64encodedCredentials;
   private final int clientPort;

   ClientHttpRequest(final String scheme,
                     final String newMethod,
                     final String newUri,
                     final String newHost,
                     final int newClientPort) {
      this(scheme, newMethod, newUri, newHost, newClientPort, null, null);
   }

   ClientHttpRequest(final String scheme,
                     final String newMethod,
                     final String newUri,
                     final String newHost,
                     final int newClientPort,
                     final String newBase64encodedCredentials) {
      this(scheme, newMethod, newUri, newHost, newClientPort, newBase64encodedCredentials, null);
   }

   ClientHttpRequest(final String scheme,
                     final String method,
                     final String uri,
                     final String host,
                     final int clientPort,
                     final String newBase64encodedCredentials,
                     final String post) {
      this.scheme = scheme;
      this.method = method;
      this.uri = uri;
      this.host = host;
      this.clientPort = clientPort;
      this.post = post;
      this.base64encodedCredentials = newBase64encodedCredentials;
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

   public String getPost() {
      return StringUtils.isSet(post) ? post : "";
   }

   public String getBase64encodedCredentials() {
      return base64encodedCredentials;
   }

   public int getClientPort() {
      return clientPort;
   }

   public String getScheme() {
      return scheme;
   }
}
