package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
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

   private final StubbedDataManager stubbedDataManager;

   public ConfigurationScanner(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
      ANSITerminal.status(String.format("Configuration scan enabled, watching %s", stubbedDataManager.getDataYaml().getAbsolutePath()));
   }

   @Override
   public void run() {

      try {
         final File dataYaml = stubbedDataManager.getDataYaml();
         long lastModified = dataYaml.lastModified();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(3000);

            final long currentFileModified = dataYaml.lastModified();
            if (lastModified >= currentFileModified) {
               continue;
            }

            ANSITerminal.info(String.format("\nConfiguration scan detected change in %s\n", stubbedDataManager.getDataYaml().getAbsolutePath()));

            try {
               lastModified = currentFileModified;
               final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(dataYaml.getParent(), FileUtils.constructReader(dataYaml));

               stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);
               ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of YAML configuration from: %s%s",
                  "\n",
                  dataYaml.getAbsolutePath(),
                  "\n"));
            } catch (final Exception ex) {
               ANSITerminal.error("Could not refresh YAML configuration: " + ex.toString());
               ANSITerminal.warn(String.format("YAML refresh aborted, previously loaded stubs remain untouched"));
            }
         }

      } catch (final Exception ex) {
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }
}