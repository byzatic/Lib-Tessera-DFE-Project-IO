package io.github.byzatic.lib.configio.infrastructure.util;

import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.project_and_graph.GraphStructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ProjectStructureFlattenerUtility {

    public Map<GraphNodeReferenceDataObject, NodeDataObject> flatten(
            GraphStructure rootNode
    ) throws ProjectLoadingException {
        if (rootNode == null) {
            throw new ProjectLoadingException("Project structure is missing");
        }

        Map<GraphNodeReferenceDataObject, NodeDataObject> nodes =
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeDataObject>();
        flattenNode(rootNode, null, nodes);
        return nodes;
    }

    private void flattenNode(
            GraphStructure graphNode,
            GraphNodeReferenceDataObject nodeReference,
            Map<GraphNodeReferenceDataObject, NodeDataObject> nodes
    ) throws ProjectLoadingException {
        validateNode(graphNode);

        GraphNodeReferenceDataObject currentReference = nodeReference;
        if (currentReference == null) {
            currentReference = createReference(graphNode);
        }

        List<GraphNodeReferenceDataObject> downstreamReferences =
                new ArrayList<GraphNodeReferenceDataObject>();
        List<GraphStructure> downstreamNodes = graphNode.getDownstream();
        if (downstreamNodes != null) {
            for (GraphStructure downstreamNode : downstreamNodes) {
                if (downstreamNode == null) {
                    throw new ProjectLoadingException(
                            "Project structure contains a null downstream node"
                    );
                }
                downstreamReferences.add(createReference(downstreamNode));
            }
        }

        NodeDataObject node = new NodeDataObject(
                currentReference.getNodeUuid(),
                graphNode.getId(),
                graphNode.getName(),
                graphNode.getDescription(),
                downstreamReferences
        );
        nodes.put(currentReference, node);

        if (downstreamNodes != null) {
            for (int index = 0; index < downstreamNodes.size(); index++) {
                flattenNode(
                        downstreamNodes.get(index),
                        downstreamReferences.get(index),
                        nodes
                );
            }
        }
    }

    private GraphNodeReferenceDataObject createReference(GraphStructure graphNode)
            throws ProjectLoadingException {
        validateNode(graphNode);
        String nodeUuid = UUID.randomUUID()
                + "-" + graphNode.getId()
                + "-" + graphNode.getName();
        return new GraphNodeReferenceDataObject(nodeUuid);
    }

    private void validateNode(GraphStructure graphNode) throws ProjectLoadingException {
        if (graphNode == null) {
            throw new ProjectLoadingException("Project node must not be null");
        }
        if (graphNode.getId() == null || graphNode.getId().isBlank()) {
            throw new ProjectLoadingException("Project node id must not be blank");
        }
        if (graphNode.getName() == null || graphNode.getName().isBlank()) {
            throw new ProjectLoadingException("Project node name must not be blank");
        }
    }
}
