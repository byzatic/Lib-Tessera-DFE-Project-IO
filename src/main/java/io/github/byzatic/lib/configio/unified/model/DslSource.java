package io.github.byzatic.lib.configio.unified.model;

import java.util.Locale;
import java.util.Objects;

/** Immutable DSL source file stored for a worker in a project node. */
public final class DslSource {

    private final NodeId nodeId;
    private final String baseName;
    private final String content;

    private DslSource(Builder builder) {
        this.nodeId = Objects.requireNonNull(builder.nodeId, "nodeId");
        this.baseName = Objects.requireNonNull(builder.baseName, "baseName");
        this.content = Objects.requireNonNull(builder.content, "content");
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("DSL base name must not be blank");
        }
        if (baseName.contains("/") || baseName.contains("\\")) {
            throw new IllegalArgumentException("DSL base name must not contain path separators");
        }
        if (baseName.toLowerCase(Locale.ROOT).endsWith(".mcg3dsl")) {
            throw new IllegalArgumentException("DSL base name must not include .mcg3dsl");
        }
    }

    /** Returns a new builder for DslSource. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the owning node identifier. */
    public NodeId getNodeId() {
        return nodeId;
    }

    /** Returns the file name without extension. */
    public String getBaseName() {
        return baseName;
    }

    /** Returns the DSL source content. */
    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DslSource)) {
            return false;
        }
        DslSource that = (DslSource) object;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(baseName, that.baseName)
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, baseName, content);
    }

    @Override
    public String toString() {
        return "DslSource{" +
                "nodeId=" + nodeId
                 + ", baseName=" + baseName
                 + ", content=" + content +
                '}';
    }

    /** Fluent builder for immutable DslSource values. */
    public static final class Builder {

        private NodeId nodeId;
        private String baseName;
        private String content;

        private Builder() {
        }

        /** Sets the owning node identifier. */
        public Builder nodeId(NodeId value) {
            this.nodeId = value;
            return this;
        }

        /** Sets the file name without extension. */
        public Builder baseName(String value) {
            this.baseName = value;
            return this;
        }

        /** Sets the DSL source content. */
        public Builder content(String value) {
            this.content = value;
            return this;
        }

        /** Builds and validates an immutable DslSource. */
        public DslSource build() {
            return new DslSource(this);
        }
    }
}

