package io.github.byzatic.lib.configio.service_spi;

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
        long distinctParameterIds = parameters.stream()
                .map(ServiceParameterDescriptor::getParameterId)
                .distinct()
                .count();
        if (distinctParameterIds != parameters.size()) {
            throw new IllegalArgumentException("parameterId must be unique within a service");
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<ServiceParameterDescriptor> getParameters() {
        return parameters;
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

        public Builder parameters(List<ServiceParameterDescriptor> value) {
            this.parameters = List.copyOf(Objects.requireNonNull(value, "parameters"));
            return this;
        }

        public ServiceEditorDescriptor build() {
            return new ServiceEditorDescriptor(this);
        }
    }
}
