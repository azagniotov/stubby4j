package org.stubby;

import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;
import org.stubby.server.JettyOrchestrator;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public final class Stubby4JRunner {

   public static void main(String[] args) {

      if (CommandLineIntepreter.isHelp(args)) {
         CommandLineIntepreter.printDefaultCommandSample(Stubby4JRunner.class);
      } else {
         final String pathToYamlFile = args[0];
         final Repository repository = startDatabase(pathToYamlFile);
         try {
            JettyOrchestrator.startJetty(repository, args);
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
