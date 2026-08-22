package io.github.byzatic.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Complete immutable view of one node, including configuration and pipeline. */
public final class ProjectNode {

    private final NodeId nodeId;
    private final String id;
    private final String name;
    private final String description;
    private final List<NodeId> downstream;
    private final NodeConfiguration configuration;
    private final Pipeline pipeline;

    private ProjectNode(Builder builder) {
        this.nodeId = Objects.requireNonNull(builder.nodeId, "nodeId");
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.downstream = List.copyOf(Objects.requireNonNull(builder.downstream, "downstream"));
        this.configuration = Objects.requireNonNull(builder.configuration, "configuration");
        this.pipeline = Objects.requireNonNull(builder.pipeline, "pipeline");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Node logical id must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Node name must not be blank");
        }
        if (downstream.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Downstream nodes must not contain null");
        }
    }

    /** Returns a new builder for ProjectNode. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the stable node identifier. */
    public NodeId getNodeId() {
        return nodeId;
    }

    /** Returns the logical node identifier. */
    public String getId() {
        return id;
    }

    /** Returns the node name. */
    public String getName() {
        return name;
    }

    /** Returns the node description. */
    public String getDescription() {
        return description;
    }

    /** Returns the immutable downstream node identifiers. */
    public List<NodeId> getDownstream() {
        return downstream;
    }

    /** Returns the node-local configuration. */
    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    /** Returns the node pipeline. */
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProjectNode)) {
            return false;
        }
        ProjectNode that = (ProjectNode) object;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(downstream, that.downstream)
                && Objects.equals(configuration, that.configuration)
                && Objects.equals(pipeline, that.pipeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, id, name, description, downstream, configuration, pipeline);
    }

    @Override
    public String toString() {
        return "ProjectNode{" +
                "nodeId=" + nodeId
                 + ", id=" + id
                 + ", name=" + name
                 + ", description=" + description
                 + ", downstream=" + downstream
                 + ", configuration=" + configuration
                 + ", pipeline=" + pipeline +
                '}';
    }

    /** Fluent builder for immutable ProjectNode values. */
    public static final class Builder {

        private NodeId nodeId;
        private String id;
        private String name;
        private String description;
        private List<NodeId> downstream = List.of();
        private NodeConfiguration configuration;
        private Pipeline pipeline;

        private Builder() {
        }

        /** Sets the stable node identifier. */
        public Builder nodeId(NodeId value) {
            this.nodeId = value;
            return this;
        }

        /** Sets the logical node identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the node name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the node description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the immutable downstream node identifiers. */
        public Builder downstream(List<NodeId> value) {
            this.downstream = List.copyOf(Objects.requireNonNull(value, "downstream"));
            return this;
        }

        /** Sets the node-local configuration. */
        public Builder configuration(NodeConfiguration value) {
            this.configuration = value;
            return this;
        }

        /** Sets the node pipeline. */
        public Builder pipeline(Pipeline value) {
            this.pipeline = value;
            return this;
        }

        /** Builds and validates an immutable ProjectNode. */
        public ProjectNode build() {
            return new ProjectNode(this);
        }
    }
}

