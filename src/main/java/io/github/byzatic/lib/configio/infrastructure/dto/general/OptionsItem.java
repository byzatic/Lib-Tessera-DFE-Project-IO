package io.github.byzatic.lib.configio.infrastructure.dto.general;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class OptionsItem{

	@SerializedName("value")
	private String value;

	@SerializedName("key")
	private String key;

	@SerializedName("data")
	private String data;

	@SerializedName("name")
	private String name;

	public OptionsItem() {
	}

	private OptionsItem(Builder builder) {
		value = builder.value;
		key = builder.key;
		data = builder.data;
		name = builder.name;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(OptionsItem copy) {
		Builder builder = new Builder();
		builder.value = copy.getValue();
		builder.key = copy.getKey();
		builder.data = copy.getData();
		builder.name = copy.getName();
		return builder;
	}

	public String getValue(){
		return value;
	}

	public String getKey(){
		return key;
	}

	public String getData(){
		return data;
	}

	public String getName(){
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		OptionsItem that = (OptionsItem) o;
		return Objects.equals(value, that.value) && Objects.equals(key, that.key) && Objects.equals(data, that.data) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, key, data, name);
	}

	@Override
	public String toString() {
		return "OptionsItem{" +
				"value='" + value + '\'' +
				", key='" + key + '\'' +
				", data='" + data + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	/**
	 * {@code OptionsItem} builder static inner class.
	 */
	public static final class Builder {
		private String value;
		private String key;
		private String data;
		private String name;

		private Builder() {
		}

		/**
		 * Sets the {@code value} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param value the {@code value} to set
		 * @return a reference to this Builder
		 */
		public Builder setValue(String value) {
			this.value = value;
			return this;
		}

		/**
		 * Sets the {@code key} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param key the {@code key} to set
		 * @return a reference to this Builder
		 */
		public Builder setKey(String key) {
			this.key = key;
			return this;
		}

		/**
		 * Sets the {@code data} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param data the {@code data} to set
		 * @return a reference to this Builder
		 */
		public Builder setData(String data) {
			this.data = data;
			return this;
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
		 * Returns a {@code OptionsItem} built from the parameters previously set.
		 *
		 * @return a {@code OptionsItem} built with parameters of this {@code OptionsItem.Builder}
		 */
		public OptionsItem build() {
			return new OptionsItem(this);
		}
	}
}