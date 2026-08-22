package io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.project_and_graph;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Project{

	@SerializedName("project_config_version")
	private String projectConfigVersion;

	@SerializedName("project_name")
	private String projectName;

	@SerializedName("structure")
	private GraphStructure graphStructure;

	public Project() {
	}

	private Project(Builder builder) {
		projectConfigVersion = builder.projectConfigVersion;
		projectName = builder.projectName;
		graphStructure = builder.graphStructure;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(Project copy) {
		Builder builder = new Builder();
		builder.projectConfigVersion = copy.getProjectConfigVersion();
		builder.projectName = copy.getProjectName();
		builder.graphStructure = copy.getStructure();
		return builder;
	}

	public String getProjectConfigVersion(){
		return projectConfigVersion;
	}

	public String getProjectName(){
		return projectName;
	}

	public GraphStructure getStructure(){
		return graphStructure;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Project project = (Project) o;
		return Objects.equals(projectConfigVersion, project.projectConfigVersion) && Objects.equals(projectName, project.projectName) && Objects.equals(graphStructure, project.graphStructure);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectConfigVersion, projectName, graphStructure);
	}

	@Override
	public String toString() {
		return "Project{" +
				"projectConfigVersion='" + projectConfigVersion + '\'' +
				", projectName='" + projectName + '\'' +
				", graphStructure=" + graphStructure +
				'}';
	}

	/**
	 * {@code Project} builder static inner class.
	 */
	public static final class Builder {
		private String projectConfigVersion;
		private String projectName;
		private GraphStructure graphStructure;

		private Builder() {
		}

		/**
		 * Sets the {@code projectConfigVersion} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param projectConfigVersion the {@code projectConfigVersion} to set
		 * @return a reference to this Builder
		 */
		public Builder setProjectConfigVersion(String projectConfigVersion) {
			this.projectConfigVersion = projectConfigVersion;
			return this;
		}

		/**
		 * Sets the {@code projectName} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param projectName the {@code projectName} to set
		 * @return a reference to this Builder
		 */
		public Builder setProjectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		/**
		 * Sets the {@code graphStructure} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param graphStructure the {@code graphStructure} to set
		 * @return a reference to this Builder
		 */
		public Builder setStructure(GraphStructure graphStructure) {
			this.graphStructure = graphStructure;
			return this;
		}

		/**
		 * Returns a {@code Project} built from the parameters previously set.
		 *
		 * @return a {@code Project} built with parameters of this {@code Project.Builder}
		 */
		public Project build() {
			return new Project(this);
		}
	}
}