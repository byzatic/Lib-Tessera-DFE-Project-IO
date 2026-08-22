package io.github.byzatic.tessera.lib.configio.application.dao;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectStructureDataObject;

import java.nio.file.Path;
import java.util.Map;

public interface PipelineDaoInterface {

    Map<GraphNodeReferenceDataObject, PipelineDataObject> load(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure
    ) throws ProjectLoadingException;

    void save(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure,
            Map<GraphNodeReferenceDataObject, PipelineDataObject> pipelines
    ) throws ProjectSavingException;
}
