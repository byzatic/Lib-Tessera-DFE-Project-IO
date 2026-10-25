package io.github.byzatic.tessera.lib.configio.unified.spi.routine;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable editor-facing declaration of one workflow routine. */
public final class RoutineEditorDescriptor {

    private final String routineId;
    private final String displayName;
    private final String description;
    private final List<String> routineWidgetIds;
    private final List<RoutineEnvironmentDescriptor> environment;
    private final boolean allowCustomEnvironmentKeys;
    private final List<RoutineConfigurationFileDescriptor> configurationFiles;
    private final List<RoutineFunctionDescriptor> functions;

    private RoutineEditorDescriptor(Builder builder) {
        this.routineId = requireText(builder.routineId, "routineId");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.routineWidgetIds = copyDistinctTextValues(
                builder.routineWidgetIds,
                "routineWidgetIds"
        );
        this.environment = copyValues(builder.environment, "environment");
        this.allowCustomEnvironmentKeys = builder.allowCustomEnvironmentKeys;
        this.configurationFiles = copyValues(
                builder.configurationFiles,
                "configurationFiles"
        );
        this.functions = List.copyOf(Objects.requireNonNull(builder.functions, "functions"));
        if (functions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("functions must not contain null");
        }
        if (functions.stream()
                .map(RoutineFunctionDescriptor::getFunctionId)
                .distinct()
                .count() != functions.size()) {
            throw new IllegalArgumentException("functionId must be unique within a routine");
        }
        requireUniqueKeys(environment.stream()
                .map(RoutineEnvironmentDescriptor::getKey).toList(), "environment");
        requireUniqueKeys(configurationFiles.stream()
                .map(RoutineConfigurationFileDescriptor::getKey).toList(),
                "configurationFiles");
        Set<String> environmentKeys = Set.copyOf(environment.stream()
                .map(RoutineEnvironmentDescriptor::getKey).toList());
        if (configurationFiles.stream().map(RoutineConfigurationFileDescriptor::getKey)
                .anyMatch(environmentKeys::contains)) {
            throw new IllegalArgumentException(
                    "a key must not be declared as both environment and configuration file"
            );
        }
        validateWidgetIds();
    }

