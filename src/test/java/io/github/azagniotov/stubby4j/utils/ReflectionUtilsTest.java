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

import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ReflectionUtilsTest {

    private StubRequest.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubRequest.Builder();
    }

    @Test
    public void shouldGetObjectPropertiesAndValues() throws Exception {
        final StubRequest stubRequest = builder.withMethod(HttpMethods.POST).build();
        final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);

        assertThat("[POST]").isEqualTo(properties.get("method"));
        assertThat(properties.get("url")).isNull();
        assertThat(properties.get("post")).isNull();
        assertThat(properties.get("headers")).isNull();
    }

    @Test
    public void shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws Exception {
        final StubRequest stubRequest = builder.build();
        assertThat(stubRequest.getUrl()).isNull();

        final Map<String, Object> values = new HashMap<>();
        values.put("url", "google.com");
        ReflectionUtils.injectObjectFields(stubRequest, values);

        assertThat(stubRequest.getUrl()).isEqualTo("google.com");
    }

    @Test
    public void shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws Exception {
        final StubRequest stubRequest = builder.build();
        assertThat(stubRequest.getUrl()).isNull();

        final Map<String, Object> values = new HashMap<>();
        values.put("nonExistentProperty", "google.com");
        ReflectionUtils.injectObjectFields(stubRequest, values);

        assertThat(stubRequest.getUrl()).isNull();
    }

    @Test
    public void shouldReturnNullWhenClassHasNoDeclaredMethods() throws Exception {
        final Object result = ReflectionUtils.getPropertyValue(new MethodLessInterface() {}, "somePropertyName");

        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnPropertyValueWhenClassHasDeclaredMethods() throws Exception {
        final String expectedMethodValue = "cheburashka";
        final Object result = ReflectionUtils.getPropertyValue(
                new MethodFulInterface() {
                    @Override
                    public String getName() {
                        return expectedMethodValue;
                    }
                },
                "name");

        assertThat(result).isEqualTo(expectedMethodValue);
    }

    private interface MethodLessInterface {}

    private interface MethodFulInterface {
        String getName();
    }
}
