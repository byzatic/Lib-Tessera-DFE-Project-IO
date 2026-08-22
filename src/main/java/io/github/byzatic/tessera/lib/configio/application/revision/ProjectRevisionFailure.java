package io.github.byzatic.tessera.lib.configio.application.revision;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Describes an archive revision rejected before it could be activated.
 */
public final class ProjectRevisionFailure {

    private final Path sourceArchive;
    private final String revisionId;
    private final Throwable cause;

    public ProjectRevisionFailure(Path sourceArchive, String revisionId, Throwable cause) {
        this.sourceArchive = Objects.requireNonNull(sourceArchive, "sourceArchive");
        this.revisionId = revisionId;
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    public Path getSourceArchive() {
        return sourceArchive;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public Throwable getCause() {
        return cause;
    }
}
