package io.github.byzatic.tessera.lib.configio.routine_spi;

import java.util.List;
import java.util.Objects;

/** Immutable editor-facing declaration of one workflow routine. */
public final class RoutineEditorDescriptor {

    private final String routineId;
    private final String displayName;
    private final String description;
    private final List<RoutineFunctionDescriptor> functions;

    private RoutineEditorDescriptor(Builder builder) {
        this.routineId = requireText(builder.routineId, "routineId");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.functions = List.copyOf(Objects.requireNonNull(builder.functions, "functions"));
        if (functions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("functions must not contain null");
        }
        long distinctFunctionIds = functions.stream()
                .map(RoutineFunctionDescriptor::getFunctionId)
                .distinct()
                .count();
        if (distinctFunctionIds != functions.size()) {
            throw new IllegalArgumentException("functionId must be unique within a routine");
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getRoutineId() {
        return routineId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<RoutineFunctionDescriptor> getFunctions() {
        return functions;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public static final class Builder {

        private String routineId;
        private String displayName;
        private String description = "";
        private List<RoutineFunctionDescriptor> functions = List.of();

        private Builder() {
        }

        public Builder routineId(String routineId) {
            this.routineId = routineId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder functions(List<RoutineFunctionDescriptor> functions) {
            this.functions = List.copyOf(Objects.requireNonNull(functions, "functions"));
            return this;
        }

        public RoutineEditorDescriptor build() {
            return new RoutineEditorDescriptor(this);
        }
    }
}
