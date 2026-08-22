package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable locations produced by a successful project save. */
public final class SaveProjectResult {

    private final Path projectDirectory;
    private final Path archive;

    private SaveProjectResult(Builder builder) {
        this.projectDirectory = Objects.requireNonNull(builder.projectDirectory, "projectDirectory")
                .toAbsolutePath()
                .normalize();
        this.archive = Objects.requireNonNull(builder.archive, "archive")
                .toAbsolutePath()
                .normalize();

    }

    /** Returns a new builder for SaveProjectResult. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the normalized saved project directory. */
    public Path getProjectDirectory() {
        return projectDirectory;
    }

    /** Returns the normalized generated archive. */
    public Path getArchive() {
        return archive;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SaveProjectResult)) {
            return false;
        }
        SaveProjectResult that = (SaveProjectResult) object;
        return Objects.equals(projectDirectory, that.projectDirectory)
                && Objects.equals(archive, that.archive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectDirectory, archive);
    }

    @Override
    public String toString() {
        return "SaveProjectResult{" +
                "projectDirectory=" + projectDirectory
                 + ", archive=" + archive +
                '}';
    }

    /** Fluent builder for immutable SaveProjectResult values. */
    public static final class Builder {

        private Path projectDirectory;
        private Path archive;

        private Builder() {
        }

        /** Sets the normalized saved project directory. */
        public Builder projectDirectory(Path value) {
            this.projectDirectory = value;
            return this;
        }

        /** Sets the normalized generated archive. */
        public Builder archive(Path value) {
            this.archive = value;
            return this;
        }

        /** Builds and validates an immutable SaveProjectResult. */
        public SaveProjectResult build() {
            return new SaveProjectResult(this);
        }
    }
}

