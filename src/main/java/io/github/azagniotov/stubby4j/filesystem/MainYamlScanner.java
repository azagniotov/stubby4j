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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GeneratedCodeClassCoverageExclusion
public final class MainYamlScanner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainYamlScanner.class);

    private final long sleepTime;
    private final StubRepository stubRepository;

    public MainYamlScanner(final StubRepository stubRepository, final long sleepTime) {
        this.sleepTime = sleepTime;
        this.stubRepository = stubRepository;
        ANSITerminal.status(
                String.format("Main YAML scan enabled, watching %s", stubRepository.getYamlConfigCanonicalPath()));
        LOGGER.debug("Main YAML scan enabled, watching {}.", stubRepository.getYamlConfigCanonicalPath());
    }

    @Override
    public void run() {

        try {
            final File dataYaml = stubRepository.getYamlConfig();
            long mainYamlLastModified = dataYaml.lastModified();

            while (!Thread.currentThread().isInterrupted()) {

                Thread.sleep(sleepTime);

                final long currentFileModified = dataYaml.lastModified();
                if (mainYamlLastModified >= currentFileModified) {
                    continue;
                }

                ANSITerminal.info(String.format(
                        "%sMain YAML scan detected change in %s%s",
                        BR, stubRepository.getYamlConfigCanonicalPath(), BR));
                LOGGER.info("Main YAML scan detected change in  {}.", stubRepository.getYamlConfigCanonicalPath());

                try {
                    mainYamlLastModified = currentFileModified;
                    stubRepository.refreshStubsFromYamlConfig(new YamlParser());

                    ANSITerminal.ok(String.format(
                            "%sSuccessfully performed live refresh of main YAML file from: %s on ["
                                    + DateTimeUtils.systemDefault() + "]%s",
                            BR,
                            dataYaml.getAbsolutePath(),
                            BR));
                    LOGGER.info(
                            "Successfully performed live refresh of main YAML from: {}.", dataYaml.getAbsolutePath());
                } catch (final Exception ex) {
                    ANSITerminal.error("Could not refresh main YAML configuration, in-memory stubs remain untouched."
                            + ex.toString());
                    LOGGER.error("Could not refresh main YAML configuration, in-memory stubs remain untouched.", ex);
                }
            }

        } catch (final Exception ex) {
            LOGGER.error("Could not perform live YAML scan.", ex);
        }
    }
}
