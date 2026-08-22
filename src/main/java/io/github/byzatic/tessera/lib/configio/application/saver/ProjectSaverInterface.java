package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.DslFileDataObject;

import java.nio.file.Path;
import java.util.List;

public interface ProjectSaverInterface {

    Path save(ProjectLoadResultDataObject project) throws ProjectSavingException;

    Path save(
            ProjectLoadResultDataObject project,
            List<Path> moduleJars,
            List<Path> serviceJars
    ) throws ProjectSavingException;

    Path save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer
    ) throws ProjectSavingException;

    Path save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer,
            List<Path> moduleJars,
            List<Path> serviceJars
    ) throws ProjectSavingException;

    Path save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer,
            List<Path> moduleJars,
            List<Path> serviceJars,
            List<DslFileDataObject> dslFiles
    ) throws ProjectSavingException;
}
