package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;

import java.nio.file.Path;

public interface ProjectArchiverInterface {

    Path archive(Path projectDirectory) throws ProjectSavingException;
}
