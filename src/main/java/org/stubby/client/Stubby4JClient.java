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

   Stubby4JResponse registerStubData(final String yamlConfigurationContent, final String host, final int adminPort) throws Exception;

   Stubby4JResponse doGetOnURI(final String uri, final String host, final int clientPort) throws IOException;

   Stubby4JResponse doPostOnURI(final String uri, final String postData, final String host, final int clientPort) throws IOException;
}
