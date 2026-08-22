package io.github.byzatic.tessera.lib.configio.application.dao;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectGlobalDataObject;

import java.nio.file.Path;

public interface ProjectGlobalDaoInterface {

    ProjectGlobalDataObject load(Path projectDirectory) throws ProjectLoadingException;

    void save(Path projectDirectory, ProjectGlobalDataObject global)
            throws ProjectSavingException;
}
