package io.github.byzatic.tessera.lib.configio.unified.internal;

import io.github.byzatic.tessera.lib.configio.application.revision.ProjectRevision;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionHandle;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRuntimeSession;
import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectException;
import io.github.byzatic.tessera.lib.configio.unified.model.TesseraProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

final class DefaultProjectRevisionHandle implements ProjectRevisionHandle {

    private final ProjectRevision revision;
    private final TesseraProject project;
    private ProjectRuntimeSession runtimeSession;

    DefaultProjectRevisionHandle(ProjectRevision revision, LegacyProjectMapper mapper) {
        this.revision = Objects.requireNonNull(revision, "revision");
        this.project = Objects.requireNonNull(mapper, "mapper").toUnified(revision.getProject());
    }

    @Override
    public String getRevisionId() {
        return revision.getRevisionId();
    }

    @Override
    public Path getSourceArchive() {
        return revision.getSourceArchive();
    }

    @Override
    public Path getProjectDirectory() {
        return revision.getProjectDirectory();
    }

    @Override
    public synchronized TesseraProject getProject() {
        ensureOpen();
        return project;
    }

    @Override
    public synchronized ProjectRuntimeSession openRuntime() throws TesseraProjectException {
        ensureOpen();
        if (runtimeSession != null && !runtimeSession.isClosed()) {
            return runtimeSession;
        }
        runtimeSession = DefaultProjectRuntimeSession.open(
                revision.getProject(),
                project,
                false
        );
        return runtimeSession;
    }

    @Override
    public boolean isClosed() {
        return revision.isClosed();
    }

    @Override
    public synchronized void close() throws IOException {
        if (revision.isClosed()) {
            return;
        }
        IOException failure = null;
        if (runtimeSession != null && !runtimeSession.isClosed()) {
            try {
                runtimeSession.close();
            } catch (TesseraProjectException exception) {
                failure = new IOException("Cannot close revision runtime", exception);
            }
        }
        try {
            revision.close();
        } catch (IOException exception) {
            if (failure == null) {
                failure = exception;
            } else {
                failure.addSuppressed(exception);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private void ensureOpen() {
        if (revision.isClosed()) {
            throw new IllegalStateException("Project revision is closed: " + getRevisionId());
        }
    }
}
