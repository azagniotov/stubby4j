package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.exception.Stubby4JException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 10:26 AM
 */

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
        final String templateContent = HandlerUtils.populateHtmlTemplate("test-template", "alex");
        assertThat("<html><head></head><body>alex</body></html>").isEqualTo(templateContent);
    }

    @Test
    public void shouldNotPopulateNonExistentHtmlTemplate() throws Exception {

        expectedException.expect(Stubby4JException.class);

        HandlerUtils.populateHtmlTemplate("non-existent-template", "alex");
    }
}
