package io.github.byzatic.lib.configio.domain.model;

import io.github.byzatic.lib.configio.service_spi.ServiceEditorDescriptor;

import java.nio.file.Path;
import java.util.Objects;

/** Editor metadata discovered in one service JAR. */
public final class ServiceEditorMetadataDataObject {

    private final Path artifact;
    private final String version;
    private final ServiceEditorDescriptor descriptor;

    public ServiceEditorMetadataDataObject(
            Path artifact,
            String version,
            ServiceEditorDescriptor descriptor
    ) {
        this.artifact = Objects.requireNonNull(artifact, "artifact")
                .toAbsolutePath()
                .normalize();
        this.version = Objects.requireNonNull(version, "version");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    public Path getArtifact() {
        return artifact;
    }

    public String getArtifactFileName() {
        return artifact.getFileName().toString();
    }

    public String getVersion() {
        return version;
    }

    public String getServiceId() {
        return descriptor.getServiceId();
    }

    public ServiceEditorDescriptor getDescriptor() {
        return descriptor;
    }
}
