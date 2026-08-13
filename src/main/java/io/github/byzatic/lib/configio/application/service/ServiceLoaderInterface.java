package io.github.byzatic.lib.configio.application.service;

import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.ServiceInterface;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;

import java.util.Set;

public interface ServiceLoaderInterface extends AutoCloseable {

    ServiceInterface getService(
            String serviceClassName,
            MCg3ServiceApiInterface serviceApi,
            HealthFlagProxy healthFlagProxy
    ) throws PluginLoadingException;

    Set<String> getAvailableServiceNames();

    @Override
    void close() throws PluginLoadingException;
}
