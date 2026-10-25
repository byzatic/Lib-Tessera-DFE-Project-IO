package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.Objects;
import java.util.List;

/** Immutable detached metadata for one routine configuration-file parameter. */
public final class RoutineConfigurationFileParameter {
    private final String key;
    private final String displayName;
    private final String description;
    private final String suggestedFileName;
    private final List<String> allowedExtensions;
    private final List<ConfigurationFileScope> allowedScopes;
    private final ConfigurationFileScope defaultScope;
    private final boolean required;

    private RoutineConfigurationFileParameter(Builder builder) {
        key = requireText(builder.key, "key");
        displayName = requireText(builder.displayName, "displayName");
        description = Objects.requireNonNull(builder.description, "description");
        suggestedFileName = requireText(builder.suggestedFileName, "suggestedFileName");
        validateFileName(suggestedFileName);
        allowedExtensions = copyDistinctTextValues(
                builder.allowedExtensions,
                "allowedExtensions"
        );
        allowedScopes = List.copyOf(
                Objects.requireNonNull(builder.allowedScopes, "allowedScopes")
        );
        if (allowedScopes.isEmpty()) {
            throw new IllegalArgumentException("allowedScopes must not be empty");
        }
        if (allowedScopes.stream().distinct().count() != allowedScopes.size()) {
            throw new IllegalArgumentException("allowedScopes must not contain duplicates");
        }
        defaultScope = Objects.requireNonNull(builder.defaultScope, "defaultScope");
        if (!allowedScopes.contains(defaultScope)) {
            throw new IllegalArgumentException("defaultScope must be present in allowedScopes");
        }
        required = builder.required;
    }

    public static Builder newBuilder() { return new Builder(); }
    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getSuggestedFileName() { return suggestedFileName; }
    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public List<ConfigurationFileScope> getAllowedScopes() { return allowedScopes; }
    public ConfigurationFileScope getDefaultScope() { return defaultScope; }
    public boolean isRequired() { return required; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof RoutineConfigurationFileParameter)) return false;
        RoutineConfigurationFileParameter that = (RoutineConfigurationFileParameter) object;
        return required == that.required && Objects.equals(key, that.key)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(suggestedFileName, that.suggestedFileName)
                && Objects.equals(allowedExtensions, that.allowedExtensions)
                && Objects.equals(allowedScopes, that.allowedScopes)
                && defaultScope == that.defaultScope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, displayName, description, suggestedFileName,
                allowedExtensions, allowedScopes, defaultScope, required);
    }

    @Override
    public String toString() {
        return "RoutineConfigurationFileParameter{" + "key=" + key
                + ", displayName=" + displayName + ", description=" + description
                + ", suggestedFileName=" + suggestedFileName
                + ", allowedExtensions=" + allowedExtensions
                + ", allowedScopes=" + allowedScopes + ", defaultScope=" + defaultScope
                + ", required=" + required + '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }

    private static List<String> copyDistinctTextValues(List<String> values, String name) {
        List<String> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.stream().anyMatch(value -> value.isBlank())) {
            throw new IllegalArgumentException(name + " must contain non-blank values");
        }
        if (copy.stream().distinct().count() != copy.size()) {
            throw new IllegalArgumentException(name + " must not contain duplicates");
        }
        return copy;
    }

    private static void validateFileName(String value) {
        if (value.contains("/") || value.contains("\\") || value.contains("..")) {
            throw new IllegalArgumentException(
                    "suggestedFileName must be a file name without path segments"
            );
        }
    }

    /** Fluent builder for immutable routine configuration-file metadata. */
    public static final class Builder {
        private String key;
        private String displayName;
        private String description = "";
        private String suggestedFileName;
        private List<String> allowedExtensions = List.of();
        private List<ConfigurationFileScope> allowedScopes = List.of();
        private ConfigurationFileScope defaultScope;
        private boolean required;
        private Builder() { }
        public Builder key(String value) { key = value; return this; }
        public Builder displayName(String value) { displayName = value; return this; }
        public Builder description(String value) { description = value; return this; }
        public Builder suggestedFileName(String value) { suggestedFileName = value; return this; }
        public Builder allowedExtensions(List<String> value) {
            allowedExtensions = List.copyOf(Objects.requireNonNull(value, "allowedExtensions"));
            return this;
        }
        public Builder allowedScopes(List<ConfigurationFileScope> value) {
            allowedScopes = List.copyOf(Objects.requireNonNull(value, "allowedScopes"));
            return this;
        }
        public Builder defaultScope(ConfigurationFileScope value) {
            defaultScope = value;
            return this;
        }
        public Builder required(boolean value) { required = value; return this; }
        public RoutineConfigurationFileParameter build() {
            return new RoutineConfigurationFileParameter(this);
        }
    }
}
