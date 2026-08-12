package io.github.byzatic.lib.configio.application.loader;

import io.github.byzatic.lib.configio.application.dao.NodeGlobalDaoInterface;
import io.github.byzatic.lib.configio.application.dao.PipelineDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectGlobalDaoInterface;
import io.github.byzatic.lib.configio.application.dao.SharedResourcesDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.domain.model.SharedResourcesContainerDataObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class ProjectV1LoaderStrategy implements ProjectLoaderInterface {

    public static final String SUPPORTED_PROJECT_VERSION =
            "v1.0.0-SingleRootStrictNestedNodeTree";

    private final ProjectDaoInterface projectDao;
    private final ProjectGlobalDaoInterface projectGlobalDao;
    private final NodeGlobalDaoInterface nodeGlobalDao;
    private final PipelineDaoInterface pipelineDao;
    private final SharedResourcesDaoInterface sharedResourcesDao;

    public ProjectV1LoaderStrategy(
            ProjectDaoInterface projectDao,
            ProjectGlobalDaoInterface projectGlobalDao,
            NodeGlobalDaoInterface nodeGlobalDao,
            PipelineDaoInterface pipelineDao,
            SharedResourcesDaoInterface sharedResourcesDao
    ) {
        this.projectDao = Objects.requireNonNull(projectDao, "projectDao");
        this.projectGlobalDao = Objects.requireNonNull(projectGlobalDao, "projectGlobalDao");
        this.nodeGlobalDao = Objects.requireNonNull(nodeGlobalDao, "nodeGlobalDao");
        this.pipelineDao = Objects.requireNonNull(pipelineDao, "pipelineDao");
        this.sharedResourcesDao = Objects.requireNonNull(sharedResourcesDao, "sharedResourcesDao");
    }

    @Override
    public ProjectLoadResultDataObject load(Path projectDirectory)
            throws ProjectLoadingException {
        Path normalizedProjectDirectory = validateProjectDirectory(projectDirectory);
        SharedResourcesContainerDataObject sharedResources = null;

        try {
            ProjectStructureDataObject projectStructure =
                    projectDao.load(normalizedProjectDirectory);
            validateVersion(projectStructure);
            ProjectGlobalDataObject global = projectGlobalDao.load(normalizedProjectDirectory);
            Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> nodeGlobals =
                    nodeGlobalDao.load(normalizedProjectDirectory, projectStructure);
            Map<GraphNodeReferenceDataObject, PipelineDataObject> pipelines =
                    pipelineDao.load(normalizedProjectDirectory, projectStructure);
            sharedResources = sharedResourcesDao.load(normalizedProjectDirectory);

            NodeContainerDataObject nodeContainer = new NodeContainerDataObject(
                    projectStructure,
                    nodeGlobals,
                    pipelines
            );
            return new ProjectLoadResultDataObject(
                    normalizedProjectDirectory,
                    global,
                    nodeContainer,
                    sharedResources
            );
        } catch (ProjectLoadingException exception) {
            closeAfterFailure(sharedResources, exception);
            throw exception;
        } catch (RuntimeException exception) {
            closeAfterFailure(sharedResources, exception);
            throw new ProjectLoadingException("Cannot load project", exception);
        }
    }

    private Path validateProjectDirectory(Path projectDirectory)
            throws ProjectLoadingException {
        if (projectDirectory == null) {
            throw new ProjectLoadingException("Project directory must not be null");
        }
        Path normalizedPath = projectDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalizedPath)) {
            throw new ProjectLoadingException(
                    "Project directory does not exist: " + normalizedPath
            );
        }
        return normalizedPath;
    }

    private void validateVersion(ProjectStructureDataObject projectStructure)
            throws ProjectLoadingException {
        String version = projectStructure.getProject().getProjectConfigVersion();
        if (!SUPPORTED_PROJECT_VERSION.equals(version)) {
            throw new ProjectLoadingException("Unsupported project version: " + version);
        }
    }

    private void closeAfterFailure(
            SharedResourcesContainerDataObject sharedResources,
            Throwable failure
    ) {
        if (sharedResources == null) {
            return;
        }
        try {
            sharedResources.close();
        } catch (IOException exception) {
            failure.addSuppressed(exception);
        }
    }
}
