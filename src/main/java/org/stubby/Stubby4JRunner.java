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

package org.stubby;

import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;
import org.stubby.server.JettyOrchestrator;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class Stubby4JRunner {

   public static void main(final String[] args) throws Exception {

      CommandLineIntepreter.parseCommandLine(args);

      if (CommandLineIntepreter.isHelp()) {
         CommandLineIntepreter.printHelp(Stubby4JRunner.class);
      } else if (!CommandLineIntepreter.isYamlProvided()) {
         System.err.println("\n\nYAML configuration was not provided using command line option '-f' or '--config'.\nPlease run again with option '--help'\n\n");
         System.exit(1);
      } else {
         final Map<String, String> params = CommandLineIntepreter.getCommandlineParams();
         final Repository repository = startDatabase(params.get("config"));
         try {
            JettyOrchestrator.startJetty(repository, params);
         } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }
   }

   private static Repository startDatabase(final String yamlConfigFilename) {
      try {
         final List<StubHttpLifecycle> httpLifecycles = YamlConsumer.readYaml(yamlConfigFilename);
         return new Repository(httpLifecycles);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         System.exit(1);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
      return null;
   }
}
