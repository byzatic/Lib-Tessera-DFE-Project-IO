package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable project or node storage declaration. */
public final class StorageDefinition {

    private final String id;
    private final String description;
    private final List<StorageOption> options;

    private StorageDefinition(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.options = List.copyOf(Objects.requireNonNull(builder.options, "options"));
        if (id.isBlank()) {
            throw new IllegalArgumentException("Storage id must not be blank");
        }
        if (options.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Storage options must not contain null");
        }
    }

    /** Returns a new builder for StorageDefinition. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the storage identifier. */
    public String getId() {
        return id;
    }

    /** Returns the storage description. */
    public String getDescription() {
        return description;
    }

    /** Returns the immutable storage options. */
    public List<StorageOption> getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof StorageDefinition)) {
            return false;
        }
        StorageDefinition that = (StorageDefinition) object;
        return Objects.equals(id, that.id)
                && Objects.equals(description, that.description)
                && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, options);
    }

    @Override
    public String toString() {
        return "StorageDefinition{" +
                "id=" + id
                 + ", description=" + description
                 + ", options=" + options +
                '}';
    }

    /** Fluent builder for immutable StorageDefinition values. */
    public static final class Builder {

        private String id;
        private String description;
        private List<StorageOption> options = List.of();

        private Builder() {
        }

        /** Sets the storage identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the storage description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the immutable storage options. */
        public Builder options(List<StorageOption> value) {
            this.options = List.copyOf(Objects.requireNonNull(value, "options"));
            return this;
        }

        /** Builds and validates an immutable StorageDefinition. */
        public StorageDefinition build() {
            return new StorageDefinition(this);
        }
    }
}

