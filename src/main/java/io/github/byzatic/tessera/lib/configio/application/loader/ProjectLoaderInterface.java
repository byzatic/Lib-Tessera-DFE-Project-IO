package io.github.byzatic.tessera.lib.configio.application.loader;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectLoadResultDataObject;

import java.nio.file.Path;

public interface ProjectLoaderInterface {

    ProjectLoadResultDataObject load(Path projectDirectory) throws ProjectLoadingException;
}
