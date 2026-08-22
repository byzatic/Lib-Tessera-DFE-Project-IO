package io.github.byzatic.tessera.lib.configio.infrastructure.strategy;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeDataObject;

import java.nio.file.Path;
import java.util.Objects;

public final class NodePathResolverStrategy {

    private static final String NAMED_NODE_ID = "#NAMED";

    public Path resolve(Path projectDirectory, NodeDataObject node)
            throws ProjectLoadingException {
        Objects.requireNonNull(projectDirectory, "projectDirectory");
        Objects.requireNonNull(node, "node");

        String directoryName;
        if (NAMED_NODE_ID.equals(node.getId())) {
            directoryName = node.getName();
        } else {
            directoryName = node.getId() + "-" + node.getName();
        }

        Path nodesDirectory = projectDirectory
                .resolve("data")
                .resolve("nodes")
                .toAbsolutePath()
                .normalize();
        Path nodeDirectory = nodesDirectory.resolve(directoryName).normalize();
        if (!nodeDirectory.startsWith(nodesDirectory)) {
            throw new ProjectLoadingException(
                    "Node directory escapes project nodes directory: " + directoryName
            );
        }
        return nodeDirectory;
    }
}
