package io.github.byzatic.lib.configio.application.revision;

import io.github.byzatic.lib.configio.domain.exception.ProjectRevisionException;

/**
 * Observes a project artifact and publishes stable, isolated project revisions.
 */
public interface ProjectRevisionSource extends AutoCloseable {

    /**
     * Starts observation and publishes the current archive followed by later changes.
     *
     * @param listener revision consumer
     * @throws ProjectRevisionException when observation cannot be started
     * @throws IllegalStateException when the source has already been started or closed
     */
    void start(ProjectRevisionListener listener) throws ProjectRevisionException;

    /**
     * Stops observation and releases source-owned resources.
     */
    @Override
    void close();
}
