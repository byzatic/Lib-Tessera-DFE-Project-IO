package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Immutable detached editor metadata discovered in a workflow-routine JAR. */
public final class RoutineMetadata {

    private final String id;
    private final String displayName;
    private final String description;
    private final String version;
    private final Path artifact;
    private final List<RoutineFunction> functions;

    private RoutineMetadata(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.version = Objects.requireNonNull(builder.version, "version");
        this.artifact = Objects.requireNonNull(builder.artifact, "artifact")
                .toAbsolutePath()
                .normalize();
        this.functions = List.copyOf(Objects.requireNonNull(builder.functions, "functions"));

    }

    /** Returns a new builder for RoutineMetadata. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the routine identifier. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the routine description. */
    public String getDescription() {
        return description;
    }

    /** Returns the artifact implementation version. */
    public String getVersion() {
        return version;
    }

    /** Returns the normalized artifact path. */
    public Path getArtifact() {
        return artifact;
    }

    /** Returns the immutable routine functions. */
    public List<RoutineFunction> getFunctions() {
        return functions;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoutineMetadata)) {
            return false;
        }
        RoutineMetadata that = (RoutineMetadata) object;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(version, that.version)
                && Objects.equals(artifact, that.artifact)
                && Objects.equals(functions, that.functions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, version, artifact, functions);
    }

    @Override
    public String toString() {
        return "RoutineMetadata{" +
                "id=" + id
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", version=" + version
                 + ", artifact=" + artifact
                 + ", functions=" + functions +
                '}';
    }

    /** Fluent builder for immutable RoutineMetadata values. */
    public static final class Builder {

        private String id;
        private String displayName;
        private String description;
        private String version;
        private Path artifact;
        private List<RoutineFunction> functions = List.of();

        private Builder() {
        }

        /** Sets the routine identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the display name. */
        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        /** Sets the routine description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the artifact implementation version. */
        public Builder version(String value) {
            this.version = value;
            return this;
        }

        /** Sets the normalized artifact path. */
        public Builder artifact(Path value) {
            this.artifact = value;
            return this;
        }

        /** Sets the immutable routine functions. */
        public Builder functions(List<RoutineFunction> value) {
            this.functions = List.copyOf(Objects.requireNonNull(value, "functions"));
            return this;
        }

        /** Builds and validates an immutable RoutineMetadata. */
        public RoutineMetadata build() {
            return new RoutineMetadata(this);
        }
    }
}

