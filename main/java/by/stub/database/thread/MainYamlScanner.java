package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.utils.FileUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class MainYamlScanner implements Runnable {

   private final StubbedDataManager stubbedDataManager;

   public MainYamlScanner(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
      ANSITerminal.status(String.format("Main YAML scan enabled, watching %s", stubbedDataManager.getYamlAbsolutePath()));
   }

   @Override
   public void run() {

      try {
         final File dataYaml = stubbedDataManager.getDataYaml();
         long mainYamlLastModified = dataYaml.lastModified();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(3000);

            final long currentFileModified = dataYaml.lastModified();
            if (mainYamlLastModified >= currentFileModified) {
               continue;
            }

            ANSITerminal.info(String.format("\nMain YAML scan detected change in %s\n", stubbedDataManager.getYamlAbsolutePath()));

            try {
               mainYamlLastModified = currentFileModified;
               final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(dataYaml.getParent(), FileUtils.constructReader(dataYaml));

               stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);
               ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of main YAML file from: %s%s on [" + new Date().toString() + "]",
                  "\n",
                  dataYaml.getAbsolutePath(),
                  "\n"));
            } catch (final Exception ex) {
               ANSITerminal.error("Could not refresh YAML file: " + ex.toString());
               ANSITerminal.warn(String.format("YAML refresh aborted, in-memory stubs remain untouched"));
            }
         }

      } catch (final Exception ex) {
         ex.printStackTrace();
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }
}