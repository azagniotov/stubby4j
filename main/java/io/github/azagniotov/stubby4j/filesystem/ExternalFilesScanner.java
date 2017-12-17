package io.github.azagniotov.stubby4j.filesystem;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public final class ExternalFilesScanner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExternalFilesScanner.class);

    private final long sleepTime;
    private final StubRepository stubRepository;

    public ExternalFilesScanner(final StubRepository stubRepository, final long sleepTime) {
        this.sleepTime = sleepTime;
        this.stubRepository = stubRepository;
        logger.debug("External file scan enabled, watching external files referenced from {}.",
                stubRepository.getYAMLConfigCanonicalPath());
    }

    @Override
    public void run() {

        try {
            final Map<File, Long> externalFiles = stubRepository.getExternalFiles();

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

                logger.info("External file scan detected change in {}.", offendingFilename);

                try {
                    stubRepository.refreshStubsFromYAMLConfig(new YAMLParser());
                    logger.info("Successfully performed live refresh of main YAML with external files from: {}.",
                            stubRepository.getYAMLConfig());
                } catch (final Exception ex) {
                    logger.error("Could not refresh YAML configuration.", ex);
                    logger.warn("YAML refresh aborted, previously loaded stubs remain untouched.");
                }
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
            logger.error("Could not perform live YAML scan.", ex);
        }
    }
}