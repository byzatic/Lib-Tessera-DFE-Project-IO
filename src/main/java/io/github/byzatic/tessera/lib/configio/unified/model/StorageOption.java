package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.Objects;

/** One immutable key-value option of a storage declaration. */
public final class StorageOption {

    private final String key;
    private final String value;

    private StorageOption(Builder builder) {
        this.key = Objects.requireNonNull(builder.key, "key");
        this.value = Objects.requireNonNull(builder.value, "value");

    }

    /** Returns a new builder for StorageOption. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the option key. */
    public String getKey() {
        return key;
    }

    /** Returns the option value. */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof StorageOption)) {
            return false;
        }
        StorageOption that = (StorageOption) object;
        return Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "StorageOption{" +
                "key=" + key
                 + ", value=" + value +
                '}';
    }

    /** Fluent builder for immutable StorageOption values. */
    public static final class Builder {

        private String key;
        private String value;

        private Builder() {
        }

        /** Sets the option key. */
        public Builder key(String value) {
            this.key = value;
            return this;
        }

        /** Sets the option value. */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /** Builds and validates an immutable StorageOption. */
        public StorageOption build() {
            return new StorageOption(this);
        }
    }
}

