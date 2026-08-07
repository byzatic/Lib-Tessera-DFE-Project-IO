package io.github.byzatic.lib.configio.infrastructure.dto.raw.project_and_graph;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class GraphStructure {

	@SerializedName("name")
	private String name;

	@SerializedName("downstream")
	private List<GraphStructure> graphStructures;

	@SerializedName("description")
	private String description;

	@SerializedName("id")
	private String id;

	public GraphStructure() {
	}

	private GraphStructure(Builder builder) {
		name = builder.name;
		graphStructures = builder.graphStructures;
		description = builder.description;
		id = builder.id;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(GraphStructure copy) {
		Builder builder = new Builder();
		builder.name = copy.getName();
		builder.graphStructures = copy.getDownstream();
		builder.description = copy.getDescription();
		builder.id = copy.getId();
		return builder;
	}

	public String getName(){
		return name;
	}

	public List<GraphStructure> getDownstream(){
		return graphStructures;
	}

	public String getDescription(){
		return description;
	}

	public String getId(){
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		GraphStructure graphStructure = (GraphStructure) o;
		return Objects.equals(name, graphStructure.name) && Objects.equals(graphStructures, graphStructure.graphStructures) && Objects.equals(description, graphStructure.description) && Objects.equals(id, graphStructure.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, graphStructures, description, id);
	}

	@Override
	public String toString() {
		return "GraphStructure{" +
				"name='" + name + '\'' +
				", graphStructures=" + graphStructures +
				", description='" + description + '\'' +
				", id='" + id + '\'' +
				'}';
	}

	/**
	 * {@code GraphStructure} builder static inner class.
	 */
	public static final class Builder {
		private String name;
		private List<GraphStructure> graphStructures;
		private String description;
		private String id;

		private Builder() {
		}

		/**
		 * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param name the {@code name} to set
		 * @return a reference to this Builder
		 */
		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the {@code graphStructures} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param graphStructures the {@code graphStructures} to set
		 * @return a reference to this Builder
		 */
		public Builder setDownstream(List<GraphStructure> graphStructures) {
			this.graphStructures = graphStructures;
			return this;
		}

		/**
		 * Sets the {@code description} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param description the {@code description} to set
		 * @return a reference to this Builder
		 */
		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}

		/**
		 * Sets the {@code id} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param id the {@code id} to set
		 * @return a reference to this Builder
		 */
		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Returns a {@code GraphStructure} built from the parameters previously set.
		 *
		 * @return a {@code GraphStructure} built with parameters of this {@code GraphStructure.Builder}
		 */
		public GraphStructure build() {
			return new GraphStructure(this);
		}
	}
}