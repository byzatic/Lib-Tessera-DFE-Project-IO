package io.github.byzatic.lib.configio.domain.model;

import io.github.byzatic.lib.configio.routine_spi.RoutineEditorDescriptor;

import java.nio.file.Path;
import java.util.Objects;

/** Editor metadata discovered in one workflow-routine JAR. */
public final class RoutineEditorMetadataDataObject {

    private final Path artifact;
    private final String version;
    private final RoutineEditorDescriptor descriptor;

    public RoutineEditorMetadataDataObject(
            Path artifact,
            String version,
            RoutineEditorDescriptor descriptor
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

    public String getRoutineId() {
        return descriptor.getRoutineId();
    }

    public RoutineEditorDescriptor getDescriptor() {
        return descriptor;
    }
}
