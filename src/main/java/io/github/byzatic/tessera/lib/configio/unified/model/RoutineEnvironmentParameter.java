package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.Objects;

/** Immutable detached metadata for one routine environment parameter. */
public final class RoutineEnvironmentParameter {
    private final String key;
    private final String displayName;
    private final String description;
    private final String defaultValue;
    private final boolean required;

    private RoutineEnvironmentParameter(Builder builder) {
        key = requireText(builder.key, "key");
        displayName = requireText(builder.displayName, "displayName");
        description = Objects.requireNonNull(builder.description, "description");
        defaultValue = Objects.requireNonNull(builder.defaultValue, "defaultValue");
        required = builder.required;
    }

    public static Builder newBuilder() { return new Builder(); }
    /** Returns the environment variable name. */
    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getDefaultValue() { return defaultValue; }
    public boolean isRequired() { return required; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof RoutineEnvironmentParameter)) return false;
        RoutineEnvironmentParameter that = (RoutineEnvironmentParameter) object;
        return required == that.required && Objects.equals(key, that.key)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() { return Objects.hash(key, displayName, description, defaultValue, required); }

    @Override
    public String toString() {
        return "RoutineEnvironmentParameter{" + "key=" + key + ", displayName=" + displayName
                + ", description=" + description + ", defaultValue=" + defaultValue
                + ", required=" + required + '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }

    /** Fluent builder for immutable routine environment metadata. */
    public static final class Builder {
        private String key;
        private String displayName;
        private String description = "";
        private String defaultValue = "";
        private boolean required;
        private Builder() { }
        public Builder key(String value) { key = value; return this; }
        public Builder displayName(String value) { displayName = value; return this; }
        public Builder description(String value) { description = value; return this; }
        public Builder defaultValue(String value) { defaultValue = value; return this; }
        public Builder required(boolean value) { required = value; return this; }
        public RoutineEnvironmentParameter build() { return new RoutineEnvironmentParameter(this); }
    }
}
