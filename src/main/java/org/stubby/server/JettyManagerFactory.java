package org.stubby.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.stubby.cli.EmptyLogger;
import org.stubby.database.DataStore;
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 11:29 PM
 */
public final class JettyManagerFactory {

   public JettyManagerFactory() {

   }

   public JettyManager construct(final String yamlConfigFilename, final Map<String, String> commandLineArgs) throws Exception {

      synchronized (JettyManagerFactory.class) {
         Log.setLog(new EmptyLogger());

         final YamlParser yamlParser = new YamlParser(yamlConfigFilename);
         final List<StubHttpLifecycle> httpLifecycles = yamlParser.load(yamlParser.buildYamlReaderFromFilename());
         final DataStore dataStore = new DataStore(httpLifecycles);
         final JettyFactory jettyFactory = new JettyFactory(commandLineArgs, dataStore, yamlParser);
         final Server server = jettyFactory.construct(dataStore, yamlParser);

         return new JettyManager(server);
      }
   }
}
