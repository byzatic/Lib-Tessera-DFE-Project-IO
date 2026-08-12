package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProjectStructureDataObject {

    private final ProjectDataObject project;
    private final Map<GraphNodeReferenceDataObject, NodeDataObject> nodes;

    public ProjectStructureDataObject(
            ProjectDataObject project,
            Map<GraphNodeReferenceDataObject, NodeDataObject> nodes
    ) {
        this.project = Objects.requireNonNull(project, "project");
        Objects.requireNonNull(nodes, "nodes");
        this.nodes = Collections.unmodifiableMap(
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeDataObject>(nodes)
        );
    }

    public ProjectDataObject getProject() {
        return project;
    }

    public Map<GraphNodeReferenceDataObject, NodeDataObject> getNodes() {
        return nodes;
    }

    public List<GraphNodeReferenceDataObject> getNodeReferences() {
        return Collections.unmodifiableList(
                new ArrayList<GraphNodeReferenceDataObject>(nodes.keySet())
        );
    }

    public NodeDataObject getNode(GraphNodeReferenceDataObject nodeReference) {
        NodeDataObject node = nodes.get(nodeReference);
        if (node == null) {
            throw new IllegalArgumentException("Unknown node reference: " + nodeReference);
        }
        return node;
    }
}
