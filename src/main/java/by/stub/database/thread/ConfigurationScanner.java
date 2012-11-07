package by.stub.database.thread;

import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 8:01 AM
 */
public final class ConfigurationScanner implements Runnable {

   private long lastModified;
   private final YamlParser yamlParser;
   private final DataStore dataStore;
   private static volatile boolean startFlag = true;

   public ConfigurationScanner(final YamlParser yamlParser, final DataStore dataStore) {
      this.yamlParser = yamlParser;
      this.dataStore = dataStore;
   }

   @Override
   public void run() {

      try {
         final String loadedConfigYamlPath = yamlParser.getLoadedConfigYamlPath();
         final File loadedConfig = new File(loadedConfigYamlPath);
         this.lastModified = loadedConfig.lastModified();

         while (startFlag) {

            Thread.sleep(3000);

            final long currentFileModified = loadedConfig.lastModified();
            if (this.lastModified >= currentFileModified) {
               continue;
            }

            this.lastModified = currentFileModified;
            final InputStream is = new FileInputStream(loadedConfigYamlPath);
            final Reader yamlReader = new InputStreamReader(is, StringUtils.utf8Charset());
            final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad(yamlReader);

            dataStore.resetStubHttpLifecycles(stubHttpLifecycles);
            ANSITerminal.ok(String.format("%sSuccessfully performed live reload of YAML configuration from: %s%s",
                  "\n",
                  loadedConfigYamlPath,
                  "\n"));
         }

      } catch (final Exception ex) {
         ANSITerminal.error("Could not reload YAML configuration: " + ex.toString());
      }
   }

   public void stopScanner(final boolean toStop) {
      synchronized (ConfigurationScanner.class) {
         startFlag = toStop;
      }
   }
}
