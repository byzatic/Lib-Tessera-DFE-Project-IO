package io.github.byzatic.lib.configio.service_spi;

import java.util.List;
import java.util.Objects;

/** Immutable editor-facing declaration of one service configuration parameter. */
public final class ServiceParameterDescriptor {

    private final String parameterId;
    private final String displayName;
    private final String description;
    private final ServiceParameterType type;
    private final String defaultValue;
    private final List<String> selectOptions;
    private final ServiceStorageRole storageRole;

    private ServiceParameterDescriptor(Builder builder) {
        this.parameterId = requireText(builder.parameterId, "parameterId");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.type = Objects.requireNonNull(builder.type, "type");
        this.defaultValue = Objects.requireNonNull(builder.defaultValue, "defaultValue");
        this.selectOptions = List.copyOf(
                Objects.requireNonNull(builder.selectOptions, "selectOptions")
        );
        this.storageRole = Objects.requireNonNull(builder.storageRole, "storageRole");
        if (selectOptions.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("selectOptions must contain non-blank values");
        }
        if (selectOptions.stream().distinct().count() != selectOptions.size()) {
            throw new IllegalArgumentException("selectOptions must not contain duplicates");
        }
        if (type != ServiceParameterType.SELECT && !selectOptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "selectOptions are only supported for SELECT parameters"
            );
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getParameterId() {
        return parameterId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ServiceParameterType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getSelectOptions() {
        return selectOptions;
    }

    public ServiceStorageRole getStorageRole() {
        return storageRole;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Fluent builder for immutable service parameter descriptors. */
    public static final class Builder {

        private String parameterId;
        private String displayName;
        private String description = "";
        private ServiceParameterType type = ServiceParameterType.STRING;
        private String defaultValue = "";
        private List<String> selectOptions = List.of();
        private ServiceStorageRole storageRole = ServiceStorageRole.NONE;

        private Builder() {
        }

        public Builder parameterId(String value) {
            this.parameterId = value;
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

        public Builder type(ServiceParameterType value) {
            this.type = value;
            return this;
        }

        public Builder defaultValue(String value) {
            this.defaultValue = value;
            return this;
        }

        public Builder selectOptions(List<String> value) {
            this.selectOptions = List.copyOf(Objects.requireNonNull(value, "selectOptions"));
            return this;
        }

        public Builder storageRole(ServiceStorageRole value) {
            this.storageRole = value;
            return this;
        }

        public ServiceParameterDescriptor build() {
            return new ServiceParameterDescriptor(this);
        }
    }
}
