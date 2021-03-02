package io.github.azagniotov.stubby4j.filesystem;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

@GeneratedCodeCoverageExclusion
public final class MainIncludedYamlScanner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainIncludedYamlScanner.class);

    private final long sleepTime;
    private final StubRepository stubRepository;

    public MainIncludedYamlScanner(final StubRepository stubRepository, final long sleepTime) {
        this.sleepTime = sleepTime;
        this.stubRepository = stubRepository;
    }

    @Override
    public void run() {

        final YamlParser yamlParser = new YamlParser();

        try {
            final File mainDataYaml = stubRepository.getYamlConfig();
            final String mainYamlParentDirectory = mainDataYaml.getParent();
            try (final InputStream mainConfigInputStream = FileUtils.constructInputStream(mainDataYaml)) {
                final Object rawYamlConfig = yamlParser.loadRawYamlConfig(mainConfigInputStream);

                // This means that our main YAML config does not include other files, i.e.:
                //
                // includes:
                //  - service-1-stubs.yaml
                //  - service-2-stubs.yaml
                //  - service-3-stubs.yaml
                //
                if (!yamlParser.isMainYamlHasIncludes(rawYamlConfig)) {
                    return;
                }

                ANSITerminal.status(String.format("Main YAML with included YAMLs scan enabled, watching included YAMLs referenced from %s", stubRepository.getYamlConfigCanonicalPath()));
                LOGGER.debug("Main YAML with included YAMLs scan enabled, watching included YAMLs referenced from {}.",
                        stubRepository.getYamlConfigCanonicalPath());

                final List<File> yamlIncludes = yamlParser.getYamlIncludes(mainYamlParentDirectory, rawYamlConfig);
                final Map<File, Long> yamlIncludeFiles = new HashMap<>();
                for (final File include : yamlIncludes) {
                    yamlIncludeFiles.put(include, include.lastModified());
                }

                while (!Thread.currentThread().isInterrupted()) {

                    Thread.sleep(sleepTime);

                    boolean isContinue = true;
                    String offendingFilename = "";
                    for (Map.Entry<File, Long> entry : yamlIncludeFiles.entrySet()) {
                        final File file = entry.getKey();
                        final long lastModified = entry.getValue();
                        final long currentFileModified = file.lastModified();

                        if (lastModified < currentFileModified) {
                            yamlIncludeFiles.put(file, currentFileModified);
                            isContinue = false;
                            offendingFilename = file.getAbsolutePath();
                            break;
                        }
                    }

                    if (isContinue) {
                        continue;
                    }

                    ANSITerminal.info(String.format("%sMain YAML included YAMLs scan detected change in %s%s", BR, offendingFilename, BR));
                    LOGGER.info("Main YAML included YAMLs scan detected change in {}.", offendingFilename);

                    try {
                        stubRepository.refreshStubsFromYamlConfig(yamlParser);

                        ANSITerminal.ok(String.format("%sSuccessfully performed live refresh of main YAML with included YAMLs from: %s on [" + DateTimeUtils.systemDefault() + "]%s",
                                BR, stubRepository.getYamlConfig(), BR));
                        LOGGER.info("Successfully performed live refresh of main YAML with included YAMLs from: {}.",
                                stubRepository.getYamlConfig());
                    } catch (final Exception ex) {
                        ANSITerminal.error("Could not refresh YAML configuration, previously loaded stubs remain untouched." + ex.toString());
                        LOGGER.error("Could not refresh YAML configuration, previously loaded stubs remain untouched.", ex);
                    }
                }

            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Could not perform live main YAML scan with included YAMLs.", ex);
        }
    }
}