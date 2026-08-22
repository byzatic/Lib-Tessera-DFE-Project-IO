package io.github.byzatic.lib.configio.unified.internal;

import io.github.byzatic.lib.configio.application.module.ModuleLoaderInterface;
import io.github.byzatic.lib.configio.application.module.RoutineEditorMetadataLoaderInterface;
import io.github.byzatic.lib.configio.application.service.ServiceEditorMetadataLoaderInterface;
import io.github.byzatic.lib.configio.application.service.ServiceLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ModuleLoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.RoutineEditorMetadataLoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ServiceEditorMetadataLoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ServiceLoaderFactory;
import io.github.byzatic.lib.configio.unified.ProjectRuntimeSession;
import io.github.byzatic.lib.configio.unified.RoutineCreationRequest;
import io.github.byzatic.lib.configio.unified.ServiceCreationRequest;
import io.github.byzatic.lib.configio.unified.TesseraProjectException;
import io.github.byzatic.lib.configio.unified.TesseraProjectOperation;
import io.github.byzatic.lib.configio.unified.model.RoutineMetadata;
import io.github.byzatic.lib.configio.unified.model.ServiceMetadata;
import io.github.byzatic.lib.configio.unified.model.TesseraProject;
import io.github.byzatic.tessera.service.service.ServiceInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

final class DefaultProjectRuntimeSession implements ProjectRuntimeSession {

    private final ProjectLoadResultDataObject loadedProject;
    private final TesseraProject project;
    private final boolean ownsLoadedProject;
    private final ModuleLoaderInterface moduleLoader;
    private final ServiceLoaderInterface serviceLoader;
    private final RoutineEditorMetadataLoaderInterface routineMetadataLoader;
    private final ServiceEditorMetadataLoaderInterface serviceMetadataLoader;
    private boolean closed;

    private DefaultProjectRuntimeSession(
            ProjectLoadResultDataObject loadedProject,
            TesseraProject project,
            boolean ownsLoadedProject,
            ModuleLoaderInterface moduleLoader,
            ServiceLoaderInterface serviceLoader,
            RoutineEditorMetadataLoaderInterface routineMetadataLoader,
            ServiceEditorMetadataLoaderInterface serviceMetadataLoader
    ) {
        this.loadedProject = loadedProject;
        this.project = project;
        this.ownsLoadedProject = ownsLoadedProject;
        this.moduleLoader = moduleLoader;
        this.serviceLoader = serviceLoader;
        this.routineMetadataLoader = routineMetadataLoader;
        this.serviceMetadataLoader = serviceMetadataLoader;
    }

    static DefaultProjectRuntimeSession open(
            ProjectLoadResultDataObject loadedProject,
            TesseraProject project,
            boolean ownsLoadedProject
    ) throws TesseraProjectException {
        Path projectDirectory = loadedProject.getProjectDirectory();
        Path modulesDirectory = projectDirectory.resolve("modules").resolve("workflow_routines");
        Path servicesDirectory = projectDirectory.resolve("modules").resolve("services");
        ModuleLoaderInterface moduleLoader = null;
        ServiceLoaderInterface serviceLoader = null;
        RoutineEditorMetadataLoaderInterface routineMetadataLoader = null;
        ServiceEditorMetadataLoaderInterface serviceMetadataLoader = null;
        try {
            moduleLoader = ModuleLoaderFactory.create(
                    modulesDirectory,
                    loadedProject.getSharedResourcesContainer()
            );
            serviceLoader = ServiceLoaderFactory.create(
                    servicesDirectory,
                    loadedProject.getSharedResourcesContainer()
            );
            routineMetadataLoader = RoutineEditorMetadataLoaderFactory.create(
                    modulesDirectory,
                    loadedProject.getSharedResourcesContainer()
            );
            serviceMetadataLoader = ServiceEditorMetadataLoaderFactory.create(
                    servicesDirectory,
                    loadedProject.getSharedResourcesContainer()
            );
            return new DefaultProjectRuntimeSession(
                    loadedProject,
                    project,
                    ownsLoadedProject,
                    moduleLoader,
                    serviceLoader,
                    routineMetadataLoader,
                    serviceMetadataLoader
            );
        } catch (Exception failure) {
            closeAfterFailure(
                    moduleLoader,
                    serviceLoader,
                    routineMetadataLoader,
                    serviceMetadataLoader,
                    ownsLoadedProject ? loadedProject : null,
                    failure
            );
            throw failure(
                    TesseraProjectOperation.OPEN_RUNTIME,
                    projectDirectory,
                    "Cannot open project runtime: " + projectDirectory,
                    failure
            );
        }
    }

    @Override
    public synchronized TesseraProject getProject() {
        ensureOpen();
        return project;
    }

