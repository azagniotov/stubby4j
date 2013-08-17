package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.utils.FileUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class ExternalFilesScanner implements Runnable {

   private final StubbedDataManager stubbedDataManager;

   public ExternalFilesScanner(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
      ANSITerminal.status(String.format("External file scan enabled, watching external files referenced from %s", stubbedDataManager.getYamlAbsolutePath()));
   }

   @Override
   public void run() {

      try {
         final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(3000);

            boolean isContinue = true;
            String offendingFilename = "";
            for (Map.Entry<File, Long> entry : externalFiles.entrySet()) {
               final File file = entry.getKey();
               final long lastModified = entry.getValue();
               final long currentFileModified = file.lastModified();

               if (lastModified < currentFileModified) {
                  externalFiles.put(file, currentFileModified);
                  isContinue = false;
                  offendingFilename = file.getAbsolutePath();
                  break;
               }
            }

            if (isContinue) {
               continue;
            }

            ANSITerminal.info(String.format("\nExternal file scan detected change in %s\n", offendingFilename));

            try {
               final List<StubHttpLifecycle> stubHttpLifecycles =
                  new YamlParser().parse(stubbedDataManager.getYamlParentDirectory(), FileUtils.constructReader(stubbedDataManager.getDataYaml()));

               stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);
               ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of main YAML with external files from: %s%s on [" + new Date().toString() + "]",
                  "\n",
                  stubbedDataManager.getDataYaml(),
                  "\n"));
            } catch (final Exception ex) {
               ANSITerminal.error("Could not refresh YAML configuration: " + ex.toString());
               ANSITerminal.warn(String.format("YAML refresh aborted, previously loaded stubs remain untouched"));
            }
         }

      } catch (final Exception ex) {
         ex.printStackTrace();
         ANSITerminal.error("Could not perform live YAML scan: " + ex.toString());
      }
   }
}