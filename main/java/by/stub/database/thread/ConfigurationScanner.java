package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.utils.FileUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class ConfigurationScanner implements Runnable {

   private final DataStore dataStore;

   public ConfigurationScanner(final DataStore dataStore) {
      this.dataStore = dataStore;
      ANSITerminal.status(String.format("Configuration scan enabled, watching %s", dataStore.getDataYaml().getAbsolutePath()));
   }

   @Override
   public void run() {

      try {
         final File dataYaml = dataStore.getDataYaml();
         long lastModified = dataYaml.lastModified();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(3000);

            final long currentFileModified = dataYaml.lastModified();
            if (lastModified >= currentFileModified) {
               continue;
            }

            ANSITerminal.info(String.format("\nConfiguration scan detected change in %s\n", dataStore.getDataYaml().getAbsolutePath()));

            try {
               lastModified = currentFileModified;
               final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(dataYaml.getParent(), FileUtils.constructReader(dataYaml));

               dataStore.resetStubHttpLifecycles(stubHttpLifecycles);
               ANSITerminal.ok(String.format("%sSuccessfully performed live reload of YAML configuration from: %s%s",
                  "\n",
                  dataYaml.getAbsolutePath(),
                  "\n"));
            } catch (final Exception ex) {
               ANSITerminal.warn("Could not reload YAML configuration: " + ex.toString());
               ANSITerminal.error(String.format("%sFailed to perform live reload of YAML configuration from: %s%s",
                  "\n",
                  dataYaml.getAbsolutePath(),
                  "\n"));
            }
         }

      } catch (final Exception ex) {
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }
}