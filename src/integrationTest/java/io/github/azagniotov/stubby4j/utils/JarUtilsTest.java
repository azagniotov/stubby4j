package io.github.azagniotov.stubby4j.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class JarUtilsTest {

    @Test
    public void shouldReadManifestImplementationVersion() throws Exception {
        final String result = JarUtils.readManifestImplementationVersion();

        assertThat(result).isNotEqualTo("x.x.xx");
    }

    @Test
    public void shouldReadManifestBuiltDate() throws Exception {
        final String result = JarUtils.readManifestBuiltDate();

        assertThat(result).contains("20");
    }

}