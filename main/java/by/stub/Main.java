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

package by.stub;

import by.stub.cli.ANSITerminal;
import by.stub.cli.CommandLineInterpreter;
import by.stub.exception.Stubby4JException;
import by.stub.server.StubbyManager;
import by.stub.server.StubbyManagerFactory;
import org.apache.commons.cli.ParseException;

import java.util.Map;

public final class Main {

   private static CommandLineInterpreter commandLineInterpreter;

   private Main() {

   }

   public static void main(final String[] args) {

      commandLineInterpreter = new CommandLineInterpreter();

      parseCommandLineArgs(args);
      if (printHelpIfRequested()) {
         return;
      }

      verifyYamlDataProvided();
      startStubby4jUsingCommandLineArgs();
   }

   private static void parseCommandLineArgs(final String[] args) {
      try {
         commandLineInterpreter.parseCommandLine(args);
      } catch (final ParseException ex) {
         final String msg =
            String.format("Could not parse provided command line arguments, error: %s",
               ex.toString());

         throw new Stubby4JException(msg);
      }
   }

   private static boolean printHelpIfRequested() {
      if (!commandLineInterpreter.isHelp()) {
         return false;
      }

      commandLineInterpreter.printHelp();

      return true;
   }

   private static void verifyYamlDataProvided() {
      if (commandLineInterpreter.isYamlProvided()) {
         return;
      }
      final String msg =
         String.format("YAML data was not provided using command line option '--%s'. %s"
            + "To see all command line options run again with option '--%s'",
            CommandLineInterpreter.OPTION_CONFIG, "\n", CommandLineInterpreter.OPTION_HELP);

      throw new Stubby4JException(msg);
   }

   private static void startStubby4jUsingCommandLineArgs() {
      try {
         final Map<String, String> commandLineArgs = commandLineInterpreter.getCommandlineParams();
         final String yamlConfigFilename = commandLineArgs.get(CommandLineInterpreter.OPTION_CONFIG);

         ANSITerminal.muteConsole(commandLineInterpreter.isMute());

         final StubbyManagerFactory factory = new StubbyManagerFactory();
         final StubbyManager stubbyManager = factory.construct(yamlConfigFilename, commandLineArgs);
         stubbyManager.startJetty();

      } catch (final Exception ex) {
         final String msg =
            String.format("Could not init stubby4j, error: %s", ex.toString());

         throw new Stubby4JException(msg, ex);
      }
   }
}