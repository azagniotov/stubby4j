package io.github.azagniotov.stubby4j.filesystem;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class MainYamlScanner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MainYamlScanner.class);

    private final long sleepTime;
    private final StubRepository stubRepository;

    public MainYamlScanner(final StubRepository stubRepository, final long sleepTime) {
        this.sleepTime = sleepTime;
        this.stubRepository = stubRepository;
        logger.debug("Main YAML scan enabled, watching {}.", stubRepository.getYAMLConfigCanonicalPath());
    }

    @Override
    public void run() {

        try {
            final File dataYaml = stubRepository.getYAMLConfig();
            long mainYamlLastModified = dataYaml.lastModified();

            while (!Thread.currentThread().isInterrupted()) {

                Thread.sleep(sleepTime);

                final long currentFileModified = dataYaml.lastModified();
                if (mainYamlLastModified >= currentFileModified) {
                    continue;
                }

                logger.info("Main YAML scan detected change in  {}.", stubRepository.getYAMLConfigCanonicalPath());

                try {
                    mainYamlLastModified = currentFileModified;
                    stubRepository.refreshStubsFromYAMLConfig(new YAMLParser());
                    logger.info("Successfully performed live refresh of main YAML from: {}.",
                            dataYaml.getAbsolutePath());
                } catch (final Exception ex) {
                    logger.error("Could not refresh YAML file.", ex);
                    logger.warn("YAML refresh aborted, in-memory  stubs remain untouched.");
                }
            }

        } catch (final Exception ex) {
            logger.error("Could not perform live YAML scan.", ex);
        }
    }
}