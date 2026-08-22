package io.github.byzatic.tessera.lib.configio.application.dao;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.SharedResourcesContainerDataObject;

import java.nio.file.Path;

public interface SharedResourcesDaoInterface {

    SharedResourcesContainerDataObject load(Path projectDirectory)
            throws ProjectLoadingException;
}
