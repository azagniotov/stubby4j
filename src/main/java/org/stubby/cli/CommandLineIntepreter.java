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

package org.stubby.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:41 PM
 */
public final class CommandLineIntepreter {

   private static CommandLine line = null;
   private final static CommandLineParser parser = new PosixParser();
   private final static Options options = new Options();

   public static final String OPTION_ADDRESS = "address";
   public static final String OPTION_CLIENTPORT = "clientport";
   public static final String OPTION_ADMINPORT = "adminport";
   public static final String OPTION_CONFIG = "config";
   private static final String OPTION_HELP = "help";

   static {
      options.addOption("a", OPTION_ADDRESS, true, "Host address that stubby4j should run on");
      options.addOption("c", OPTION_CLIENTPORT, true, "Port for incoming client requests");
      options.addOption("m", OPTION_ADMINPORT, true, "Port for admin status check requests");
      options.addOption("f", OPTION_CONFIG, true, "YAML file with request/response configuration");
      options.addOption("h", OPTION_HELP, false, "This help message");
   }

   private CommandLineIntepreter() {

   }

   public static void parseCommandLine(final String[] args) {
      try {
         line = parser.parse(options, args);
      } catch (ParseException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }

   private static String getCurrentJarLocation(final Class theclass) {
      final URL location = theclass.getProtectionDomain().getCodeSource().getLocation();
      try {
         final String jar = new File(location.getFile()).getName();
         if (jar.toLowerCase().endsWith(".jar")) {
            return jar;
         }
         return "stubby4j-x.x.x-SNAPSHOT.jar";
      } catch (Exception ignored) {
         return "stubby4j-x.x.x-SNAPSHOT.jar";
      }
   }

   public static boolean isYamlProvided() {
      return line.hasOption(OPTION_CONFIG);
   }

   public static boolean isHelp() {
      return line.hasOption(OPTION_HELP);
   }

   public static void printHelp(final Class theclass) {
      final HelpFormatter formatter = new HelpFormatter();
      final String command = String.format("\njava -jar %s", getCurrentJarLocation(theclass));
      formatter.printHelp(command, options, true);
   }

   public static Map<String, String> getCommandlineParams() {

      final Map<String, String> params = new HashMap<String, String>();
      if (line.hasOption(OPTION_ADDRESS)) {
         params.put(OPTION_ADDRESS, line.getOptionValue(OPTION_ADDRESS));
      }

      if (line.hasOption(OPTION_CLIENTPORT)) {
         params.put(OPTION_CLIENTPORT, line.getOptionValue(OPTION_CLIENTPORT));
      }

      if (line.hasOption(OPTION_ADMINPORT)) {
         params.put(OPTION_ADMINPORT, line.getOptionValue(OPTION_ADMINPORT));
      }

      if (line.hasOption(OPTION_CONFIG)) {
         params.put(OPTION_CONFIG, line.getOptionValue(OPTION_CONFIG));
      }

      return params;
   }
}
