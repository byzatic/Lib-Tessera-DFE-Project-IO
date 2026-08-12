package io.github.byzatic.lib.configio.application.dao;

import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;

import java.nio.file.Path;
import java.util.Map;

public interface NodeGlobalDaoInterface {

    Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> load(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure
    ) throws ProjectLoadingException;
}
