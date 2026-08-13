package io.github.byzatic.lib.configio.application.dao;

import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;

import java.nio.file.Path;

public interface ProjectDaoInterface {

    ProjectStructureDataObject load(Path projectDirectory) throws ProjectLoadingException;

    void save(Path projectDirectory, ProjectStructureDataObject projectStructure)
            throws ProjectSavingException;
}
