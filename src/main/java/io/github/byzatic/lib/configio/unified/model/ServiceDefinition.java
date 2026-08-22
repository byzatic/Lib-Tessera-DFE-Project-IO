package io.github.byzatic.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable project service declaration. */
public final class ServiceDefinition {

    private final String id;
    private final String description;
    private final List<ServiceOption> options;

    private ServiceDefinition(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.options = List.copyOf(Objects.requireNonNull(builder.options, "options"));
        if (id.isBlank()) {
            throw new IllegalArgumentException("Service id must not be blank");
        }
        if (options.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Service options must not contain null");
        }
    }

    /** Returns a new builder for ServiceDefinition. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the service identifier. */
    public String getId() {
        return id;
    }

    /** Returns the service description. */
    public String getDescription() {
        return description;
    }

    /** Returns the immutable service options. */
    public List<ServiceOption> getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceDefinition)) {
            return false;
        }
        ServiceDefinition that = (ServiceDefinition) object;
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
        return "ServiceDefinition{" +
                "id=" + id
                 + ", description=" + description
                 + ", options=" + options +
                '}';
    }

    /** Fluent builder for immutable ServiceDefinition values. */
    public static final class Builder {

        private String id;
        private String description;
        private List<ServiceOption> options = List.of();

        private Builder() {
        }

        /** Sets the service identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the service description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the immutable service options. */
        public Builder options(List<ServiceOption> value) {
            this.options = List.copyOf(Objects.requireNonNull(value, "options"));
            return this;
        }

        /** Builds and validates an immutable ServiceDefinition. */
        public ServiceDefinition build() {
            return new ServiceDefinition(this);
        }
    }
}

