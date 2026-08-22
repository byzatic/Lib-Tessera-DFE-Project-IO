package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable input for exporting a project directly to a ZIP archive. */
public final class ExportProjectRequest {

    private final Path archiveDestination;
    private final TesseraProject project;
    private final ProjectArtifacts artifacts;

    private ExportProjectRequest(Builder builder) {
        this.archiveDestination = Objects.requireNonNull(builder.archiveDestination, "archiveDestination")
                .toAbsolutePath()
                .normalize();
        this.project = Objects.requireNonNull(builder.project, "project");
        this.artifacts = Objects.requireNonNull(builder.artifacts, "artifacts");

    }

    /** Returns a new builder for ExportProjectRequest. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the normalized archive destination. */
    public Path getArchiveDestination() {
        return archiveDestination;
    }

    /** Returns the project to export. */
    public TesseraProject getProject() {
        return project;
    }

    /** Returns the additional project artifacts. */
    public ProjectArtifacts getArtifacts() {
        return artifacts;
    }

    /** Creates an export request without additional plugin or DSL artifacts. */
    public static ExportProjectRequest of(Path archiveDestination, TesseraProject project) {
        return newBuilder().archiveDestination(archiveDestination).project(project).build();
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ExportProjectRequest)) {
            return false;
        }
        ExportProjectRequest that = (ExportProjectRequest) object;
        return Objects.equals(archiveDestination, that.archiveDestination)
                && Objects.equals(project, that.project)
                && Objects.equals(artifacts, that.artifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archiveDestination, project, artifacts);
    }

    @Override
    public String toString() {
        return "ExportProjectRequest{" +
                "archiveDestination=" + archiveDestination
                 + ", project=" + project
                 + ", artifacts=" + artifacts +
                '}';
    }

    /** Fluent builder for immutable ExportProjectRequest values. */
    public static final class Builder {

        private Path archiveDestination;
        private TesseraProject project;
        private ProjectArtifacts artifacts = ProjectArtifacts.empty();

        private Builder() {
        }

        /** Sets the normalized archive destination. */
        public Builder archiveDestination(Path value) {
            this.archiveDestination = value;
            return this;
        }

        /** Sets the project to export. */
        public Builder project(TesseraProject value) {
            this.project = value;
            return this;
        }

        /** Sets the additional project artifacts. */
        public Builder artifacts(ProjectArtifacts value) {
            this.artifacts = value;
            return this;
        }

        /** Builds and validates an immutable ExportProjectRequest. */
        public ExportProjectRequest build() {
            return new ExportProjectRequest(this);
        }
    }
}

