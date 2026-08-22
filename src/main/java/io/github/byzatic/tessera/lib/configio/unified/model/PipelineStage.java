package io.github.byzatic.tessera.lib.configio.unified.model;

import java.util.List;
import java.util.Objects;

/** Immutable pipeline stage with its order and workers. */
public final class PipelineStage {

    private final String id;
    private final int position;
    private final List<Worker> workers;

    private PipelineStage(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.position = builder.position;
        this.workers = List.copyOf(Objects.requireNonNull(builder.workers, "workers"));
        if (id.isBlank()) {
            throw new IllegalArgumentException("Pipeline stage id must not be blank");
        }
        if (workers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Workers must not contain null");
        }
    }

    /** Returns a new builder for PipelineStage. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the stage identifier. */
    public String getId() {
        return id;
    }

    /** Returns the stage position. */
    public int getPosition() {
        return position;
    }

    /** Returns the immutable worker declarations. */
    public List<Worker> getWorkers() {
        return workers;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PipelineStage)) {
            return false;
        }
        PipelineStage that = (PipelineStage) object;
        return Objects.equals(id, that.id)
                && position == that.position
                && Objects.equals(workers, that.workers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, workers);
    }

    @Override
    public String toString() {
        return "PipelineStage{" +
                "id=" + id
                 + ", position=" + position
                 + ", workers=" + workers +
                '}';
    }

    /** Fluent builder for immutable PipelineStage values. */
    public static final class Builder {

        private String id;
        private int position;
        private List<Worker> workers = List.of();

        private Builder() {
        }

        /** Sets the stage identifier. */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        /** Sets the stage position. */
        public Builder position(int value) {
            this.position = value;
            return this;
        }

        /** Sets the immutable worker declarations. */
        public Builder workers(List<Worker> value) {
            this.workers = List.copyOf(Objects.requireNonNull(value, "workers"));
            return this;
        }

        /** Builds and validates an immutable PipelineStage. */
        public PipelineStage build() {
            return new PipelineStage(this);
        }
    }
}

