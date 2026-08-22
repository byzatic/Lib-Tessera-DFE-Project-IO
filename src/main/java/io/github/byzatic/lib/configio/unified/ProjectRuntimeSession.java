package io.github.byzatic.lib.configio.unified;

import io.github.byzatic.lib.configio.unified.model.RoutineMetadata;
import io.github.byzatic.lib.configio.unified.model.TesseraProject;
import io.github.byzatic.tessera.service.service.ServiceInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Project-scoped runtime resources and plugin factories.
 *
 * <p>A session owns plugin class loaders and must be closed after the engine runtime stops.</p>
 */
public interface ProjectRuntimeSession extends AutoCloseable {

    /** Returns the immutable project loaded for this runtime. */
    TesseraProject getProject();

    /** Returns the normalized project directory backing this runtime. */
    Path getProjectDirectory();

    /** Returns immutable names of discovered workflow routines. */
    Set<String> getAvailableRoutineNames();

    /** Returns immutable names of discovered services. */
    Set<String> getAvailableServiceNames();

    /**
     * Returns detached editor metadata published by workflow-routine JARs.
     *
     * @return immutable metadata list, possibly empty
     * @throws TesseraProjectException when metadata discovery fails
     * @throws IllegalStateException when the session is closed
     */
    List<RoutineMetadata> getRoutineMetadata() throws TesseraProjectException;

    /**
     * Creates one workflow-routine instance from a discovered factory.
     *
     * @param request routine name and engine dependencies
     * @return created workflow-routine instance
     * @throws TesseraProjectException when the routine is missing or creation fails
     * @throws IllegalStateException when the session is closed
     */
    WorkflowRoutineInterface createRoutine(RoutineCreationRequest request)
            throws TesseraProjectException;

    /**
     * Creates one service instance from a discovered factory.
     *
     * @param request service name and engine dependencies
     * @return created service instance
     * @throws TesseraProjectException when the service is missing or creation fails
     * @throws IllegalStateException when the session is closed
     */
    ServiceInterface createService(ServiceCreationRequest request)
            throws TesseraProjectException;

    /** Returns whether this session has released its resources. */
    boolean isClosed();

    /**
     * Releases plugin class loaders and owned project resources.
     *
     * @throws TesseraProjectException when one or more resources cannot be released
     */
    @Override
    void close() throws TesseraProjectException;
}
