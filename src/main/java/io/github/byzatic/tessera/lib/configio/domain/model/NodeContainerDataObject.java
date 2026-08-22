package io.github.byzatic.tessera.lib.configio.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class NodeContainerDataObject {

    private final ProjectStructureDataObject projectStructure;
    private final Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> nodeGlobals;
    private final Map<GraphNodeReferenceDataObject, PipelineDataObject> pipelines;

    public NodeContainerDataObject(
            ProjectStructureDataObject projectStructure,
            Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> nodeGlobals,
            Map<GraphNodeReferenceDataObject, PipelineDataObject> pipelines
    ) {
        this.projectStructure = Objects.requireNonNull(projectStructure, "projectStructure");
        Objects.requireNonNull(nodeGlobals, "nodeGlobals");
        Objects.requireNonNull(pipelines, "pipelines");
        if (!projectStructure.getNodes().keySet().equals(nodeGlobals.keySet())) {
            throw new IllegalArgumentException(
                    "Node global map must contain every project node"
            );
        }
        if (!projectStructure.getNodes().keySet().equals(pipelines.keySet())) {
            throw new IllegalArgumentException(
                    "Pipeline map must contain every project node"
            );
        }
        this.nodeGlobals = Collections.unmodifiableMap(
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeGlobalDataObject>(nodeGlobals)
        );
        this.pipelines = Collections.unmodifiableMap(
                new LinkedHashMap<GraphNodeReferenceDataObject, PipelineDataObject>(pipelines)
        );
    }

    public ProjectStructureDataObject getProjectStructure() {
        return projectStructure;
    }

    public Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> getNodeGlobals() {
        return nodeGlobals;
    }

    public Map<GraphNodeReferenceDataObject, PipelineDataObject> getPipelines() {
        return pipelines;
    }

    public NodeGlobalDataObject getNodeGlobal(GraphNodeReferenceDataObject nodeReference) {
        NodeGlobalDataObject nodeGlobal = nodeGlobals.get(nodeReference);
        if (nodeGlobal == null) {
            throw new IllegalArgumentException("Node global is not loaded: " + nodeReference);
        }
        return nodeGlobal;
    }

    public PipelineDataObject getPipeline(GraphNodeReferenceDataObject nodeReference) {
        PipelineDataObject pipeline = pipelines.get(nodeReference);
        if (pipeline == null) {
            throw new IllegalArgumentException("Node pipeline is not loaded: " + nodeReference);
        }
        return pipeline;
    }
}
