package io.github.byzatic.lib.configio.domain.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class ProjectLoadResultDataObject implements AutoCloseable {

    private final Path projectDirectory;
    private final ProjectGlobalDataObject global;
    private final NodeContainerDataObject nodeContainer;
    private final SharedResourcesContainerDataObject sharedResourcesContainer;

    public ProjectLoadResultDataObject(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer,
            SharedResourcesContainerDataObject sharedResourcesContainer
    ) {
        this.projectDirectory = Objects.requireNonNull(projectDirectory, "projectDirectory");
        this.global = Objects.requireNonNull(global, "global");
        this.nodeContainer = Objects.requireNonNull(nodeContainer, "nodeContainer");
        this.sharedResourcesContainer = Objects.requireNonNull(
                sharedResourcesContainer,
                "sharedResourcesContainer"
        );
    }

    public Path getProjectDirectory() {
        return projectDirectory;
    }

    public ProjectGlobalDataObject getGlobal() {
        return global;
    }

    public NodeContainerDataObject getNodeContainer() {
        return nodeContainer;
    }

    public SharedResourcesContainerDataObject getSharedResourcesContainer() {
        return sharedResourcesContainer;
    }

    @Override
    public void close() throws IOException {
        sharedResourcesContainer.close();
    }
}
