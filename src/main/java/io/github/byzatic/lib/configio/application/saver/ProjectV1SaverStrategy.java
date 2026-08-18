package io.github.byzatic.lib.configio.application.saver;

import io.github.byzatic.lib.configio.application.dao.NodeGlobalDaoInterface;
import io.github.byzatic.lib.configio.application.dao.PipelineDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectDaoInterface;
import io.github.byzatic.lib.configio.application.dao.ProjectGlobalDaoInterface;
import io.github.byzatic.lib.configio.application.module.ModuleSaverInterface;
import io.github.byzatic.lib.configio.application.service.ServiceSaverInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginSavingException;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ProjectV1SaverStrategy implements ProjectSaverInterface {

    public static final String SUPPORTED_PROJECT_VERSION =
            "v1.0.0-SingleRootStrictNestedNodeTree";

    private final ProjectDaoInterface projectDao;
    private final ProjectGlobalDaoInterface projectGlobalDao;
    private final NodeGlobalDaoInterface nodeGlobalDao;
    private final PipelineDaoInterface pipelineDao;
    private final ModuleSaverInterface moduleSaver;
    private final ServiceSaverInterface serviceSaver;
    private final ProjectArchiverInterface projectArchiver;

    public ProjectV1SaverStrategy(
            ProjectDaoInterface projectDao,
            ProjectGlobalDaoInterface projectGlobalDao,
            NodeGlobalDaoInterface nodeGlobalDao,
            PipelineDaoInterface pipelineDao,
            ModuleSaverInterface moduleSaver,
            ServiceSaverInterface serviceSaver,
            ProjectArchiverInterface projectArchiver
    ) {
        this.projectDao = Objects.requireNonNull(projectDao, "projectDao");
        this.projectGlobalDao = Objects.requireNonNull(projectGlobalDao, "projectGlobalDao");
        this.nodeGlobalDao = Objects.requireNonNull(nodeGlobalDao, "nodeGlobalDao");
        this.pipelineDao = Objects.requireNonNull(pipelineDao, "pipelineDao");
        this.moduleSaver = Objects.requireNonNull(moduleSaver, "moduleSaver");
        this.serviceSaver = Objects.requireNonNull(serviceSaver, "serviceSaver");
        this.projectArchiver = Objects.requireNonNull(projectArchiver, "projectArchiver");
    }

    @Override
    public Path save(ProjectLoadResultDataObject project) throws ProjectSavingException {
        return save(project, Collections.<Path>emptyList(), Collections.<Path>emptyList());
    }

    @Override
    public Path save(
            ProjectLoadResultDataObject project,
            List<Path> moduleJars,
            List<Path> serviceJars
    ) throws ProjectSavingException {
        if (project == null) {
            throw new ProjectSavingException("Project must not be null");
        }
        return save(
                project.getProjectDirectory(),
                project.getGlobal(),
                project.getNodeContainer(),
                moduleJars,
                serviceJars
        );
    }

    @Override
    public Path save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer
    ) throws ProjectSavingException {
        return save(
                projectDirectory,
                global,
                nodeContainer,
                Collections.<Path>emptyList(),
                Collections.<Path>emptyList()
        );
    }

    @Override
    public Path save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer,
            List<Path> moduleJars,
            List<Path> serviceJars
    ) throws ProjectSavingException {
        Path normalizedProjectDirectory = normalizeProjectDirectory(projectDirectory);
        if (global == null) {
            throw new ProjectSavingException("Project global configuration must not be null");
        }
        if (nodeContainer == null) {
            throw new ProjectSavingException("Node container must not be null");
        }
        validateJarLists(moduleJars, serviceJars);

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
            saveModules(moduleJars, normalizedProjectDirectory);
            saveServices(serviceJars, normalizedProjectDirectory);
            return projectArchiver.archive(normalizedProjectDirectory);
        } catch (ProjectSavingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ProjectSavingException("Cannot save project", exception);
        }
    }

    private void validateJarLists(List<Path> moduleJars, List<Path> serviceJars)
            throws ProjectSavingException {
        if (moduleJars == null) {
            throw new ProjectSavingException("Module JAR list must not be null");
        }
        if (serviceJars == null) {
            throw new ProjectSavingException("Service JAR list must not be null");
        }
    }

    private void saveModules(List<Path> moduleJars, Path projectDirectory)
            throws ProjectSavingException {
        for (Path moduleJar : moduleJars) {
            try {
                moduleSaver.save(moduleJar, projectDirectory);
            } catch (PluginSavingException exception) {
                throw new ProjectSavingException(
                        "Cannot save module JAR: " + moduleJar,
                        exception
                );
            }
        }
    }

    private void saveServices(List<Path> serviceJars, Path projectDirectory)
            throws ProjectSavingException {
        for (Path serviceJar : serviceJars) {
            try {
                serviceSaver.save(serviceJar, projectDirectory);
            } catch (PluginSavingException exception) {
                throw new ProjectSavingException(
                        "Cannot save service JAR: " + serviceJar,
                        exception
                );
            }
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
