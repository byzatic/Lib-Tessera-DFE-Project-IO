package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.Objects;

/** Stable identifier used to connect project nodes. */
public final class NodeId {

    private final String value;

    private NodeId(Builder builder) {
        this.value = Objects.requireNonNull(builder.value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Node id must not be blank");
        }
    }

    /** Returns a new builder for NodeId. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the node identifier. */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NodeId)) {
            return false;
        }
        NodeId that = (NodeId) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NodeId{" +
                "value=" + value +
                '}';
    }

    /** Fluent builder for immutable NodeId values. */
    public static final class Builder {

        private String value;

        private Builder() {
        }

        /** Sets the node identifier. */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /** Builds and validates an immutable NodeId. */
        public NodeId build() {
            return new NodeId(this);
        }
    }
}

