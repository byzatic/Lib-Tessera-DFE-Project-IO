package io.github.byzatic.tessera.lib.configio.application.revision;

/**
 * Receives stable project revisions and preparation failures.
 */
public interface ProjectRevisionListener {

    /**
     * Accepts ownership of a prepared revision.
     *
     * @param revision prepared revision which must eventually be closed by the receiver
     */
    void onRevisionAvailable(ProjectRevision revision);

    /**
     * Reports a rejected archive while leaving the previously delivered revision unaffected.
     *
     * @param failure rejection details
     */
    void onRevisionRejected(ProjectRevisionFailure failure);
}
