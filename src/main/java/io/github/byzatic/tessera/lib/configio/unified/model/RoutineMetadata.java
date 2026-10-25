package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable detached editor metadata discovered in a workflow-routine JAR. */
public final class RoutineMetadata {

    private final String id;
    private final String displayName;
    private final String description;
    private final String version;
    private final Path artifact;
    private final List<String> routineWidgetIds;
    private final List<RoutineEnvironmentParameter> environmentParameters;
    private final boolean allowCustomEnvironmentKeys;
    private final List<RoutineConfigurationFileParameter> configurationFileParameters;
    private final List<RoutineFunction> functions;

    private RoutineMetadata(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.version = Objects.requireNonNull(builder.version, "version");
        this.artifact = Objects.requireNonNull(builder.artifact, "artifact")
                .toAbsolutePath()
                .normalize();
        this.routineWidgetIds = copyValues(builder.routineWidgetIds, "routineWidgetIds");
        this.environmentParameters = copyValues(
                builder.environmentParameters,
                "environmentParameters"
        );
        this.allowCustomEnvironmentKeys = builder.allowCustomEnvironmentKeys;
        this.configurationFileParameters = copyValues(
                builder.configurationFileParameters,
                "configurationFileParameters"
        );
        this.functions = List.copyOf(Objects.requireNonNull(builder.functions, "functions"));
        validateMetadata();
    }

    /** Returns a new builder for RoutineMetadata. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the routine identifier. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the routine description. */
    public String getDescription() {
        return description;
    }

    /** Returns the artifact implementation version. */
    public String getVersion() {
        return version;
    }

    /** Returns the normalized artifact path. */
    public Path getArtifact() {
        return artifact;
    }

    /** Returns immutable routine-level BDUI widget identifiers. */
    public List<String> getRoutineWidgetIds() { return routineWidgetIds; }

    /** Returns immutable routine environment parameter metadata. */
    public List<RoutineEnvironmentParameter> getEnvironmentParameters() {
        return environmentParameters;
    }

    /** Returns whether users may add environment keys not declared by the routine. */
    public boolean isAllowCustomEnvironmentKeys() {
        return allowCustomEnvironmentKeys;
    }

    /** Returns immutable routine configuration-file parameter metadata. */
    public List<RoutineConfigurationFileParameter> getConfigurationFileParameters() {
        return configurationFileParameters;
    }

