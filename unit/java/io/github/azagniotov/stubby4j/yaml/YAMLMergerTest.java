package io.github.azagniotov.stubby4j.yaml;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.file.Files.exists;

/**
 * @author Abhijit Sarkar
 */
public class YAMLMergerTest {
    @Test
    public void testMerge() throws IOException, URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());

        // IntelliJ and Gradle have different output directories
        Path resources = root.resolveSibling("resources");
        if (!exists(resources)) {
            resources = root.getParent().resolveSibling("resources");
        }
        assertThat(exists(resources)).isTrue();

        File merged = YAMLMerger.merge(resources.toFile());
        assertThat(merged).isNotNull();
        assertThat(merged.exists() && merged.isFile() && merged.canRead()).isTrue();

        for (String str : Arrays.asList("method: PUT", "status: 200")) {
            long count = Files.lines(merged.toPath())
                    .filter(line -> line.trim().equals(str))
                    .count();
            assertThat(count).isEqualTo(3L);
        }
    }
}
