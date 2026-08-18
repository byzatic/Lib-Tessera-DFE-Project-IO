package io.github.byzatic.lib.configio.domain.model;

import java.util.Objects;

public final class GraphNodeReferenceDataObject {

    private final String nodeUuid;

    public GraphNodeReferenceDataObject(String nodeUuid) {
        if (nodeUuid == null || nodeUuid.isBlank()) {
            throw new IllegalArgumentException("nodeUuid must not be blank");
        }
        this.nodeUuid = nodeUuid;
    }

    public String getNodeUuid() {
        return nodeUuid;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof GraphNodeReferenceDataObject)) {
            return false;
        }
        GraphNodeReferenceDataObject that = (GraphNodeReferenceDataObject) object;
        return Objects.equals(nodeUuid, that.nodeUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeUuid);
    }

    @Override
    public String toString() {
        return "GraphNodeReferenceDataObject{" +
                "nodeUuid='" + nodeUuid + '\'' +
                '}';
    }
}
