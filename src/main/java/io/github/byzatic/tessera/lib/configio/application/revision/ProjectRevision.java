package io.github.byzatic.tessera.lib.configio.application.revision;

import io.github.byzatic.tessera.lib.configio.domain.model.ProjectLoadResultDataObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An immutable, loaded project snapshot backed by an isolated temporary directory.
 *
 * <p>The receiver owns the revision and must close it after every runtime using the
 * revision has stopped. Closing releases project class loaders and removes the temporary
 * directory.</p>
 */
public final class ProjectRevision implements AutoCloseable {

    private final String revisionId;
    private final Path sourceArchive;
    private final Path projectDirectory;
    private final Path revisionDirectory;
    private final ProjectLoadResultDataObject project;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ProjectRevision(
            String revisionId,
            Path sourceArchive,
            Path projectDirectory,
            Path revisionDirectory,
            ProjectLoadResultDataObject project
    ) {
        this.revisionId = Objects.requireNonNull(revisionId, "revisionId");
        this.sourceArchive = Objects.requireNonNull(sourceArchive, "sourceArchive");
        this.projectDirectory = Objects.requireNonNull(projectDirectory, "projectDirectory");
        this.revisionDirectory = Objects.requireNonNull(revisionDirectory, "revisionDirectory");
        this.project = Objects.requireNonNull(project, "project");
    }

    /**
     * Returns the SHA-256 identifier of the source archive.
     *
     * @return stable revision identifier
     */
    public String getRevisionId() {
        return revisionId;
    }

    /**
     * Returns the archive observed by the revision source.
     *
     * @return normalized source archive path
     */
    public Path getSourceArchive() {
        return sourceArchive;
    }

    /**
     * Returns the isolated directory containing the extracted project.
     *
     * @return project directory valid until this revision is closed
     */
    public Path getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Returns the loaded project snapshot.
     *
     * @return loaded project valid until this revision is closed
     * @throws IllegalStateException when the revision has already been closed
     */
    public ProjectLoadResultDataObject getProject() {
        if (closed.get()) {
            throw new IllegalStateException("Project revision is closed: " + revisionId);
        }
        return project;
    }

    /**
     * Returns whether this revision has released its resources.
     *
     * @return {@code true} after the first close call
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Closes project class loaders and recursively removes the isolated revision directory.
     *
     * @throws IOException when one or more resources cannot be released
     */
    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        IOException failure = null;
        try {
            project.close();
        } catch (IOException exception) {
            failure = exception;
        }

        try {
            deleteRecursively(revisionDirectory);
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

    private void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path visitedDirectory, IOException failure)
                    throws IOException {
                if (failure != null) {
                    throw failure;
                }
                Files.deleteIfExists(visitedDirectory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
