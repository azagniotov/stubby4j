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
      } else if (!CommandLineIntepreter.hasYaml()) {
         throw new Exception("Command line option '-f' or '--config' with YAML configuration file was not provided.\nPlease run again with option '--help'");
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
