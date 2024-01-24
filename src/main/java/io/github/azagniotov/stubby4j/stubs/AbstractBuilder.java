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

package io.github.azagniotov.stubby4j.stubs;

import static io.github.azagniotov.generics.TypeSafeConverter.as;

import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractBuilder<T extends ReflectableStub> {

    protected final Map<ConfigurableYAMLProperty, Object> fieldNameAndValues;

    public AbstractBuilder() {
        this.fieldNameAndValues = new HashMap<>();
    }

    public <E> E getStaged(final Class<E> clazzor, final ConfigurableYAMLProperty property, E defaultValue) {
        return fieldNameAndValues.containsKey(property) ? as(clazzor, fieldNameAndValues.get(property)) : defaultValue;
    }

    public void stage(final ConfigurableYAMLProperty fieldName, final Optional<Object> fieldValueOptional) {
        fieldValueOptional.ifPresent(value -> fieldNameAndValues.put(fieldName, value));
    }

    public abstract String yamlFamilyName();

    public abstract T build();
}
