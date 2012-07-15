package org.stubby.client;

import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 7/13/12, 10:57 PM
 */
public interface Stubby4JClient {

   void start() throws Exception;

   void start(final int clientPort, final int adminPort) throws Exception;

   void stop() throws Exception;

   Stubby4JResponse makeRequestWith(final ClientRequestInfo clientRequest) throws IOException;
}