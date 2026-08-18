package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.service.ServiceLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.SharedResourcesContainerDataObject;
import io.github.byzatic.lib.configio.infrastructure.loader.ServiceLoaderStrategy;

import java.nio.file.Path;

public final class ServiceLoaderFactory {

    private ServiceLoaderFactory() {
    }

    public static ServiceLoaderInterface create(
            Path pluginsDirectory,
            SharedResourcesContainerDataObject sharedResources
    ) throws PluginLoadingException {
        ClassLoader sharedResourcesClassLoader = null;
        if (sharedResources != null) {
            sharedResourcesClassLoader = sharedResources.getLastClassLoader();
        }
        return create(pluginsDirectory, sharedResourcesClassLoader);
    }

    public static ServiceLoaderInterface create(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        return new ServiceLoaderStrategy(
                pluginsDirectory,
                sharedResourcesClassLoader
        );
    }
}
