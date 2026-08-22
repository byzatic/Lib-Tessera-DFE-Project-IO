package io.github.byzatic.tessera.lib.configio.routine_spi;

import java.util.List;
import java.util.Objects;

/** Immutable declaration of a DSL function implemented by a workflow routine. */
public final class RoutineFunctionDescriptor {

    private final String functionId;
    private final String displayName;
    private final String description;
    private final List<String> bduiWidgetIds;
    private final List<String> argumentIds;

    private RoutineFunctionDescriptor(Builder builder) {
        this.functionId = requireText(builder.functionId, "functionId");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.bduiWidgetIds = List.copyOf(
                Objects.requireNonNull(builder.bduiWidgetIds, "bduiWidgetIds")
        );
        this.argumentIds = List.copyOf(
                Objects.requireNonNull(builder.argumentIds, "argumentIds")
        );
        if (bduiWidgetIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("bduiWidgetIds must contain non-blank values");
        }
        if (bduiWidgetIds.stream().distinct().count() != bduiWidgetIds.size()) {
            throw new IllegalArgumentException("bduiWidgetIds must not contain duplicates");
        }
        if (argumentIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("argumentIds must contain non-blank values");
        }
        if (argumentIds.stream().distinct().count() != argumentIds.size()) {
            throw new IllegalArgumentException("argumentIds must not contain duplicates");
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getFunctionId() {
        return functionId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getBduiWidgetIds() {
        return bduiWidgetIds;
    }

    public List<String> getArgumentIds() {
        return argumentIds;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public static final class Builder {

        private String functionId;
        private String displayName;
        private String description = "";
        private List<String> bduiWidgetIds = List.of();
        private List<String> argumentIds = List.of();

        private Builder() {
        }

        public Builder functionId(String functionId) {
            this.functionId = functionId;
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

        public Builder bduiWidgetIds(List<String> bduiWidgetIds) {
            this.bduiWidgetIds = List.copyOf(
                    Objects.requireNonNull(bduiWidgetIds, "bduiWidgetIds")
            );
            return this;
        }

        public Builder argumentIds(List<String> argumentIds) {
            this.argumentIds = List.copyOf(
                    Objects.requireNonNull(argumentIds, "argumentIds")
            );
            return this;
        }

        public RoutineFunctionDescriptor build() {
            return new RoutineFunctionDescriptor(this);
        }
    }
}
