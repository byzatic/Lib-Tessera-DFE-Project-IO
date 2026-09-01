package io.github.byzatic.tessera.lib.configio.unified.spi.routine;

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
        if (functions.stream()
                .map(RoutineFunctionDescriptor::getFunctionId)
                .distinct()
                .count() != functions.size()) {
            throw new IllegalArgumentException("functionId must be unique within a routine");
        }
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
                && Objects.equals(functions, that.functions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineId, displayName, description, functions);
    }

    @Override
    public String toString() {
        return "RoutineEditorDescriptor{" +
                "routineId=" + routineId
                 + ", displayName=" + displayName
                 + ", description=" + description
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

    /** Fluent builder for immutable routine editor descriptors. */
    public static final class Builder {

        private String routineId;
        private String displayName;
        private String description = "";
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

        public Builder functions(List<RoutineFunctionDescriptor> values) {
            this.functions = List.copyOf(Objects.requireNonNull(values, "functions"));
            return this;
        }

        public RoutineEditorDescriptor build() {
            return new RoutineEditorDescriptor(this);
        }
    }
}
