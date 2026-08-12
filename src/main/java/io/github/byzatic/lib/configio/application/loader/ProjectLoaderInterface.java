package io.github.byzatic.lib.configio.application.loader;

import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;

import java.nio.file.Path;

public interface ProjectLoaderInterface {

    ProjectLoadResultDataObject load(Path projectDirectory) throws ProjectLoadingException;
}
