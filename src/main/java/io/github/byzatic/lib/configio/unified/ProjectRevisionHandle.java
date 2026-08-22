package io.github.byzatic.lib.configio.unified;

import io.github.byzatic.lib.configio.unified.model.TesseraProject;

import java.io.IOException;
import java.nio.file.Path;

/** Isolated loaded project revision transferred to a revision listener. */
public interface ProjectRevisionHandle extends AutoCloseable {

    /** Returns the SHA-256 revision identifier. */
    String getRevisionId();

    /** Returns the normalized source archive path. */
    Path getSourceArchive();

    /** Returns the isolated extracted project directory. */
    Path getProjectDirectory();

    /** Returns the detached immutable project aggregate. */
    TesseraProject getProject();

    /**
     * Opens or returns the project-scoped runtime session for this revision.
     *
     * @return runtime session owned by this revision handle
     * @throws TesseraProjectException when plugin resources cannot be loaded
     * @throws IllegalStateException when the revision is closed
     */
    ProjectRuntimeSession openRuntime() throws TesseraProjectException;

    /** Returns whether this revision has released its resources. */
    boolean isClosed();

    /**
     * Closes an opened runtime, releases shared resources, and removes staging files.
     *
     * @throws IOException when one or more resources cannot be released
     */
    @Override
    void close() throws IOException;
}
