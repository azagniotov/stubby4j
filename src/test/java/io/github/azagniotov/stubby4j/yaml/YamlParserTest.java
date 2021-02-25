package io.github.azagniotov.stubby4j.yaml;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class YamlParserTest {

    @Test
    public void isMainYamlHasIncludesFoundAsTrue() {
        final YamlParser yamlParser = new YamlParser();

        final Map<String, List<String>> loadedYamlConfig = new HashMap<>();
        loadedYamlConfig.put("includes", new ArrayList<>());

        assertThat(yamlParser.isMainYamlHasIncludes(loadedYamlConfig)).isTrue();
    }

    @Test
    public void isMainYamlHasIncludesFoundAsFalse() {
        final YamlParser yamlParser = new YamlParser();

        final Map<String, List<String>> loadedYamlConfig = new HashMap<>();
        loadedYamlConfig.put("unexpectedKey", new ArrayList<>());

        assertThat(yamlParser.isMainYamlHasIncludes(loadedYamlConfig)).isFalse();
    }

    @Test
    public void isMainYamlHasIncludesFoundAsFalseWhenNotMap() {
        final YamlParser yamlParser = new YamlParser();

        assertThat(yamlParser.isMainYamlHasIncludes(new ArrayList<>())).isFalse();
    }
}