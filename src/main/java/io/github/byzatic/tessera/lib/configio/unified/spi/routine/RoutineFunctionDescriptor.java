package io.github.byzatic.tessera.lib.configio.unified.spi.routine;

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
        this.bduiWidgetIds = copyDistinctTextValues(
                builder.bduiWidgetIds,
                "bduiWidgetIds"
        );
        this.argumentIds = copyDistinctTextValues(builder.argumentIds, "argumentIds");
    }

    /** Returns a new builder for a routine function descriptor. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the stable function identifier. */
    public String getFunctionId() {
        return functionId;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the function description. */
    public String getDescription() {
        return description;
    }

    /** Returns immutable BDUI widget identifiers. */
    public List<String> getBduiWidgetIds() {
        return bduiWidgetIds;
    }

    /** Returns immutable argument identifiers. */
    public List<String> getArgumentIds() {
        return argumentIds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineFunctionDescriptor)) {
            return false;
        }
        RoutineFunctionDescriptor that = (RoutineFunctionDescriptor) object;
        return Objects.equals(functionId, that.functionId)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(bduiWidgetIds, that.bduiWidgetIds)
                && Objects.equals(argumentIds, that.argumentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                functionId,
                displayName,
                description,
                bduiWidgetIds,
                argumentIds
        );
    }

    @Override
    public String toString() {
        return "RoutineFunctionDescriptor{" +
                "functionId=" + functionId
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", bduiWidgetIds=" + bduiWidgetIds
                 + ", argumentIds=" + argumentIds +
                '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
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

    /** Fluent builder for immutable routine function descriptors. */
    public static final class Builder {

        private String functionId;
        private String displayName;
        private String description = "";
        private List<String> bduiWidgetIds = List.of();
        private List<String> argumentIds = List.of();

        private Builder() {
        }

        public Builder functionId(String value) {
            this.functionId = value;
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

        public Builder bduiWidgetIds(List<String> values) {
            this.bduiWidgetIds = List.copyOf(
                    Objects.requireNonNull(values, "bduiWidgetIds")
            );
            return this;
        }

        public Builder argumentIds(List<String> values) {
            this.argumentIds = List.copyOf(Objects.requireNonNull(values, "argumentIds"));
            return this;
        }

        public RoutineFunctionDescriptor build() {
            return new RoutineFunctionDescriptor(this);
        }
    }
}
