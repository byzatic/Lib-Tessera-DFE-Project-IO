package io.github.byzatic.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable detached editor metadata for one service configuration parameter. */
public final class ServiceParameterMetadata {

    private final String id;
    private final String displayName;
    private final String description;
    private final ServiceParameterType type;
    private final String defaultValue;
    private final List<String> selectOptions;
    private final ServiceStorageRole storageRole;

    private ServiceParameterMetadata(Builder builder) {
        this.id = requireText(builder.id, "id");
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

    /** Returns a new builder for ServiceParameterMetadata. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the parameter identifier used in service options. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the parameter description. */
    public String getDescription() {
        return description;
    }

    /** Returns the parameter value type. */
    public ServiceParameterType getType() {
        return type;
    }

    /** Returns the default serialized value, or an empty string when no value is declared. */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** Returns immutable values available to a SELECT parameter. */
    public List<String> getSelectOptions() {
        return selectOptions;
    }

    /** Returns whether this parameter selects an input or output storage. */
    public ServiceStorageRole getStorageRole() {
        return storageRole;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceParameterMetadata)) {
            return false;
        }
        ServiceParameterMetadata that = (ServiceParameterMetadata) object;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && type == that.type
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(selectOptions, that.selectOptions)
                && storageRole == that.storageRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                displayName,
                description,
                type,
                defaultValue,
                selectOptions,
                storageRole
        );
    }

    @Override
    public String toString() {
        return "ServiceParameterMetadata{" +
                "id=" + id
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", type=" + type
                 + ", defaultValue=" + defaultValue
                 + ", selectOptions=" + selectOptions
                 + ", storageRole=" + storageRole +
                '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Fluent builder for immutable service parameter metadata. */
    public static final class Builder {

        private String id;
        private String displayName;
        private String description = "";
        private ServiceParameterType type = ServiceParameterType.STRING;
        private String defaultValue = "";
        private List<String> selectOptions = List.of();
        private ServiceStorageRole storageRole = ServiceStorageRole.NONE;

        private Builder() {
        }

        /** Sets the parameter identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the display name. */
        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        /** Sets the parameter description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the parameter value type. */
        public Builder type(ServiceParameterType value) {
            this.type = value;
            return this;
        }

        /** Sets the default serialized value. */
        public Builder defaultValue(String value) {
            this.defaultValue = value;
            return this;
        }

        /** Sets the immutable SELECT values. */
        public Builder selectOptions(List<String> value) {
            this.selectOptions = List.copyOf(Objects.requireNonNull(value, "selectOptions"));
            return this;
        }

        /** Sets the semantic storage role. */
        public Builder storageRole(ServiceStorageRole value) {
            this.storageRole = value;
            return this;
        }

        /** Builds and validates immutable service parameter metadata. */
        public ServiceParameterMetadata build() {
            return new ServiceParameterMetadata(this);
        }
    }
}
