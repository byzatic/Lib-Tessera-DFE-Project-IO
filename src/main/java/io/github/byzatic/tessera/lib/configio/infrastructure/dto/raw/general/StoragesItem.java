package io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.general;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class StoragesItem{

	@SerializedName("options")
	private List<OptionsItem> options;

	@SerializedName("description")
	private String description;

	@SerializedName("id_name")
	private String idName;

	public StoragesItem() {
	}

	private StoragesItem(Builder builder) {
		options = builder.options;
		description = builder.description;
		idName = builder.idName;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(StoragesItem copy) {
		Builder builder = new Builder();
		builder.options = copy.getOptions();
		builder.description = copy.getDescription();
		builder.idName = copy.getIdName();
		return builder;
	}

	public List<OptionsItem> getOptions(){
		return options;
	}

	public String getDescription(){
		return description;
	}

	public String getIdName(){
		return idName;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		StoragesItem that = (StoragesItem) o;
		return Objects.equals(options, that.options) && Objects.equals(description, that.description) && Objects.equals(idName, that.idName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(options, description, idName);
	}

	@Override
	public String toString() {
		return "StoragesItem{" +
				"options=" + options +
				", description='" + description + '\'' +
				", idName='" + idName + '\'' +
				'}';
	}

	/**
	 * {@code StoragesItem} builder static inner class.
	 */
	public static final class Builder {
		private List<OptionsItem> options;
		private String description;
		private String idName;

		private Builder() {
		}

		/**
		 * Sets the {@code options} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param options the {@code options} to set
		 * @return a reference to this Builder
		 */
		public Builder setOptions(List<OptionsItem> options) {
			this.options = options;
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
		 * Sets the {@code idName} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param idName the {@code idName} to set
		 * @return a reference to this Builder
		 */
		public Builder setIdName(String idName) {
			this.idName = idName;
			return this;
		}

		/**
		 * Returns a {@code StoragesItem} built from the parameters previously set.
		 *
		 * @return a {@code StoragesItem} built with parameters of this {@code StoragesItem.Builder}
		 */
		public StoragesItem build() {
			return new StoragesItem(this);
		}
	}
}