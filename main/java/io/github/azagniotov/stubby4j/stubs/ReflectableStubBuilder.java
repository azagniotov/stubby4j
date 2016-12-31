package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;

import java.util.Optional;

public interface ReflectableStubBuilder<T extends ReflectableStub> {
    T build();

    <E> E get(final Class<E> clazzor, final ConfigurableYAMLProperty property, final E orElse);

    void stage(final Optional<ConfigurableYAMLProperty> fieldNameOptional, final Object fieldValue);
}
