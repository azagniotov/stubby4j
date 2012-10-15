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
import org.stubby.cli.ANSITerminal;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.server.JettyOrchestratorFactory;

import java.awt.*;
import java.util.Map;

final class Stubby4JStarter {

   private Stubby4JStarter() {

   }

   public static void main(final String[] args) {

      parseCommandLineArgs(args);
      printHelpIfRequested();
      verifyYamlDataProvided();
      startStubby4jUsingCommandLineArgs();
   }

   private static void parseCommandLineArgs(final String[] args) {
      try {
         CommandLineIntepreter.parseCommandLine(args);
      } catch (final ParseException ex) {
         final String msg = String.format("Could not parse provided command line arguments, error: %s", ex.toString());
         System.err.println(msg);
         System.exit(1);
      }
   }

   private static void printHelpIfRequested() {
      if (CommandLineIntepreter.isHelp()) {
         CommandLineIntepreter.printHelp(Stubby4JStarter.class);
         System.exit(0);
      }
   }

   private static void verifyYamlDataProvided() {
      if (!CommandLineIntepreter.isYamlProvided()) {
         final String msg = String.format("YAML data was not provided using command line option '--%s'. \nTo see all command line options run again with option '--%s'",
               CommandLineIntepreter.OPTION_CONFIG,
               CommandLineIntepreter.OPTION_HELP);
         System.err.println(msg);
         System.exit(1);
      }
   }

   private static void startStubby4jUsingCommandLineArgs() {
      try {
         final Map<String, String> commandLineArgs = CommandLineIntepreter.getCommandlineParams();
         final String yamlConfigFilename = commandLineArgs.get(CommandLineIntepreter.OPTION_CONFIG);

         ANSITerminal.mute = CommandLineIntepreter.isMute();

         JettyOrchestratorFactory.getInstance(yamlConfigFilename, commandLineArgs).startJetty();

      } catch (final Exception ex) {
         final String msg = String.format("Could not start stubby4j, error: %s", ex.toString());
         System.err.println(msg);
         System.exit(1);
      }
   }
}