package io.github.byzatic.lib.configio.infrastructure.dto.node.pipeline;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class Pipeline{

	@SerializedName("stages_consistency")
	private List<StagesConsistencyItem> stagesConsistency;

	@SerializedName("stages_description")
	private List<StagesDescriptionItem> stagesDescription;

	public Pipeline() {
	}

	private Pipeline(Builder builder) {
		stagesConsistency = builder.stagesConsistency;
		stagesDescription = builder.stagesDescription;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(Pipeline copy) {
		Builder builder = new Builder();
		builder.stagesConsistency = copy.getStagesConsistency();
		builder.stagesDescription = copy.getStagesDescription();
		return builder;
	}

	public List<StagesConsistencyItem> getStagesConsistency(){
		return stagesConsistency;
	}

	public List<StagesDescriptionItem> getStagesDescription(){
		return stagesDescription;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Pipeline pipeline = (Pipeline) o;
		return Objects.equals(stagesConsistency, pipeline.stagesConsistency) && Objects.equals(stagesDescription, pipeline.stagesDescription);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stagesConsistency, stagesDescription);
	}

	@Override
	public String toString() {
		return "Pipeline{" +
				"stagesConsistency=" + stagesConsistency +
				", stagesDescription=" + stagesDescription +
				'}';
	}

	/**
	 * {@code Pipeline} builder static inner class.
	 */
	public static final class Builder {
		private List<StagesConsistencyItem> stagesConsistency;
		private List<StagesDescriptionItem> stagesDescription;

		private Builder() {
		}

		/**
		 * Sets the {@code stagesConsistency} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param stagesConsistency the {@code stagesConsistency} to set
		 * @return a reference to this Builder
		 */
		public Builder setStagesConsistency(List<StagesConsistencyItem> stagesConsistency) {
			this.stagesConsistency = stagesConsistency;
			return this;
		}

		/**
		 * Sets the {@code stagesDescription} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param stagesDescription the {@code stagesDescription} to set
		 * @return a reference to this Builder
		 */
		public Builder setStagesDescription(List<StagesDescriptionItem> stagesDescription) {
			this.stagesDescription = stagesDescription;
			return this;
		}

		/**
		 * Returns a {@code Pipeline} built from the parameters previously set.
		 *
		 * @return a {@code Pipeline} built with parameters of this {@code Pipeline.Builder}
		 */
		public Pipeline build() {
			return new Pipeline(this);
		}
	}
}