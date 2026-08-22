package io.github.byzatic.tessera.lib.configio.unified.spi.service;

import java.util.List;
import java.util.Objects;

/** Immutable editor-facing declaration of one service. */
public final class ServiceEditorDescriptor {

    private final String serviceId;
    private final String displayName;
    private final String description;
    private final List<ServiceParameterDescriptor> parameters;

    private ServiceEditorDescriptor(Builder builder) {
        this.serviceId = requireText(builder.serviceId, "serviceId");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.parameters = List.copyOf(Objects.requireNonNull(builder.parameters, "parameters"));
        if (parameters.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("parameters must not contain null");
        }
        if (parameters.stream()
                .map(ServiceParameterDescriptor::getParameterId)
                .distinct()
                .count() != parameters.size()) {
            throw new IllegalArgumentException("parameterId must be unique within a service");
        }
    }

    /** Returns a new builder for a service editor descriptor. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the stable service identifier. */
    public String getServiceId() {
        return serviceId;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the service description. */
    public String getDescription() {
        return description;
    }

    /** Returns immutable service parameter declarations. */
    public List<ServiceParameterDescriptor> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceEditorDescriptor)) {
            return false;
        }
        ServiceEditorDescriptor that = (ServiceEditorDescriptor) object;
        return Objects.equals(serviceId, that.serviceId)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, displayName, description, parameters);
    }

    @Override
    public String toString() {
        return "ServiceEditorDescriptor{" +
                "serviceId=" + serviceId
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", parameters=" + parameters +
                '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Fluent builder for immutable service editor descriptors. */
    public static final class Builder {

        private String serviceId;
        private String displayName;
        private String description = "";
        private List<ServiceParameterDescriptor> parameters = List.of();

        private Builder() {
        }

        public Builder serviceId(String value) {
            this.serviceId = value;
            return this;
        }

        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder parameters(List<ServiceParameterDescriptor> values) {
            this.parameters = List.copyOf(Objects.requireNonNull(values, "parameters"));
            return this;
        }

        public ServiceEditorDescriptor build() {
            return new ServiceEditorDescriptor(this);
        }
    }
}
