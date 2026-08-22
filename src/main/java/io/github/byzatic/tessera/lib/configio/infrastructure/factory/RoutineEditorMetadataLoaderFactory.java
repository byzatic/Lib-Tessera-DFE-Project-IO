package io.github.byzatic.tessera.lib.configio.infrastructure.factory;

import io.github.byzatic.tessera.lib.configio.application.module.RoutineEditorMetadataLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.SharedResourcesContainerDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.loader.RoutineEditorMetadataLoaderStrategy;

import java.nio.file.Path;

/** Creates the standard JAR-backed routine editor metadata loader. */
public final class RoutineEditorMetadataLoaderFactory {

    private RoutineEditorMetadataLoaderFactory() {
    }

    public static RoutineEditorMetadataLoaderInterface create(
            Path pluginsDirectory,
            SharedResourcesContainerDataObject sharedResources
    ) throws PluginLoadingException {
        ClassLoader sharedResourcesClassLoader = null;
        if (sharedResources != null) {
            sharedResourcesClassLoader = sharedResources.getLastClassLoader();
        }
        return create(pluginsDirectory, sharedResourcesClassLoader);
    }

    public static RoutineEditorMetadataLoaderInterface create(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        return new RoutineEditorMetadataLoaderStrategy(
                pluginsDirectory,
                sharedResourcesClassLoader
        );
    }
}
