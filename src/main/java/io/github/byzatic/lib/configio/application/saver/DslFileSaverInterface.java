package io.github.byzatic.lib.configio.application.saver;

import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.DslFileDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;

import java.nio.file.Path;
import java.util.List;

public interface DslFileSaverInterface {

    void save(
            Path projectDirectory,
            NodeContainerDataObject nodeContainer,
            List<DslFileDataObject> dslFiles
    ) throws ProjectSavingException;
}
