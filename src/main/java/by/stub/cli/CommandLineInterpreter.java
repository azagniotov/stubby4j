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

package by.stub.cli;

import by.stub.utils.StringUtils;
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

public class CommandLineInterpreter {

   private static CommandLine line;
   private static final CommandLineParser POSIX_PARSER = new PosixParser();
   private static final Options OPTIONS = new Options();

   public static final String OPTION_ADDRESS = "location";
   public static final String OPTION_CLIENTPORT = "stubs";
   public static final String OPTION_SSLPORT = "ssl";
   public static final String OPTION_ADMINPORT = "admin";
   public static final String OPTION_CONFIG = "data";
   public static final String OPTION_KEYSTORE = "keystore";
   public static final String OPTION_KEYPASS = "password";
   public static final String OPTION_MUTE = "mute";
   public static final String OPTION_WATCH = "watch";
   public static final String OPTION_DEBUG = "debug";

   private static final String[] ALL_OPTIONS = {OPTION_ADDRESS, OPTION_CLIENTPORT, OPTION_SSLPORT, OPTION_ADMINPORT, OPTION_CONFIG, OPTION_KEYSTORE, OPTION_KEYPASS};

   public static final String OPTION_HELP = "help";

   static {
      OPTIONS.addOption("l", OPTION_ADDRESS, true, "Hostname at which to bind stubby.");
      OPTIONS.addOption("s", OPTION_CLIENTPORT, true, "Port for stub portal. Defaults to 8882.");
      OPTIONS.addOption("a", OPTION_ADMINPORT, true, "Port for admin portal. Defaults to 8889.");
      OPTIONS.addOption("t", OPTION_SSLPORT, true, "Port for SSL connection. Defaults to 7443.");
      OPTIONS.addOption("d", OPTION_CONFIG, true, "Data file to pre-load endpoints. Valid YAML 1.1 expected.");
      OPTIONS.addOption("k", OPTION_KEYSTORE, true, "Keystore file for custom SSL. By default SSL is enabled using internal keystore.");
      OPTIONS.addOption("p", OPTION_KEYPASS, true, "Password for the provided keystore file.");
      OPTIONS.addOption("h", OPTION_HELP, false, "This help text.");
      OPTIONS.addOption("m", OPTION_MUTE, false, "Prevent stubby from printing to the console.");
      OPTIONS.addOption("w", OPTION_WATCH, false, "Reload datafile when changes are made.");
      OPTIONS.addOption(null, OPTION_DEBUG, false, "Show comparison print-outs when endpoints are hit.");
   }

   private CommandLineInterpreter() {

   }

   public static void parseCommandLine(final String[] args) throws ParseException {
      line = POSIX_PARSER.parse(OPTIONS, args);
   }

   /**
    * Returns stubby4j JAR path, relative to current execution directory under local filesystem based
    * on the given class executed within the JAR.
    *
    * @param theclass Class executed within the JAR. The class discloses JAR's current location
    * @return relative stubby4j JAR path
    */
   public static String getCurrentJarLocation(final Class theclass) {
      final URL location = theclass.getProtectionDomain().getCodeSource().getLocation();

      final String jarAbsolutePath = new File(location.getFile()).getAbsolutePath();
      final String jar = jarAbsolutePath.replaceAll(System.getProperty("user.dir") + "/", "");

      if (StringUtils.toLower(jar).endsWith(".jar")) {
         return jar;
      }

      return "stubby4j-x.x.x-SNAPSHOT.jar";
   }

   /**
    * Checks if console output has been muted
    *
    * @return true if the user disabled output to console using command line arg
    */
   public static boolean isMute() {
      return line.hasOption(OPTION_MUTE);
   }

   /**
    * Checks if console output has been muted
    *
    * @return true if the user disabled output to console using command line arg
    */
   public static boolean isDebug() {
      return line.hasOption(OPTION_DEBUG);
   }

   /**
    * Checks if the watch flag was given
    *
    * @return true if the user wants stubby to auto-update the datafile when changes are made
    */
   public static boolean isWatching() {
      return line.hasOption(OPTION_WATCH);
   }

   /**
    * Checks if data YAML was provided
    *
    * @return true if the user provided stub data as YAML file with using command line arg
    */
   public static boolean isYamlProvided() {
      return line.hasOption(OPTION_CONFIG);
   }

   /**
    * Checks if help option was provided
    *
    * @return true if the user has provided 'help' command line arg
    */
   public static boolean isHelp() {
      return line.hasOption(OPTION_HELP);
   }

   /**
    * Prints 'help' message which describes avilable command line arguments
    *
    * @param theclass The class that discloses JAR's current location
    */
   public static void printHelp(final Class theclass) {
      final HelpFormatter formatter = new HelpFormatter();
      final String command = String.format("%sjava -jar %s", "\n", getCurrentJarLocation(theclass));
      formatter.printHelp(command, OPTIONS, true);
   }

   /**
    * Identifies what command line arguments from available have been passed by user
    *
    * @return a map of passed command line arguments where key is the name of the argument
    */
   public static Map<String, String> getCommandlineParams() {

      final Map<String, String> params = new HashMap<String, String>();
      for (final String option : ALL_OPTIONS) {
         if (line.hasOption(option)) {
            params.put(option, line.getOptionValue(option));
         }
      }
      return params;
   }
}
