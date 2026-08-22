package io.github.byzatic.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable input for saving a project directory and its companion ZIP archive. */
public final class SaveProjectRequest {

    private final Path projectDirectory;
    private final TesseraProject project;
    private final ProjectArtifacts artifacts;

    private SaveProjectRequest(Builder builder) {
        this.projectDirectory = Objects.requireNonNull(builder.projectDirectory, "projectDirectory")
                .toAbsolutePath()
                .normalize();
        this.project = Objects.requireNonNull(builder.project, "project");
        this.artifacts = Objects.requireNonNull(builder.artifacts, "artifacts");

    }

    /** Returns a new builder for SaveProjectRequest. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the normalized destination project directory. */
    public Path getProjectDirectory() {
        return projectDirectory;
    }

    /** Returns the project to save. */
    public TesseraProject getProject() {
        return project;
    }

    /** Returns the additional project artifacts. */
    public ProjectArtifacts getArtifacts() {
        return artifacts;
    }

    /** Creates a request without additional plugin or DSL artifacts. */
    public static SaveProjectRequest of(Path projectDirectory, TesseraProject project) {
        return newBuilder().projectDirectory(projectDirectory).project(project).build();
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SaveProjectRequest)) {
            return false;
        }
        SaveProjectRequest that = (SaveProjectRequest) object;
        return Objects.equals(projectDirectory, that.projectDirectory)
                && Objects.equals(project, that.project)
                && Objects.equals(artifacts, that.artifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectDirectory, project, artifacts);
    }

    @Override
    public String toString() {
        return "SaveProjectRequest{" +
                "projectDirectory=" + projectDirectory
                 + ", project=" + project
                 + ", artifacts=" + artifacts +
                '}';
    }

    /** Fluent builder for immutable SaveProjectRequest values. */
    public static final class Builder {

        private Path projectDirectory;
        private TesseraProject project;
        private ProjectArtifacts artifacts = ProjectArtifacts.empty();

        private Builder() {
        }

        /** Sets the normalized destination project directory. */
        public Builder projectDirectory(Path value) {
            this.projectDirectory = value;
            return this;
        }

        /** Sets the project to save. */
        public Builder project(TesseraProject value) {
            this.project = value;
            return this;
        }

        /** Sets the additional project artifacts. */
        public Builder artifacts(ProjectArtifacts value) {
            this.artifacts = value;
            return this;
        }

        /** Builds and validates an immutable SaveProjectRequest. */
        public SaveProjectRequest build() {
            return new SaveProjectRequest(this);
        }
    }
}

