package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class HandlerUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldGetHtmlResourceByName() throws Exception {
        final String templateContent = HandlerUtils.getHtmlResourceByName("test-template");
        assertThat("<html><head></head><body>%s</body></html>").isEqualTo(templateContent);
    }

    @Test
    public void shouldPopulateHtmlTemplate() throws Exception {
        final String templateContent = HandlerUtils.populateHtmlTemplate("test-template", "cheburashka");
        assertThat("<html><head></head><body>cheburashka</body></html>").isEqualTo(templateContent);
    }

    @Test
    public void shouldNotPopulateNonExistentHtmlTemplate() throws Exception {

        expectedException.expect(IOException.class);

        HandlerUtils.populateHtmlTemplate("non-existent-template", "cheburashka");
    }
}
