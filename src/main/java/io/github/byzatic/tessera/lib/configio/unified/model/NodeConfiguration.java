package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable configuration local to one project node. */
public final class NodeConfiguration {

    private final List<StorageDefinition> storages;

    private NodeConfiguration(Builder builder) {
        this.storages = List.copyOf(Objects.requireNonNull(builder.storages, "storages"));
        if (storages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Storages must not contain null");
        }
    }

    /** Returns a new builder for NodeConfiguration. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the immutable node storage declarations. */
    public List<StorageDefinition> getStorages() {
        return storages;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NodeConfiguration)) {
            return false;
        }
        NodeConfiguration that = (NodeConfiguration) object;
        return Objects.equals(storages, that.storages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storages);
    }

    @Override
    public String toString() {
        return "NodeConfiguration{" +
                "storages=" + storages +
                '}';
    }

    /** Fluent builder for immutable NodeConfiguration values. */
    public static final class Builder {

        private List<StorageDefinition> storages = List.of();

        private Builder() {
        }

        /** Sets the immutable node storage declarations. */
        public Builder storages(List<StorageDefinition> value) {
            this.storages = List.copyOf(Objects.requireNonNull(value, "storages"));
            return this;
        }

        /** Builds and validates an immutable NodeConfiguration. */
        public NodeConfiguration build() {
            return new NodeConfiguration(this);
        }
    }
}

