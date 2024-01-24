/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.filesystem;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import java.io.File;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GeneratedCodeClassCoverageExclusion
public final class ExternalFilesScanner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalFilesScanner.class);

    private final long sleepTime;
    private final StubRepository stubRepository;

    public ExternalFilesScanner(final StubRepository stubRepository, final long sleepTime) {
        this.sleepTime = sleepTime;
        this.stubRepository = stubRepository;
        ANSITerminal.status(String.format(
                "External file scan enabled, watching external files referenced from %s",
                stubRepository.getYamlConfigCanonicalPath()));
        LOGGER.debug(
                "External file scan enabled, watching external files referenced from {}.",
                stubRepository.getYamlConfigCanonicalPath());
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

                ANSITerminal.info(
                        String.format("%sExternal file scan detected change in %s%s", BR, offendingFilename, BR));
                LOGGER.info("External file scan detected change in {}.", offendingFilename);

                try {
                    stubRepository.refreshStubsFromYamlConfig(new YamlParser());

                    ANSITerminal.ok(String.format(
                            "%sSuccessfully performed live refresh of main YAML with external files from: %s on ["
                                    + DateTimeUtils.systemDefault() + "]%s",
                            BR,
                            stubRepository.getYamlConfig(),
                            BR));
                    LOGGER.info(
                            "Successfully performed live refresh of main YAML with external files from: {}.",
                            stubRepository.getYamlConfig());
                } catch (final Exception ex) {
                    ANSITerminal.error("Could not refresh YAML configuration, previously loaded stubs remain untouched."
                            + ex.toString());
                    LOGGER.error("Could not refresh YAML configuration, previously loaded stubs remain untouched.", ex);
                }
            }

        } catch (final Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Could not perform live YAML scan.", ex);
        }
    }
}
