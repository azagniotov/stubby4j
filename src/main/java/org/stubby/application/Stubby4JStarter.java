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

package org.stubby.application;

import org.apache.commons.cli.ParseException;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.server.JettyOrchestratorFactory;

import java.util.Map;

final class Stubby4JStarter {

   private Stubby4JStarter() {

   }

   public static void main(final String[] args) {

      try {
         CommandLineIntepreter.parseCommandLine(args);
      } catch (ParseException e) {
         e.printStackTrace();
         System.exit(1);
      }
      if (CommandLineIntepreter.isHelp()) {
         CommandLineIntepreter.printHelp(Stubby4JStarter.class);

      } else if (!CommandLineIntepreter.isYamlProvided()) {
         System.err.println("\n\nYAML configuration was not provided using command line option '-f' or '--config'.\nPlease run again with option '--help'\n\n");
         System.exit(1);

      } else {

         try {
            final Map<String, String> commandLineArgs = CommandLineIntepreter.getCommandlineParams();
            final String yamlConfigFilename = commandLineArgs.get(CommandLineIntepreter.OPTION_CONFIG);

            JettyOrchestratorFactory.getInstance(yamlConfigFilename, commandLineArgs).startJetty();

         } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }
   }
}