package io.github.byzatic.lib.configio.unified;

import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;
import java.util.Objects;

/** Immutable dependencies required to create one workflow-routine instance. */
public final class RoutineCreationRequest {

    private final String routineName;
    private final MCg3WorkflowRoutineApiInterface api;
    private final HealthFlagProxy health;

    private RoutineCreationRequest(Builder builder) {
        this.routineName = Objects.requireNonNull(builder.routineName, "routineName");
        this.api = Objects.requireNonNull(builder.api, "api");
        this.health = Objects.requireNonNull(builder.health, "health");
        if (routineName.isBlank()) {
            throw new IllegalArgumentException("Routine name must not be blank");
        }
    }

    /** Returns a new builder for RoutineCreationRequest. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the workflow-routine name. */
    public String getRoutineName() {
        return routineName;
    }

    /** Returns the workflow-routine API. */
    public MCg3WorkflowRoutineApiInterface getApi() {
        return api;
    }

    /** Returns the workflow-routine health proxy. */
    public HealthFlagProxy getHealth() {
        return health;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineCreationRequest)) {
            return false;
        }
        RoutineCreationRequest that = (RoutineCreationRequest) object;
        return Objects.equals(routineName, that.routineName)
                && Objects.equals(api, that.api)
                && Objects.equals(health, that.health);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineName, api, health);
    }

    @Override
    public String toString() {
        return "RoutineCreationRequest{" +
                "routineName=" + routineName
                 + ", api=" + api
                 + ", health=" + health +
                '}';
    }

    /** Fluent builder for immutable RoutineCreationRequest values. */
    public static final class Builder {

        private String routineName;
        private MCg3WorkflowRoutineApiInterface api;
        private HealthFlagProxy health;

        private Builder() {
        }

        /** Sets the workflow-routine name. */
        public Builder routineName(String value) {
            this.routineName = value;
            return this;
        }

        /** Sets the workflow-routine API. */
        public Builder api(MCg3WorkflowRoutineApiInterface value) {
            this.api = value;
            return this;
        }

        /** Sets the workflow-routine health proxy. */
        public Builder health(HealthFlagProxy value) {
            this.health = value;
            return this;
        }

        /** Builds and validates an immutable RoutineCreationRequest. */
        public RoutineCreationRequest build() {
            return new RoutineCreationRequest(this);
        }
    }
}

