package io.github.azagniotov.stubby4j.utils.helpers;

public final class AnotherType {
    private final Long value;

    public AnotherType(final Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnotherType anotherType = (AnotherType) o;

        return getValue().equals(anotherType.getValue());

    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }
}
