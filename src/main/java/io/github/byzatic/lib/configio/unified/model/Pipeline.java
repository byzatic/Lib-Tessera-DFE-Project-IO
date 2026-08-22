package io.github.byzatic.lib.configio.unified.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Ordered and internally consistent immutable pipeline description. */
public final class Pipeline {

    private final List<PipelineStage> stages;

    private Pipeline(Builder builder) {
        this.stages = List.copyOf(Objects.requireNonNull(builder.stages, "stages"));
        if (stages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Pipeline stages must not contain null");
        }
        Set<String> ids = new HashSet<String>();
        Set<Integer> positions = new HashSet<Integer>();
        for (PipelineStage stage : stages) {
            if (!ids.add(stage.getId())) {
                throw new IllegalArgumentException("Duplicate pipeline stage id: " + stage.getId());
            }
            if (!positions.add(stage.getPosition())) {
                throw new IllegalArgumentException(
                        "Duplicate pipeline stage position: " + stage.getPosition()
                );
            }
        }
    }

    /** Returns a new builder for Pipeline. */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Returns the immutable ordered pipeline stages. */
    public List<PipelineStage> getStages() {
        return stages;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Pipeline)) {
            return false;
        }
        Pipeline that = (Pipeline) object;
        return Objects.equals(stages, that.stages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stages);
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "stages=" + stages +
                '}';
    }

    /** Fluent builder for immutable Pipeline values. */
    public static final class Builder {

        private List<PipelineStage> stages = List.of();

        private Builder() {
        }

        /** Sets the immutable ordered pipeline stages. */
        public Builder stages(List<PipelineStage> value) {
            this.stages = List.copyOf(Objects.requireNonNull(value, "stages"));
            return this;
        }

        /** Builds and validates an immutable Pipeline. */
        public Pipeline build() {
            return new Pipeline(this);
        }
    }
}

