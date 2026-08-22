package io.github.byzatic.tessera.lib.configio.infrastructure.factory;

import io.github.byzatic.tessera.lib.configio.application.service.ServiceEditorMetadataLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.SharedResourcesContainerDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.loader.ServiceEditorMetadataLoaderStrategy;

import java.nio.file.Path;

/** Creates the standard JAR-backed service editor metadata loader. */
public final class ServiceEditorMetadataLoaderFactory {

    private ServiceEditorMetadataLoaderFactory() {
    }

    public static ServiceEditorMetadataLoaderInterface create(
            Path pluginsDirectory,
            SharedResourcesContainerDataObject sharedResources
    ) throws PluginLoadingException {
        ClassLoader sharedResourcesClassLoader = null;
        if (sharedResources != null) {
            sharedResourcesClassLoader = sharedResources.getLastClassLoader();
        }
        return create(pluginsDirectory, sharedResourcesClassLoader);
    }

    public static ServiceEditorMetadataLoaderInterface create(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        return new ServiceEditorMetadataLoaderStrategy(
                pluginsDirectory,
                sharedResourcesClassLoader
        );
    }
}