    @Override
    public synchronized Path getProjectDirectory() {
        ensureOpen();
        return loadedProject.getProjectDirectory();
    }

    @Override
    public synchronized Set<String> getAvailableRoutineNames() {
        ensureOpen();
        return moduleLoader.getAvailableModuleNames();
    }

    @Override
    public synchronized Set<String> getAvailableServiceNames() {
        ensureOpen();
        return serviceLoader.getAvailableServiceNames();
    }

    @Override
    public synchronized List<RoutineMetadata> getRoutineMetadata()
            throws TesseraProjectException {
        ensureOpen();
        try {
            return new LegacyProjectMapper().toRoutineMetadata(
                    routineMetadataLoader.getAvailableMetadata()
            );
        } catch (PluginLoadingException exception) {
            throw failure(
                    TesseraProjectOperation.USE_RUNTIME,
                    loadedProject.getProjectDirectory(),
                    "Cannot read routine metadata",
                    exception
            );
        }
    }

    @Override
    public synchronized List<ServiceMetadata> getServiceMetadata()
            throws TesseraProjectException {
        ensureOpen();
        try {
            return new LegacyProjectMapper().toServiceMetadata(
                    serviceMetadataLoader.getAvailableMetadata()
            );
        } catch (PluginLoadingException exception) {
            throw failure(
                    TesseraProjectOperation.USE_RUNTIME,
                    loadedProject.getProjectDirectory(),
                    "Cannot read service metadata",
                    exception
            );
        }
    }

    @Override
    public synchronized WorkflowRoutineInterface createRoutine(RoutineCreationRequest request)
            throws TesseraProjectException {
        ensureOpen();
        if (request == null) {
            throw new NullPointerException("request");
        }
        try {
            return moduleLoader.getModule(
                    request.getRoutineName(),
                    request.getApi(),
                    request.getHealth()
            );
        } catch (PluginLoadingException exception) {
            throw failure(
                    TesseraProjectOperation.USE_RUNTIME,
                    loadedProject.getProjectDirectory(),
                    "Cannot create routine: " + request.getRoutineName(),
                    exception
            );
        }
    }

    @Override
    public synchronized ServiceInterface createService(ServiceCreationRequest request)
            throws TesseraProjectException {
        ensureOpen();
        if (request == null) {
            throw new NullPointerException("request");
        }
        try {
            return serviceLoader.getService(
                    request.getServiceName(),
                    request.getApi(),
                    request.getHealth()
            );
        } catch (PluginLoadingException exception) {
            throw failure(
                    TesseraProjectOperation.USE_RUNTIME,
                    loadedProject.getProjectDirectory(),
                    "Cannot create service: " + request.getServiceName(),
                    exception
            );
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() throws TesseraProjectException {
        if (closed) {
            return;
        }
        closed = true;
        Exception failure = null;
        failure = close(moduleLoader, failure);
        failure = close(serviceLoader, failure);
        failure = close(routineMetadataLoader, failure);
        failure = close(serviceMetadataLoader, failure);
        if (ownsLoadedProject) {
            try {
                loadedProject.close();
            } catch (IOException exception) {
                failure = addFailure(failure, exception);
            }
        }
        if (failure != null) {
            throw failure(
                    TesseraProjectOperation.CLOSE_RUNTIME,
                    loadedProject.getProjectDirectory(),
                    "Cannot close project runtime",
                    failure
            );
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Project runtime session is closed");
        }
    }

    private static Exception close(AutoCloseable resource, Exception failure) {
        try {
            resource.close();
            return failure;
        } catch (Exception exception) {
            return addFailure(failure, exception);
        }
    }

    private static Exception addFailure(Exception failure, Exception exception) {
        if (failure == null) {
            return exception;
        }
        failure.addSuppressed(exception);
        return failure;
    }

    private static void closeAfterFailure(
            ModuleLoaderInterface moduleLoader,
            ServiceLoaderInterface serviceLoader,
            RoutineEditorMetadataLoaderInterface routineMetadataLoader,
            ServiceEditorMetadataLoaderInterface serviceMetadataLoader,
            ProjectLoadResultDataObject loadedProject,
            Throwable failure
    ) {
        closeAfterFailure(serviceMetadataLoader, failure);
        closeAfterFailure(routineMetadataLoader, failure);
        closeAfterFailure(serviceLoader, failure);
        closeAfterFailure(moduleLoader, failure);
        closeAfterFailure(loadedProject, failure);
    }

    private static void closeAfterFailure(AutoCloseable resource, Throwable failure) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception exception) {
            failure.addSuppressed(exception);
        }
    }

    private static TesseraProjectException failure(
            TesseraProjectOperation operation,
            Path location,
            String message,
            Throwable cause
    ) {
        return new TesseraProjectException(operation, location, message, cause);
    }
}
