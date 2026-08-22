package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Immutable detached editor metadata discovered in a service JAR. */
public final class ServiceMetadata {

    private final String id;
    private final String displayName;
    private final String description;
    private final String version;
    private final Path artifact;
    private final List<ServiceParameterMetadata> parameters;

    private ServiceMetadata(Builder builder) {
        this.id = requireText(builder.id, "id");
        this.displayName = requireText(builder.displayName, "displayName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.version = Objects.requireNonNull(builder.version, "version");
        this.artifact = Objects.requireNonNull(builder.artifact, "artifact")
                .toAbsolutePath()
                .normalize();
        this.parameters = List.copyOf(Objects.requireNonNull(builder.parameters, "parameters"));
        if (parameters.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("parameters must not contain null");
        }
        if (parameters.stream().map(ServiceParameterMetadata::getId).distinct().count()
                != parameters.size()) {
            throw new IllegalArgumentException("parameter id must be unique within a service");
        }
    }

    /** Returns a new builder for ServiceMetadata. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the service identifier. */
    public String getId() {
        return id;
    }

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the service description. */
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

    /** Returns immutable editor metadata for service parameters. */
    public List<ServiceParameterMetadata> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceMetadata)) {
            return false;
        }
        ServiceMetadata that = (ServiceMetadata) object;
        return Objects.equals(id, that.id)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(version, that.version)
                && Objects.equals(artifact, that.artifact)
                && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, version, artifact, parameters);
    }

    @Override
    public String toString() {
        return "ServiceMetadata{" +
                "id=" + id
                 + ", displayName=" + displayName
                 + ", description=" + description
                 + ", version=" + version
                 + ", artifact=" + artifact
                 + ", parameters=" + parameters +
                '}';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /** Fluent builder for immutable ServiceMetadata values. */
    public static final class Builder {

        private String id;
        private String displayName;
        private String description = "";
        private String version = "";
        private Path artifact;
        private List<ServiceParameterMetadata> parameters = List.of();

        private Builder() {
        }

        /** Sets the service identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the display name. */
        public Builder displayName(String value) {
            this.displayName = value;
            return this;
        }

        /** Sets the service description. */
        public Builder description(String value) {
            this.description = value;
            return this;
        }

        /** Sets the artifact implementation version. */
        public Builder version(String value) {
            this.version = value;
            return this;
        }

        /** Sets the artifact path. */
        public Builder artifact(Path value) {
            this.artifact = value;
            return this;
        }

        /** Sets immutable service parameter metadata. */
        public Builder parameters(List<ServiceParameterMetadata> value) {
            this.parameters = List.copyOf(Objects.requireNonNull(value, "parameters"));
            return this;
        }

        /** Builds and validates immutable ServiceMetadata. */
        public ServiceMetadata build() {
            return new ServiceMetadata(this);
        }
    }
}
