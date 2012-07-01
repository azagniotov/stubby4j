package org.stubby.server;

import org.eclipse.jetty.server.Server;
import org.stubby.database.Repository;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/30/12, 5:18 PM
 */
public final class JettyOrchestratorFactory {

   private static JettyOrchestrator jettyOrchestrator = null;

   JettyOrchestratorFactory() {

   }

   public static JettyOrchestrator getInstance(final String yamlConfigFilename, final Map<String, String> commandLineArgs) throws IOException {
      if (jettyOrchestrator == null) {
         final YamlConsumer yamlConsumer = new YamlConsumer(yamlConfigFilename);
         final List<StubHttpLifecycle> httpLifecycles = yamlConsumer.parseYaml();
         final Repository repository = new Repository(httpLifecycles);
         repository.init();
         jettyOrchestrator = new JettyOrchestrator(new Server(), repository, commandLineArgs);
      }
      return jettyOrchestrator;
   }
}
