package io.github.byzatic.tessera.lib.configio.infrastructure.factory;

import io.github.byzatic.tessera.lib.configio.application.module.ModuleLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.SharedResourcesContainerDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.loader.ModuleLoaderStrategy;

import java.nio.file.Path;

public final class ModuleLoaderFactory {

    private ModuleLoaderFactory() {
    }

    public static ModuleLoaderInterface create(
            Path pluginsDirectory,
            SharedResourcesContainerDataObject sharedResources
    ) throws PluginLoadingException {
        ClassLoader sharedResourcesClassLoader = null;
        if (sharedResources != null) {
            sharedResourcesClassLoader = sharedResources.getLastClassLoader();
        }
        return create(pluginsDirectory, sharedResourcesClassLoader);
    }

    public static ModuleLoaderInterface create(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        return new ModuleLoaderStrategy(
                pluginsDirectory,
                sharedResourcesClassLoader
        );
    }
}
