package io.github.azagniotov.stubby4j.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import static io.github.azagniotov.stubby4j.utils.FileUtils.quietIO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;

/**
 * @author Abhijit Sarkar
 */
public final class YAMLMerger {
    private static final Logger LOGGER = LoggerFactory.getLogger(YAMLMerger.class);
    private static final PathMatcher YAML_MATCHER = FileSystems.getDefault()
            .getPathMatcher("glob:**.{yaml,yml}");

    private YAMLMerger() {
    }

    public static File merge(File config) throws IOException {
        Path configDir = config.toPath();
        LOGGER.info("Config dir: {}.", configDir.toAbsolutePath());

        if (!exists(configDir) || !isDirectory(configDir)) {
            throw new IllegalArgumentException(
                    String.format("%s doesn't exist or is not a directory.", configDir.toAbsolutePath())
            );
        }

        List<Path> files = findAllYaml(configDir);
        Path merged = Files.createTempFile(configDir, null, ".yaml");

        files.forEach(f -> quietIO(() -> merge(f, merged)));

        LOGGER.info("Done merging. Merged file: {}.", merged.toAbsolutePath());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> quietIO(() -> {
            delete(merged);
            return null;
        })));

        return merged.toFile();
    }

    private static Path merge(Path from, Path to) throws IOException {
        LOGGER.info("Merging: {}.", from.toAbsolutePath());

        try (
                BufferedWriter writer = newBufferedWriter(to, WRITE, APPEND, CREATE);
        ) {
            writer.write(String.format("# Begin %s%n%n", from.getFileName()));
            Files.lines(from, UTF_8)
                    .forEach(str -> quietIO(() -> {
                        writer.write(String.format("%s%n", str));
                        return null;
                    }));
            writer.write(String.format("%n%n# End %s%n%n", from.getFileName()));
        }
        return to;
    }

    private static List<Path> findAllYaml(Path configDir) throws IOException {
        return Files.find(
                configDir,
                Integer.MAX_VALUE,
                (path, attr) -> attr.isRegularFile() && YAML_MATCHER.matches(path)
        )
                .collect(toList());
    }
}
