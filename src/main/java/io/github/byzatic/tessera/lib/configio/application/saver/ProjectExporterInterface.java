package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectExportDataObject;

import java.nio.file.Path;

public interface ProjectExporterInterface {

    Path export(ProjectExportDataObject project) throws ProjectSavingException;
}
