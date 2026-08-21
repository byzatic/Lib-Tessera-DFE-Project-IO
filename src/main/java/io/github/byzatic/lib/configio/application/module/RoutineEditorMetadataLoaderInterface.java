package io.github.byzatic.lib.configio.application.module;

import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.RoutineEditorMetadataDataObject;

import java.util.List;
import java.util.Optional;

/** Discovers editor metadata published by workflow-routine JARs. */
public interface RoutineEditorMetadataLoaderInterface extends AutoCloseable {

    List<RoutineEditorMetadataDataObject> getAvailableMetadata()
            throws PluginLoadingException;

    Optional<RoutineEditorMetadataDataObject> findMetadata(String routineId)
            throws PluginLoadingException;

    @Override
    void close() throws PluginLoadingException;
}
