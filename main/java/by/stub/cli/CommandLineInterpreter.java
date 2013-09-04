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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.util.HashMap;
import java.util.Map;

public final class CommandLineInterpreter {

   public static final String OPTION_ADDRESS = "location";
   public static final String OPTION_CLIENTPORT = "stubs";
   public static final String OPTION_SSLPORT = "ssl";
   public static final String OPTION_ADMINPORT = "admin";
   public static final String OPTION_CONFIG = "data";
   public static final String OPTION_KEYSTORE = "keystore";
   public static final String OPTION_KEYPASS = "password";
   public static final String OPTION_MUTE = "mute";
   public static final String OPTION_WATCH = "watch";
   public static final String OPTION_HELP = "help";

   private static final CommandLineParser POSIX_PARSER = new PosixParser();
   private static final Options OPTIONS = new Options();
   private CommandLine line;

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
      Option watch =
         OptionBuilder
            .withDescription("Periodically scans for changes in last modification date of the main YAML and referenced external files (if any). The flag can accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms. If last modification date changed since the last scan period, the stub configuration is reloaded")
            .withLongOpt(OPTION_WATCH)
            .hasOptionalArg()
            .create("w");
      OPTIONS.addOption(watch);
   }


   public void parseCommandLine(final String[] args) throws ParseException {
      line = POSIX_PARSER.parse(OPTIONS, args);
   }

   /**
    * Checks if console output has been muted
    *
    * @return true if the user disabled output to console using command line arg
    */
   public boolean isMute() {
      return line.hasOption(OPTION_MUTE);
   }

   /**
    * Checks if data YAML was provided
    *
    * @return true if the user provided stub data as YAML file with using command line arg
    */
   public boolean isYamlProvided() {
      return line.hasOption(OPTION_CONFIG);
   }

   /**
    * Checks if help option was provided
    *
    * @return true if the user has provided 'help' command line arg
    */
   public boolean isHelp() {
      return line.hasOption(OPTION_HELP);
   }

   /**
    * Prints 'help' message which describes avilable command line arguments
    */
   public void printHelp() {
      final HelpFormatter formatter = new HelpFormatter();
      final String command = String.format("%sjava -jar stubby4j-x.x.xx.jar", "\n");
      formatter.printHelp(command, OPTIONS, true);
   }

   /**
    * Identifies what command line arguments that have been passed by user are matching available options
    *
    * @return a map of passed command line arguments where key is the name of the argument
    */
   public Map<String, String> getCommandlineParams() {

      final Option[] options = line.getOptions();

      return new HashMap<String, String>() {{
         for (final Option option : options) {
            put(option.getLongOpt(), option.getValue());
         }
      }};
   }
}
