package io.github.byzatic.lib.configio.application.saver;

import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;

import java.nio.file.Path;

public interface ProjectSaverInterface {

    void save(ProjectLoadResultDataObject project) throws ProjectSavingException;

    void save(
            Path projectDirectory,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer
    ) throws ProjectSavingException;
}
