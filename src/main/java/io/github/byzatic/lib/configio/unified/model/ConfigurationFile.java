package io.github.byzatic.lib.configio.unified.model;

import java.util.Objects;

/** Immutable reference to a worker configuration file. */
public final class ConfigurationFile {

    private final String id;
    private final String description;

    private ConfigurationFile(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.description = Objects.requireNonNull(builder.description, "description");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Configuration file id must not be blank");
        }
    }

    /** Returns a new builder for ConfigurationFile. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the configuration file identifier. */
    public String getId() {
        return id;
    }

    /** Returns the configuration file description. */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ConfigurationFile)) {
            return false;
        }
        ConfigurationFile that = (ConfigurationFile) object;
        return Objects.equals(id, that.id)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }

    @Override
    public String toString() {
        return "ConfigurationFile{" +
                "id=" + id
                 + ", description=" + description +
                '}';
    }

    /** Fluent builder for immutable ConfigurationFile values. */
    public static final class Builder {

        private String id;
        private String description;

        private Builder() {
        }

        /** Sets the configuration file identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the configuration file description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Builds and validates an immutable ConfigurationFile. */
        public ConfigurationFile build() {
            return new ConfigurationFile(this);
        }
    }
}

