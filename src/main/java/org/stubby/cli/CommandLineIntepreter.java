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

   static {
      options.addOption("a", "address", true, "Stub server address");
      options.addOption("c", "clientport", true, "Port that HTTP request consumer runs on");
      options.addOption("m", "adminport", true, "Port that stubby4j admin runs on");
      options.addOption("f", "config", true, "YAML file with request/response configuration");
      options.addOption("h", "help", false, "This help message");
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
         if (jar.endsWith(".jar")) {
            return jar;
         }
         return "project.jar";
      } catch (Exception ignored) {
         return "project.jar";
      }
   }

   public static boolean isHelp() {
      return line.hasOption("h");
   }

   public static void printHelp(final Class theclass) {
      final HelpFormatter formatter = new HelpFormatter();
      final String command = String.format("\njava -jar %s", getCurrentJarLocation(theclass));
      formatter.printHelp(command, options, true);
   }

   public static Map<String, String> getCommandlineParams() {

      final Map<String, String> params = new HashMap<String, String>();
      if (line.hasOption("a")) {
         params.put("address", line.getOptionValue("a"));
      }

      if (line.hasOption("c")) {
         params.put("clientport", line.getOptionValue("c"));
      }

      if (line.hasOption("m")) {
         params.put("adminport", line.getOptionValue("m"));
      }

      if (line.hasOption("f")) {
         params.put("config", line.getOptionValue("f"));
      }

      return params;
   }
}
