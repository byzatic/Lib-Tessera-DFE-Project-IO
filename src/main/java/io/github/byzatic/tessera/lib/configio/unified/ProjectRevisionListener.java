package io.github.byzatic.tessera.lib.configio.unified;

/** Receives ownership of available revisions and notifications about rejected archives. */
public interface ProjectRevisionListener {

    /**
     * Accepts ownership of a successfully prepared project revision.
     *
     * @param revision revision that must eventually be closed by the receiver
     */
    void onRevisionAvailable(ProjectRevisionHandle revision);

    /**
     * Reports an archive that could not be prepared as a project revision.
     *
     * @param error detached rejection details
     */
    void onRevisionRejected(ProjectRevisionError error);
}
