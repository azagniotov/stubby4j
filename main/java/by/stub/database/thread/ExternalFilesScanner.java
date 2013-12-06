package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.yaml.YamlParser;

import java.io.File;
import java.util.Date;
import java.util.Map;

import static by.stub.utils.FileUtils.BR;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class ExternalFilesScanner implements Runnable {

   private final long sleepTime;
   private final StubbedDataManager stubbedDataManager;

   public ExternalFilesScanner(final StubbedDataManager stubbedDataManager, final long sleepTime) {
      this.sleepTime = sleepTime;
      this.stubbedDataManager = stubbedDataManager;
      ANSITerminal.status(String.format("External file scan enabled, watching external files referenced from %s", stubbedDataManager.getYamlCanonicalPath()));
   }

   @Override
   public void run() {

      try {
         final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

         while (!Thread.currentThread().isInterrupted()) {

            Thread.sleep(sleepTime);

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

            ANSITerminal.info(String.format("%sExternal file scan detected change in %s%s", BR, offendingFilename, BR));

            try {
               stubbedDataManager.refreshStubbedData(new YamlParser());
               ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of main YAML with external files from: %s on [" + new Date().toString().trim() + "]%s",
                  BR,
                  stubbedDataManager.getDataYaml(),
                  BR));
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