    /** Returns a new builder for a routine editor descriptor. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the stable routine identifier. */
    public String getRoutineId() {
        return routineId;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the routine description. */
    public String getDescription() {
        return description;
    }

    /** Returns immutable routine-level BDUI widget identifiers. */
    public List<String> getRoutineWidgetIds() {
        return routineWidgetIds;
    }

    /** Returns immutable routine environment declarations. */
    public List<RoutineEnvironmentDescriptor> getEnvironment() {
        return environment;
    }

    /** Returns whether users may add environment keys not declared by the routine. */
    public boolean isAllowCustomEnvironmentKeys() {
        return allowCustomEnvironmentKeys;
    }

    /** Returns immutable routine configuration-file declarations. */
    public List<RoutineConfigurationFileDescriptor> getConfigurationFiles() {
        return configurationFiles;
    }

    /** Returns immutable function declarations. */
    public List<RoutineFunctionDescriptor> getFunctions() {
        return functions;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineEditorDescriptor)) {
            return false;
        }
        RoutineEditorDescriptor that = (RoutineEditorDescriptor) object;
        return Objects.equals(routineId, that.routineId)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(routineWidgetIds, that.routineWidgetIds)
                && Objects.equals(environment, that.environment)
                && allowCustomEnvironmentKeys == that.allowCustomEnvironmentKeys
                && Objects.equals(configurationFiles, that.configurationFiles)
                && Objects.equals(functions, that.functions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineId, displayName, description, routineWidgetIds,
                environment, allowCustomEnvironmentKeys, configurationFiles, functions);
    }

    @Override
    public String toString() {
        return "RoutineEditorDescriptor{" +
                "routineId=" + routineId
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", routineWidgetIds=" + routineWidgetIds
                 + ", environment=" + environment
                 + ", allowCustomEnvironmentKeys=" + allowCustomEnvironmentKeys
                 + ", configurationFiles=" + configurationFiles
                 + ", functions=" + functions +
                '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static <T> List<T> copyValues(List<T> values, String name) {
        List<T> copy = List.copyOf(Objects.requireNonNull(values, name));
        if (copy.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(name + " must not contain null");
        }
        return copy;
    }

    private static List<String> copyDistinctTextValues(List<String> values, String name) {
        List<String> copy = copyValues(values, name);
        if (copy.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(name + " must contain non-blank values");
        }
        if (copy.stream().distinct().count() != copy.size()) {
            throw new IllegalArgumentException(name + " must not contain duplicates");
        }
        return copy;
    }

    private static void requireUniqueKeys(List<String> ids, String name) {
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException(
                    "key must be unique within " + name
            );
        }
    }

    private void validateWidgetIds() {
        Set<String> routineIds = Set.of(
                BduiWidgetIds.ROUTINE_ENV,
                BduiWidgetIds.ROUTINE_CONFIGURATION_FILE
        );
        if (routineWidgetIds.stream().anyMatch(id -> !routineIds.contains(id))) {
            throw new IllegalArgumentException("routineWidgetIds contains an unknown BDUI ID");
        }
        Set<String> functionIds = Set.of(
                BduiWidgetIds.FUNC_ENV,
                BduiWidgetIds.FUNC_INPUT_DATA,
                BduiWidgetIds.FUNC_OUTPUT_DATA,
                BduiWidgetIds.INPUT_FROM_DOWNSTREAM
        );
        if (functions.stream().flatMap(function -> function.getBduiWidgetIds().stream())
                .anyMatch(id -> !functionIds.contains(id))) {
            throw new IllegalArgumentException("function widget IDs contain an unknown BDUI ID");
        }
        boolean requestsEnvironment = routineWidgetIds.contains(BduiWidgetIds.ROUTINE_ENV);
        if (requestsEnvironment != (!environment.isEmpty() || allowCustomEnvironmentKeys)) {
            throw new IllegalArgumentException(
                    "RoutineENV must be requested exactly when environment fields or custom keys "
                            + "are enabled"
            );
        }
        boolean requestsFiles = routineWidgetIds.contains(
                BduiWidgetIds.ROUTINE_CONFIGURATION_FILE
        );
        if (requestsFiles != !configurationFiles.isEmpty()) {
            throw new IllegalArgumentException(
                    "RoutineConfigurationFile must be requested exactly when files are declared"
            );
        }
    }

    /** Fluent builder for immutable routine editor descriptors. */
    public static final class Builder {

        private String routineId;
        private String displayName;
        private String description = "";
        private List<String> routineWidgetIds = List.of();
        private List<RoutineEnvironmentDescriptor> environment = List.of();
        private boolean allowCustomEnvironmentKeys;
        private List<RoutineConfigurationFileDescriptor> configurationFiles = List.of();
        private List<RoutineFunctionDescriptor> functions = List.of();

        private Builder() {
        }

        public Builder routineId(String value) {
            this.routineId = value;
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

        public Builder routineWidgetIds(List<String> values) {
            this.routineWidgetIds = List.copyOf(
                    Objects.requireNonNull(values, "routineWidgetIds")
            );
            return this;
        }

        public Builder environment(List<RoutineEnvironmentDescriptor> values) {
            this.environment = List.copyOf(Objects.requireNonNull(values, "environment"));
            return this;
        }

        public Builder allowCustomEnvironmentKeys(boolean value) {
            this.allowCustomEnvironmentKeys = value;
            return this;
        }

        public Builder configurationFiles(List<RoutineConfigurationFileDescriptor> values) {
            this.configurationFiles = List.copyOf(
                    Objects.requireNonNull(values, "configurationFiles")
            );
            return this;
        }

        public Builder functions(List<RoutineFunctionDescriptor> values) {
            this.functions = List.copyOf(Objects.requireNonNull(values, "functions"));
            return this;
        }

        public RoutineEditorDescriptor build() {
            return new RoutineEditorDescriptor(this);
        }
    }
}
