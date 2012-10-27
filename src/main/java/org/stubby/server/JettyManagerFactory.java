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

package org.stubby.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.stubby.cli.EmptyLogger;
import org.stubby.database.DataStore;
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.util.List;
import java.util.Map;

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
