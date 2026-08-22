package io.github.byzatic.lib.configio.unified.model;

import java.util.Objects;

/** One immutable named configuration value of a service declaration. */
public final class ServiceOption {

    private final String name;
    private final String data;

    private ServiceOption(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name");
        this.data = Objects.requireNonNull(builder.data, "data");

    }

    /** Returns a new builder for ServiceOption. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the option name. */
    public String getName() {
        return name;
    }

    /** Returns the option data. */
    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceOption)) {
            return false;
        }
        ServiceOption that = (ServiceOption) object;
        return Objects.equals(name, that.name)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, data);
    }

    @Override
    public String toString() {
        return "ServiceOption{" +
                "name=" + name
                 + ", data=" + data +
                '}';
    }

    /** Fluent builder for immutable ServiceOption values. */
    public static final class Builder {

        private String name;
        private String data;

        private Builder() {
        }

        /** Sets the option name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the option data. */
        public Builder data(String value) {
            this.data = value;
            return this;
        }

        /** Builds and validates an immutable ServiceOption. */
        public ServiceOption build() {
            return new ServiceOption(this);
        }
    }
}

