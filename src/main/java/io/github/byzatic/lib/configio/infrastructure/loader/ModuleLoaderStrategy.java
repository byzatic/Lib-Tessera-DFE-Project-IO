package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.module.ModuleLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.infrastructure.utils.ClassLoaderCloserUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.JarPluginLoaderUtility;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineFactoryInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModuleLoaderStrategy implements ModuleLoaderInterface {

    private final Map<String, WorkflowRoutineFactoryInterface> moduleFactories;
    private final List<URLClassLoader> classLoaders;
    private final ClassLoaderCloserUtility classLoaderCloser;
    private boolean closed;

    public ModuleLoaderStrategy(
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

    public ModuleLoaderStrategy(
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
        this.moduleFactories = new HashMap<String, WorkflowRoutineFactoryInterface>();
        this.classLoaders = new ArrayList<URLClassLoader>();
        this.classLoaderCloser = classLoaderCloser;

        try {
            pluginLoader.loadFactories(
                    pluginsDirectory,
                    sharedResourcesClassLoader,
                    WorkflowRoutineFactoryInterface.class,
                    "module",
                    moduleFactories,
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
    public synchronized WorkflowRoutineInterface getModule(
            String workflowRoutineClassName,
            MCg3WorkflowRoutineApiInterface workflowRoutineApi,
            HealthFlagProxy healthFlagProxy
    ) throws PluginLoadingException {
        ensureOpen();
        try {
            WorkflowRoutineFactoryInterface moduleFactory =
                    moduleFactories.get(workflowRoutineClassName);
            if (moduleFactory == null) {
                throw new PluginLoadingException(
                        "Module with name " + workflowRoutineClassName + " was not found"
                );
            }
            return moduleFactory.create(workflowRoutineApi, healthFlagProxy);
        } catch (PluginLoadingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new PluginLoadingException(exception.getMessage(), exception);
        }
    }

    @Override
    public synchronized Set<String> getAvailableModuleNames() {
        return Collections.unmodifiableSet(
                new HashSet<String>(moduleFactories.keySet())
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
            throw new PluginLoadingException("Module loader is closed");
        }
    }
}
