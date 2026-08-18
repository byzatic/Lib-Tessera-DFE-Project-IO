package io.github.byzatic.lib.configio.infrastructure.dto.raw.global;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.general.OptionsItem;

public class ServicesItem{

	@SerializedName("options")
	private List<OptionsItem> options;

	@SerializedName("description")
	private String description;

	@SerializedName("id_name")
	private String idName;

	public ServicesItem() {
	}

	private ServicesItem(Builder builder) {
		options = builder.options;
		description = builder.description;
		idName = builder.idName;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(ServicesItem copy) {
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
		ServicesItem that = (ServicesItem) o;
		return Objects.equals(options, that.options) && Objects.equals(description, that.description) && Objects.equals(idName, that.idName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(options, description, idName);
	}

	@Override
	public String toString() {
		return "ServicesItem{" +
				"options=" + options +
				", description='" + description + '\'' +
				", idName='" + idName + '\'' +
				'}';
	}

	/**
	 * {@code ServicesItem} builder static inner class.
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
		 * Returns a {@code ServicesItem} built from the parameters previously set.
		 *
		 * @return a {@code ServicesItem} built with parameters of this {@code ServicesItem.Builder}
		 */
		public ServicesItem build() {
			return new ServicesItem(this);
		}
	}
}