    /** Returns the immutable routine functions. */
    public List<RoutineFunction> getFunctions() {
        return functions;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineMetadata)) {
            return false;
        }
        RoutineMetadata that = (RoutineMetadata) object;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(version, that.version)
                && Objects.equals(artifact, that.artifact)
                && Objects.equals(routineWidgetIds, that.routineWidgetIds)
                && Objects.equals(environmentParameters, that.environmentParameters)
                && allowCustomEnvironmentKeys == that.allowCustomEnvironmentKeys
                && Objects.equals(configurationFileParameters, that.configurationFileParameters)
                && Objects.equals(functions, that.functions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, version, artifact, routineWidgetIds,
                environmentParameters, allowCustomEnvironmentKeys,
                configurationFileParameters, functions);
    }

    @Override
    public String toString() {
        return "RoutineMetadata{" +
                "id=" + id
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", version=" + version
                 + ", artifact=" + artifact
                 + ", routineWidgetIds=" + routineWidgetIds
                 + ", environmentParameters=" + environmentParameters
                 + ", allowCustomEnvironmentKeys=" + allowCustomEnvironmentKeys
                 + ", configurationFileParameters=" + configurationFileParameters
                 + ", functions=" + functions +
                '}';
    }

    private static <T> List<T> copyValues(List<T> values, String name) {
        List<T> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(name + " must not contain null");
        }
        return copy;
    }

    private void validateMetadata() {
        requireDistinctTextValues(routineWidgetIds, "routineWidgetIds");
        requireUniqueKeys(environmentParameters.stream()
                .map(RoutineEnvironmentParameter::getKey).toList(), "environmentParameters");
        requireUniqueKeys(configurationFileParameters.stream()
                .map(RoutineConfigurationFileParameter::getKey).toList(),
                "configurationFileParameters");
        Set<String> environmentKeys = Set.copyOf(environmentParameters.stream()
                .map(RoutineEnvironmentParameter::getKey).toList());
        if (configurationFileParameters.stream()
                .map(RoutineConfigurationFileParameter::getKey)
                .anyMatch(environmentKeys::contains)) {
            throw new IllegalArgumentException(
                    "a key must not be declared as both environment and configuration file"
            );
        }
        Set<String> knownRoutineWidgets = Set.of("RoutineENV", "RoutineConfigurationFile");
        if (routineWidgetIds.stream().anyMatch(id -> !knownRoutineWidgets.contains(id))) {
            throw new IllegalArgumentException("routineWidgetIds contains an unknown BDUI ID");
        }
        Set<String> knownFunctionWidgets = Set.of(
                "FuncENV",
                "FuncInputData",
                "FuncOutputData",
                "InpFromDownstr"
        );
        if (functions.stream().flatMap(function -> function.getWidgetIds().stream())
                .anyMatch(id -> !knownFunctionWidgets.contains(id))) {
            throw new IllegalArgumentException("function widget IDs contain an unknown BDUI ID");
        }
        boolean requestsEnvironment = routineWidgetIds.contains("RoutineENV");
        if (requestsEnvironment != (!environmentParameters.isEmpty()
                || allowCustomEnvironmentKeys)) {
            throw new IllegalArgumentException(
                    "RoutineENV must be requested exactly when environment fields or custom keys "
                            + "are enabled"
            );
        }
        boolean requestsFiles = routineWidgetIds.contains("RoutineConfigurationFile");
        if (requestsFiles != !configurationFileParameters.isEmpty()) {
            throw new IllegalArgumentException(
                    "RoutineConfigurationFile must be requested exactly when files are declared"
            );
        }
    }

    private static void requireDistinctTextValues(List<String> values, String name) {
        if (values.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(name + " must contain non-blank values");
        }
        if (values.stream().distinct().count() != values.size()) {
            throw new IllegalArgumentException(name + " must not contain duplicates");
        }
    }

    private static void requireUniqueKeys(List<String> keys, String name) {
        if (keys.stream().distinct().count() != keys.size()) {
            throw new IllegalArgumentException("key must be unique within " + name);
        }
    }

    /** Fluent builder for immutable RoutineMetadata values. */
    public static final class Builder {

        private String id;
        private String displayName;
        private String description;
        private String version;
        private Path artifact;
        private List<String> routineWidgetIds = List.of();
        private List<RoutineEnvironmentParameter> environmentParameters = List.of();
        private boolean allowCustomEnvironmentKeys;
        private List<RoutineConfigurationFileParameter> configurationFileParameters = List.of();
        private List<RoutineFunction> functions = List.of();

        private Builder() {
        }

        /** Sets the routine identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the display name. */
        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        /** Sets the routine description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the artifact implementation version. */
        public Builder version(String value) {
            this.version = value;
            return this;
        }

        /** Sets the normalized artifact path. */
        public Builder artifact(Path value) {
            this.artifact = value;
            return this;
        }

        /** Sets immutable routine-level BDUI widget identifiers. */
        public Builder routineWidgetIds(List<String> value) {
            this.routineWidgetIds = List.copyOf(
                    Objects.requireNonNull(value, "routineWidgetIds")
            );
            return this;
        }

        /** Sets immutable routine environment parameter metadata. */
        public Builder environmentParameters(List<RoutineEnvironmentParameter> value) {
            this.environmentParameters = List.copyOf(
                    Objects.requireNonNull(value, "environmentParameters")
            );
            return this;
        }

        /** Sets whether users may add undeclared routine environment keys. */
        public Builder allowCustomEnvironmentKeys(boolean value) {
            this.allowCustomEnvironmentKeys = value;
            return this;
        }

        /** Sets immutable routine configuration-file parameter metadata. */
        public Builder configurationFileParameters(
                List<RoutineConfigurationFileParameter> value
        ) {
            this.configurationFileParameters = List.copyOf(
                    Objects.requireNonNull(value, "configurationFileParameters")
            );
            return this;
        }

        /** Sets the immutable routine functions. */
        public Builder functions(List<RoutineFunction> value) {
            this.functions = List.copyOf(Objects.requireNonNull(value, "functions"));
            return this;
        }

        /** Builds and validates an immutable RoutineMetadata. */
        public RoutineMetadata build() {
            return new RoutineMetadata(this);
        }
    }
}
