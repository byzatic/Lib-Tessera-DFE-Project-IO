package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable worker declaration inside a pipeline stage. */
public final class Worker {

    private final String name;
    private final String description;
    private final List<ConfigurationFile> configurationFiles;

    private Worker(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.configurationFiles = List.copyOf(Objects.requireNonNull(builder.configurationFiles, "configurationFiles"));
        if (name.isBlank()) {
            throw new IllegalArgumentException("Worker name must not be blank");
        }
        if (configurationFiles.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Configuration files must not contain null");
        }
    }

    /** Returns a new builder for Worker. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the worker name. */
    public String getName() {
        return name;
    }

    /** Returns the worker description. */
    public String getDescription() {
        return description;
    }

    /** Returns the immutable configuration file references. */
    public List<ConfigurationFile> getConfigurationFiles() {
        return configurationFiles;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Worker)) {
            return false;
        }
        Worker that = (Worker) object;
        return Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(configurationFiles, that.configurationFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, configurationFiles);
    }

    @Override
    public String toString() {
        return "Worker{" +
                "name=" + name
                 + ", description=" + description
                 + ", configurationFiles=" + configurationFiles +
                '}';
    }

    /** Fluent builder for immutable Worker values. */
    public static final class Builder {

        private String name;
        private String description;
        private List<ConfigurationFile> configurationFiles = List.of();

        private Builder() {
        }

        /** Sets the worker name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the worker description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the immutable configuration file references. */
        public Builder configurationFiles(List<ConfigurationFile> value) {
            this.configurationFiles = List.copyOf(Objects.requireNonNull(value, "configurationFiles"));
            return this;
        }

        /** Builds and validates an immutable Worker. */
        public Worker build() {
            return new Worker(this);
        }
    }
}

