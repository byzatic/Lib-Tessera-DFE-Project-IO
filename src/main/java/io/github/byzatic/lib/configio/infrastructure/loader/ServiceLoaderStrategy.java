package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.service.ServiceLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.infrastructure.utils.ClassLoaderCloserUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.JarPluginLoaderUtility;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.ServiceFactoryInterface;
import io.github.byzatic.tessera.service.service.ServiceInterface;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ServiceLoaderStrategy implements ServiceLoaderInterface {

    private final Map<String, ServiceFactoryInterface> serviceFactories;
    private final List<URLClassLoader> classLoaders;
    private final ClassLoaderCloserUtility classLoaderCloser;
    private boolean closed;

    public ServiceLoaderStrategy(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        this(
                pluginsDirectory,
                sharedResourcesClassLoader,
                new JarPluginLoaderUtility(),
                new ClassLoaderCloserUtility()
        );
    }

    public ServiceLoaderStrategy(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader,
            JarPluginLoaderUtility pluginLoader,
            ClassLoaderCloserUtility classLoaderCloser
    ) throws PluginLoadingException {
        if (pluginLoader == null) {
            throw new IllegalArgumentException("pluginLoader must not be null");
        }
        if (classLoaderCloser == null) {
            throw new IllegalArgumentException("classLoaderCloser must not be null");
        }
        this.serviceFactories = new HashMap<String, ServiceFactoryInterface>();
        this.classLoaders = new ArrayList<URLClassLoader>();
        this.classLoaderCloser = classLoaderCloser;

        try {
            pluginLoader.loadFactories(
                    pluginsDirectory,
                    sharedResourcesClassLoader,
                    ServiceFactoryInterface.class,
                    "service",
                    serviceFactories,
                    classLoaders
            );
        } catch (PluginLoadingException exception) {
            classLoaderCloser.closeAfterFailure(classLoaders, exception);
            throw exception;
        } catch (RuntimeException exception) {
            classLoaderCloser.closeAfterFailure(classLoaders, exception);
            throw exception;
        }
    }

    @Override
    public synchronized ServiceInterface getService(
            String serviceClassName,
            MCg3ServiceApiInterface serviceApi,
            HealthFlagProxy healthFlagProxy
    ) throws PluginLoadingException {
        ensureOpen();
        try {
            ServiceFactoryInterface serviceFactory = serviceFactories.get(serviceClassName);
            if (serviceFactory == null) {
                throw new PluginLoadingException(
                        "Service with name " + serviceClassName + " was not found"
                );
            }
            return serviceFactory.create(serviceApi, healthFlagProxy);
        } catch (PluginLoadingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new PluginLoadingException(exception.getMessage(), exception);
        }
    }

    @Override
    public synchronized Set<String> getAvailableServiceNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(serviceFactories.keySet())
        );
    }

    @Override
    public synchronized void close() throws PluginLoadingException {
        if (closed) {
            return;
        }
        classLoaderCloser.close(classLoaders);
        closed = true;
    }

    private void ensureOpen() throws PluginLoadingException {
        if (closed) {
            throw new PluginLoadingException("Service loader is closed");
        }
    }
}
