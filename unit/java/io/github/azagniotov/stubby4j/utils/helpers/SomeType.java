package io.github.azagniotov.stubby4j.utils.helpers;

public final class SomeType {
    private final String value;

    public SomeType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SomeType someType = (SomeType) o;

        return getValue().equals(someType.getValue());

    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }
}
