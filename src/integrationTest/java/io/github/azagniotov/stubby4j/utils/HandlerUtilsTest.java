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

package io.github.azagniotov.stubby4j.utils;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
