package io.github.byzatic.tessera.lib.configio.unified.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Immutable plugin artifacts and DSL sources stored with a project. */
public final class ProjectArtifacts {

    private final List<Path> routineJars;
    private final List<Path> serviceJars;
    private final List<DslSource> dslSources;

    private ProjectArtifacts(Builder builder) {
        this.routineJars = copyPaths(builder.routineJars, "routineJars");
        this.serviceJars = copyPaths(builder.serviceJars, "serviceJars");
        this.dslSources = List.copyOf(Objects.requireNonNull(builder.dslSources, "dslSources"));
        if (dslSources.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("DSL sources must not contain null");
        }
    }

    /** Returns a new builder for ProjectArtifacts. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the normalized workflow-routine JAR paths. */
    public List<Path> getRoutineJars() {
        return routineJars;
    }

    /** Returns the normalized service JAR paths. */
    public List<Path> getServiceJars() {
        return serviceJars;
    }

    /** Returns the immutable DSL sources. */
    public List<DslSource> getDslSources() {
        return dslSources;
    }

    /** Returns an artifact set with no JAR or DSL additions. */
    public static ProjectArtifacts empty() {
        return newBuilder().build();
    }

    private static List<Path> copyPaths(List<Path> source, String name) {
        List<Path> paths = List.copyOf(Objects.requireNonNull(source, name));
        if (paths.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(name + " must not contain null");
        }
        return paths.stream().map(path -> path.toAbsolutePath().normalize()).toList();
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProjectArtifacts)) {
            return false;
        }
        ProjectArtifacts that = (ProjectArtifacts) object;
        return Objects.equals(routineJars, that.routineJars)
                && Objects.equals(serviceJars, that.serviceJars)
                && Objects.equals(dslSources, that.dslSources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineJars, serviceJars, dslSources);
    }

    @Override
    public String toString() {
        return "ProjectArtifacts{" +
                "routineJars=" + routineJars
                 + ", serviceJars=" + serviceJars
                 + ", dslSources=" + dslSources +
                '}';
    }

    /** Fluent builder for immutable ProjectArtifacts values. */
    public static final class Builder {

        private List<Path> routineJars = List.of();
        private List<Path> serviceJars = List.of();
        private List<DslSource> dslSources = List.of();

        private Builder() {
        }

        /** Sets the normalized workflow-routine JAR paths. */
        public Builder routineJars(List<Path> value) {
            this.routineJars = List.copyOf(Objects.requireNonNull(value, "routineJars"));
            return this;
        }

        /** Sets the normalized service JAR paths. */
        public Builder serviceJars(List<Path> value) {
            this.serviceJars = List.copyOf(Objects.requireNonNull(value, "serviceJars"));
            return this;
        }

        /** Sets the immutable DSL sources. */
        public Builder dslSources(List<DslSource> value) {
            this.dslSources = List.copyOf(Objects.requireNonNull(value, "dslSources"));
            return this;
        }

        /** Builds and validates an immutable ProjectArtifacts. */
        public ProjectArtifacts build() {
            return new ProjectArtifacts(this);
        }
    }
}

