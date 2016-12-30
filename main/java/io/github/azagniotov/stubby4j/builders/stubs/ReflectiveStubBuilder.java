package io.github.azagniotov.stubby4j.builders.stubs;


public interface ReflectiveStubBuilder<T> {
    T build() throws Exception;

    void stage(final String fieldName, final Object fieldValue);
}
