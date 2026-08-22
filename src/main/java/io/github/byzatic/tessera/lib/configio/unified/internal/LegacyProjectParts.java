package io.github.byzatic.tessera.lib.configio.unified.internal;

import io.github.byzatic.tessera.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectGlobalDataObject;

import java.util.Objects;

final class LegacyProjectParts {

    private final ProjectGlobalDataObject global;
    private final NodeContainerDataObject nodeContainer;

    private LegacyProjectParts(Builder builder) {
        global = Objects.requireNonNull(builder.global, "global");
        nodeContainer = Objects.requireNonNull(builder.nodeContainer, "nodeContainer");
    }

    static Builder newBuilder() {
        return new Builder();
    }

    ProjectGlobalDataObject getGlobal() {
        return global;
    }

    NodeContainerDataObject getNodeContainer() {
        return nodeContainer;
    }

    static final class Builder {

        private ProjectGlobalDataObject global;
        private NodeContainerDataObject nodeContainer;

        private Builder() {
        }

        Builder global(ProjectGlobalDataObject value) {
            global = value;
            return this;
        }

        Builder nodeContainer(NodeContainerDataObject value) {
            nodeContainer = value;
            return this;
        }

        LegacyProjectParts build() {
            return new LegacyProjectParts(this);
        }
    }
}
