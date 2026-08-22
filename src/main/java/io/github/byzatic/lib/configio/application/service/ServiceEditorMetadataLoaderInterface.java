package io.github.byzatic.lib.configio.application.service;

import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.ServiceEditorMetadataDataObject;

import java.util.List;
import java.util.Optional;

/** Discovers editor metadata published by service JARs. */
public interface ServiceEditorMetadataLoaderInterface extends AutoCloseable {

    List<ServiceEditorMetadataDataObject> getAvailableMetadata()
            throws PluginLoadingException;

    Optional<ServiceEditorMetadataDataObject> findMetadata(String serviceId)
            throws PluginLoadingException;

    @Override
    void close() throws PluginLoadingException;
}
