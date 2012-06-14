package org.stubby.cli;

import java.io.File;
import java.net.URL;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:41 PM
 */
public final class CommandLineIntepreter {

   private CommandLineIntepreter() {

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

   public static boolean isHelp(final String[] arguments) {
      return (arguments.length == 0) ||
            ((arguments.length == 1) &&
                  ("-h".equals(arguments[0]) ||
                        "--help".equals(arguments[0])));
   }

   public static void printDefaultCommandSample(final Class theclass) {
      System.out.printf("\njava -jar %s <path_to_yaml_file> <client_port>(Optional) <admin_port>(Optional)\n\n", getCurrentJarLocation(theclass));
   }
}
