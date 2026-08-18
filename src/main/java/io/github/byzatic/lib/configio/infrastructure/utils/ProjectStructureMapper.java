package io.github.byzatic.lib.configio.infrastructure.utils;

import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.project_and_graph.GraphStructure;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.project_and_graph.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProjectStructureMapper {

    public Project map(ProjectStructureDataObject source) throws ProjectSavingException {
        if (source == null) {
            throw new ProjectSavingException("Project structure must not be null");
        }

        GraphNodeReferenceDataObject rootReference = findRoot(source);
        Set<GraphNodeReferenceDataObject> visited =
                new HashSet<GraphNodeReferenceDataObject>();
        Set<GraphNodeReferenceDataObject> active =
                new HashSet<GraphNodeReferenceDataObject>();
        GraphStructure root = mapNode(source, rootReference, visited, active);
        if (visited.size() != source.getNodes().size()) {
            throw new ProjectSavingException(
                    "Project structure must be a connected single-root node tree"
            );
        }

        ProjectDataObject project = source.getProject();
        return Project.newBuilder()
                .setProjectConfigVersion(project.getProjectConfigVersion())
                .setProjectName(project.getProjectName())
                .setStructure(root)
                .build();
    }

    private GraphNodeReferenceDataObject findRoot(ProjectStructureDataObject source)
            throws ProjectSavingException {
        if (source.getNodes().isEmpty()) {
            throw new ProjectSavingException("Project structure must contain a root node");
        }

        Map<GraphNodeReferenceDataObject, Integer> parentCounts =
                new HashMap<GraphNodeReferenceDataObject, Integer>();
        for (GraphNodeReferenceDataObject reference : source.getNodes().keySet()) {
            parentCounts.put(reference, Integer.valueOf(0));
        }

        for (NodeDataObject node : source.getNodes().values()) {
            for (GraphNodeReferenceDataObject downstream : node.getDownstream()) {
                Integer parentCount = parentCounts.get(downstream);
                if (parentCount == null) {
                    throw new ProjectSavingException(
                            "Project node references an unknown downstream node: " + downstream
                    );
                }
                int updatedParentCount = parentCount.intValue() + 1;
                if (updatedParentCount > 1) {
                    throw new ProjectSavingException(
                            "Project node has more than one parent: " + downstream
                    );
                }
                parentCounts.put(downstream, Integer.valueOf(updatedParentCount));
            }
        }

        GraphNodeReferenceDataObject root = null;
        for (Map.Entry<GraphNodeReferenceDataObject, Integer> entry
                : parentCounts.entrySet()) {
            if (entry.getValue().intValue() == 0) {
                if (root != null) {
                    throw new ProjectSavingException(
                            "Project structure must contain exactly one root node"
                    );
                }
                root = entry.getKey();
            }
        }
        if (root == null) {
            throw new ProjectSavingException("Project structure does not contain a root node");
        }
        return root;
    }

    private GraphStructure mapNode(
            ProjectStructureDataObject source,
            GraphNodeReferenceDataObject reference,
            Set<GraphNodeReferenceDataObject> visited,
            Set<GraphNodeReferenceDataObject> active
    ) throws ProjectSavingException {
        if (!active.add(reference)) {
            throw new ProjectSavingException(
                    "Project structure contains a cycle at node: " + reference
            );
        }
        if (!visited.add(reference)) {
            throw new ProjectSavingException(
                    "Project structure contains a repeated node: " + reference
            );
        }

        NodeDataObject node = source.getNode(reference);
        if (!reference.getNodeUuid().equals(node.getUuid())) {
            throw new ProjectSavingException(
                    "Project node UUID does not match its reference: " + reference
            );
        }

        List<GraphStructure> downstream = new ArrayList<GraphStructure>();
        for (GraphNodeReferenceDataObject downstreamReference : node.getDownstream()) {
            downstream.add(mapNode(source, downstreamReference, visited, active));
        }
        active.remove(reference);

        return GraphStructure.newBuilder()
                .setId(node.getId())
                .setName(node.getName())
                .setDescription(node.getDescription())
                .setDownstream(downstream)
                .build();
    }
}
