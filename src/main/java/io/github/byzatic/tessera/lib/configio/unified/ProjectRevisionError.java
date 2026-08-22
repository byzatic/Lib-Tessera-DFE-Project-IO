package io.github.byzatic.tessera.lib.configio.unified;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable details of a project archive rejected by a revision watcher. */
public final class ProjectRevisionError {

    private final Path sourceArchive;
    private final String revisionId;
    private final Throwable cause;

    private ProjectRevisionError(Builder builder) {
        this.sourceArchive = Objects.requireNonNull(builder.sourceArchive, "sourceArchive")
                .toAbsolutePath()
                .normalize();
        this.revisionId = builder.revisionId;
        this.cause = Objects.requireNonNull(builder.cause, "cause");

    }

    /** Returns a new builder for ProjectRevisionError. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the normalized source archive path. */
    public Path getSourceArchive() {
        return sourceArchive;
    }

    /** Returns the revision identifier, or null when unavailable. */
    public String getRevisionId() {
        return revisionId;
    }

    /** Returns the rejection cause. */
    public Throwable getCause() {
        return cause;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProjectRevisionError)) {
            return false;
        }
        ProjectRevisionError that = (ProjectRevisionError) object;
        return Objects.equals(sourceArchive, that.sourceArchive)
                && Objects.equals(revisionId, that.revisionId)
                && Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceArchive, revisionId, cause);
    }

    @Override
    public String toString() {
        return "ProjectRevisionError{" +
                "sourceArchive=" + sourceArchive
                 + ", revisionId=" + revisionId
                 + ", cause=" + cause +
                '}';
    }

    /** Fluent builder for immutable ProjectRevisionError values. */
    public static final class Builder {

        private Path sourceArchive;
        private String revisionId;
        private Throwable cause;

        private Builder() {
        }

        /** Sets the normalized source archive path. */
        public Builder sourceArchive(Path value) {
            this.sourceArchive = value;
            return this;
        }

        /** Sets the revision identifier, or null when unavailable. */
        public Builder revisionId(String value) {
            this.revisionId = value;
            return this;
        }

        /** Sets the rejection cause. */
        public Builder cause(Throwable value) {
            this.cause = value;
            return this;
        }

        /** Builds and validates an immutable ProjectRevisionError. */
        public ProjectRevisionError build() {
            return new ProjectRevisionError(this);
        }
    }
}

