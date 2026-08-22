package io.github.byzatic.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable global configuration of a Tessera project. */
public final class ProjectConfiguration {

    private final List<StorageDefinition> storages;
    private final List<ServiceDefinition> services;

    private ProjectConfiguration(Builder builder) {
        this.storages = List.copyOf(Objects.requireNonNull(builder.storages, "storages"));
        this.services = List.copyOf(Objects.requireNonNull(builder.services, "services"));
        if (storages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Storages must not contain null");
        }
        if (services.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Services must not contain null");
        }
    }

    /** Returns a new builder for ProjectConfiguration. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the immutable global storage declarations. */
    public List<StorageDefinition> getStorages() {
        return storages;
    }

    /** Returns the immutable service declarations. */
    public List<ServiceDefinition> getServices() {
        return services;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProjectConfiguration)) {
            return false;
        }
        ProjectConfiguration that = (ProjectConfiguration) object;
        return Objects.equals(storages, that.storages)
                && Objects.equals(services, that.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storages, services);
    }

    @Override
    public String toString() {
        return "ProjectConfiguration{" +
                "storages=" + storages
                 + ", services=" + services +
                '}';
    }

    /** Fluent builder for immutable ProjectConfiguration values. */
    public static final class Builder {

        private List<StorageDefinition> storages = List.of();
        private List<ServiceDefinition> services = List.of();

        private Builder() {
        }

        /** Sets the immutable global storage declarations. */
        public Builder storages(List<StorageDefinition> value) {
            this.storages = List.copyOf(Objects.requireNonNull(value, "storages"));
            return this;
        }

        /** Sets the immutable service declarations. */
        public Builder services(List<ServiceDefinition> value) {
            this.services = List.copyOf(Objects.requireNonNull(value, "services"));
            return this;
        }

        /** Builds and validates an immutable ProjectConfiguration. */
        public ProjectConfiguration build() {
            return new ProjectConfiguration(this);
        }
    }
}

