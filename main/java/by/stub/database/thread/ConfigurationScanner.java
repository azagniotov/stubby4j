package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class ConfigurationScanner implements Runnable {

   private final YamlParser yamlParser;
   private final DataStore dataStore;

   public ConfigurationScanner(final YamlParser yamlParser, final DataStore dataStore) {
      this.yamlParser = yamlParser;
      this.dataStore = dataStore;
   }

   @Override
   public void run() {

      try {
         final String loadedConfigYamlPath = yamlParser.getLoadedConfigYamlPath();
         final File loadedConfig = new File(loadedConfigYamlPath);
         long lastModified = loadedConfig.lastModified();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(3000);

            final long currentFileModified = loadedConfig.lastModified();
            if (lastModified >= currentFileModified) {
               continue;
            }

            try {
               lastModified = currentFileModified;
               final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad(loadedConfigYamlPath);

               dataStore.resetStubHttpLifecycles(stubHttpLifecycles);
               ANSITerminal.ok(String.format("%sSuccessfully performed live reload of YAML configuration from: %s%s",
                     "\n",
                     loadedConfigYamlPath,
                     "\n"));
            } catch (final Exception ex) {
               ANSITerminal.warn("Could not reload YAML configuration: " + ex.toString());
               ANSITerminal.error(String.format("%sFailed to perform live reload of YAML configuration from: %s%s",
                     "\n",
                     loadedConfigYamlPath,
                     "\n"));
            }
         }

      } catch (final Exception ex) {
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }

   public void stopScanning() {
      Thread.currentThread().interrupt();
   }
}