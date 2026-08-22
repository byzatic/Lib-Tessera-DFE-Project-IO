package io.github.byzatic.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable editor-facing declaration of a workflow-routine function. */
public final class RoutineFunction {

    private final String id;
    private final String displayName;
    private final String description;
    private final List<String> widgetIds;
    private final List<String> argumentIds;

    private RoutineFunction(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.widgetIds = List.copyOf(Objects.requireNonNull(builder.widgetIds, "widgetIds"));
        this.argumentIds = List.copyOf(Objects.requireNonNull(builder.argumentIds, "argumentIds"));

    }

    /** Returns a new builder for RoutineFunction. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the function identifier. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the function description. */
    public String getDescription() {
        return description;
    }

    /** Returns the immutable BDUI widget identifiers. */
    public List<String> getWidgetIds() {
        return widgetIds;
    }

    /** Returns the immutable argument identifiers. */
    public List<String> getArgumentIds() {
        return argumentIds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineFunction)) {
            return false;
        }
        RoutineFunction that = (RoutineFunction) object;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(widgetIds, that.widgetIds)
                && Objects.equals(argumentIds, that.argumentIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, widgetIds, argumentIds);
    }

    @Override
    public String toString() {
        return "RoutineFunction{" +
                "id=" + id
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", widgetIds=" + widgetIds
                 + ", argumentIds=" + argumentIds +
                '}';
    }

    /** Fluent builder for immutable RoutineFunction values. */
    public static final class Builder {

        private String id;
        private String displayName;
        private String description;
        private List<String> widgetIds = List.of();
        private List<String> argumentIds = List.of();

        private Builder() {
        }

        /** Sets the function identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the display name. */
        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        /** Sets the function description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the immutable BDUI widget identifiers. */
        public Builder widgetIds(List<String> value) {
            this.widgetIds = List.copyOf(Objects.requireNonNull(value, "widgetIds"));
            return this;
        }

        /** Sets the immutable argument identifiers. */
        public Builder argumentIds(List<String> value) {
            this.argumentIds = List.copyOf(Objects.requireNonNull(value, "argumentIds"));
            return this;
        }

        /** Builds and validates an immutable RoutineFunction. */
        public RoutineFunction build() {
            return new RoutineFunction(this);
        }
    }
}

