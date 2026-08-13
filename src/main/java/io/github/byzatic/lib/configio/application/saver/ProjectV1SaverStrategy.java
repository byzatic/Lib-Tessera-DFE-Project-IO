package io.github.byzatic.lib.configio.application.saver;

import io.github.byzatic.lib.configio.application.dao.NodeGlobalDaoInterface;
import io.github.byzatic.lib.configio.application.dao.PipelineDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectGlobalDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;

import java.nio.file.Path;
import java.util.Objects;

public final class ProjectV1SaverStrategy implements ProjectSaverInterface {

    public static final String SUPPORTED_PROJECT_VERSION =
            "v1.0.0-SingleRootStrictNestedNodeTree";

    private final ProjectDaoInterface projectDao;
    private final ProjectGlobalDaoInterface projectGlobalDao;
    private final NodeGlobalDaoInterface nodeGlobalDao;
    private final PipelineDaoInterface pipelineDao;

    public ProjectV1SaverStrategy(
            ProjectDaoInterface projectDao,
            ProjectGlobalDaoInterface projectGlobalDao,
            NodeGlobalDaoInterface nodeGlobalDao,
            PipelineDaoInterface pipelineDao
    ) {
        this.projectDao = Objects.requireNonNull(projectDao, "projectDao");
        this.projectGlobalDao = Objects.requireNonNull(projectGlobalDao, "projectGlobalDao");
        this.nodeGlobalDao = Objects.requireNonNull(nodeGlobalDao, "nodeGlobalDao");
        this.pipelineDao = Objects.requireNonNull(pipelineDao, "pipelineDao");
    }

    @Override
    public void save(ProjectLoadResultDataObject project) throws ProjectSavingException {
        if (project == null) {
            throw new ProjectSavingException("Project must not be null");
        }
        save(project.getProjectDirectory(), project.getGlobal(), project.getNodeContainer());
    }

    @Override
    public void save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer
    ) throws ProjectSavingException {
        Path normalizedProjectDirectory = normalizeProjectDirectory(projectDirectory);
        if (global == null) {
            throw new ProjectSavingException("Project global configuration must not be null");
        }
        if (nodeContainer == null) {
            throw new ProjectSavingException("Node container must not be null");
        }

        ProjectStructureDataObject projectStructure = nodeContainer.getProjectStructure();
        validateVersion(projectStructure);

        try {
            projectDao.save(normalizedProjectDirectory, projectStructure);
            projectGlobalDao.save(normalizedProjectDirectory, global);
            nodeGlobalDao.save(
                    normalizedProjectDirectory,
                    projectStructure,
                    nodeContainer.getNodeGlobals()
            );
            pipelineDao.save(
                    normalizedProjectDirectory,
                    projectStructure,
                    nodeContainer.getPipelines()
            );
        } catch (ProjectSavingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ProjectSavingException("Cannot save project", exception);
        }
    }

    private Path normalizeProjectDirectory(Path projectDirectory)
            throws ProjectSavingException {
        if (projectDirectory == null) {
            throw new ProjectSavingException("Project directory must not be null");
        }
        return projectDirectory.toAbsolutePath().normalize();
    }

    private void validateVersion(ProjectStructureDataObject projectStructure)
            throws ProjectSavingException {
        String version = projectStructure.getProject().getProjectConfigVersion();
        if (!SUPPORTED_PROJECT_VERSION.equals(version)) {
            throw new ProjectSavingException("Unsupported project version: " + version);
        }
    }
}
