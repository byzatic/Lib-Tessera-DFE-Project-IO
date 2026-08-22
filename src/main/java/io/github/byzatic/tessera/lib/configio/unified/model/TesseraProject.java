package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Complete immutable Tessera project exposed by the unified API. */
public final class TesseraProject {

    private final String formatVersion;
    private final String name;
    private final ProjectConfiguration configuration;
    private final Map<NodeId, ProjectNode> nodes;

    private TesseraProject(Builder builder) {
        this.formatVersion = Objects.requireNonNull(builder.formatVersion, "formatVersion");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.configuration = Objects.requireNonNull(builder.configuration, "configuration");
        this.nodes = Collections.unmodifiableMap(
                new LinkedHashMap<NodeId, ProjectNode>(
                        Objects.requireNonNull(builder.nodes, "nodes")
                )
        );
        if (formatVersion.isBlank()) {
            throw new IllegalArgumentException("Project format version must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }
        validateNodes(nodes);
    }

    /** Returns a new builder for TesseraProject. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the project format version. */
    public String getFormatVersion() {
        return formatVersion;
    }

    /** Returns the project name. */
    public String getName() {
        return name;
    }

    /** Returns the global project configuration. */
    public ProjectConfiguration getConfiguration() {
        return configuration;
    }

    /** Returns the immutable nodes indexed by identifier. */
    public Map<NodeId, ProjectNode> getNodes() {
        return nodes;
    }

    private static void validateNodes(Map<NodeId, ProjectNode> nodes) {
        for (Map.Entry<NodeId, ProjectNode> entry : nodes.entrySet()) {
            NodeId nodeId = Objects.requireNonNull(entry.getKey(), "node key");
            ProjectNode node = Objects.requireNonNull(entry.getValue(), "node");
            if (!nodeId.equals(node.getNodeId())) {
                throw new IllegalArgumentException("Node map key does not match node id: " + nodeId);
            }
            for (NodeId downstream : node.getDownstream()) {
                if (!nodes.containsKey(downstream)) {
                    throw new IllegalArgumentException(
                            "Unknown downstream node " + downstream + " from " + nodeId
                    );
                }
            }
        }
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TesseraProject)) {
            return false;
        }
        TesseraProject that = (TesseraProject) object;
        return Objects.equals(formatVersion, that.formatVersion)
                && Objects.equals(name, that.name)
                && Objects.equals(configuration, that.configuration)
                && Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatVersion, name, configuration, nodes);
    }

    @Override
    public String toString() {
        return "TesseraProject{" +
                "formatVersion=" + formatVersion
                 + ", name=" + name
                 + ", configuration=" + configuration
                 + ", nodes=" + nodes +
                '}';
    }

    /** Fluent builder for immutable TesseraProject values. */
    public static final class Builder {

        private String formatVersion;
        private String name;
        private ProjectConfiguration configuration;
        private Map<NodeId, ProjectNode> nodes = Map.of();

        private Builder() {
        }

        /** Sets the project format version. */
        public Builder formatVersion(String value) {
            this.formatVersion = value;
            return this;
        }

        /** Sets the project name. */
        public Builder name(String value) {
            this.name = value;
            return this;
        }

        /** Sets the global project configuration. */
        public Builder configuration(ProjectConfiguration value) {
            this.configuration = value;
            return this;
        }

        /** Sets the immutable nodes indexed by identifier. */
        public Builder nodes(Map<NodeId, ProjectNode> value) {
            this.nodes = new LinkedHashMap<NodeId, ProjectNode>(
                    Objects.requireNonNull(value, "nodes")
            );
            return this;
        }

        /** Builds and validates an immutable TesseraProject. */
        public TesseraProject build() {
            return new TesseraProject(this);
        }
    }
}

