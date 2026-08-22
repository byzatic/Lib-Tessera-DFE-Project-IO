package io.github.byzatic.tessera.lib.configio.unified;

import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;
import java.util.Objects;

/** Immutable dependencies required to create one service instance. */
public final class ServiceCreationRequest {

    private final String serviceName;
    private final MCg3ServiceApiInterface api;
    private final HealthFlagProxy health;

    private ServiceCreationRequest(Builder builder) {
        this.serviceName = Objects.requireNonNull(builder.serviceName, "serviceName");
        this.api = Objects.requireNonNull(builder.api, "api");
        this.health = Objects.requireNonNull(builder.health, "health");
        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name must not be blank");
        }
    }

    /** Returns a new builder for ServiceCreationRequest. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the service name. */
    public String getServiceName() {
        return serviceName;
    }

    /** Returns the service API. */
    public MCg3ServiceApiInterface getApi() {
        return api;
    }

    /** Returns the service health proxy. */
    public HealthFlagProxy getHealth() {
        return health;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceCreationRequest)) {
            return false;
        }
        ServiceCreationRequest that = (ServiceCreationRequest) object;
        return Objects.equals(serviceName, that.serviceName)
                && Objects.equals(api, that.api)
                && Objects.equals(health, that.health);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, api, health);
    }

    @Override
    public String toString() {
        return "ServiceCreationRequest{" +
                "serviceName=" + serviceName
                 + ", api=" + api
                 + ", health=" + health +
                '}';
    }

    /** Fluent builder for immutable ServiceCreationRequest values. */
    public static final class Builder {

        private String serviceName;
        private MCg3ServiceApiInterface api;
        private HealthFlagProxy health;

        private Builder() {
        }

        /** Sets the service name. */
        public Builder serviceName(String value) {
            this.serviceName = value;
            return this;
        }

        /** Sets the service API. */
        public Builder api(MCg3ServiceApiInterface value) {
            this.api = value;
            return this;
        }

        /** Sets the service health proxy. */
        public Builder health(HealthFlagProxy value) {
            this.health = value;
            return this;
        }

        /** Builds and validates an immutable ServiceCreationRequest. */
        public ServiceCreationRequest build() {
            return new ServiceCreationRequest(this);
        }
    }
}

