/*
A Java-based HTTP stub server

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

package org.stubby.server;

import org.eclipse.jetty.server.Server;
import org.stubby.database.DataStore;
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
         final DataStore dataStore = new DataStore(httpLifecycles);
         jettyOrchestrator = new JettyOrchestrator(new Server(), dataStore, commandLineArgs);
      }
      return jettyOrchestrator;
   }
}
