package io.github.byzatic.lib.configio.infrastructure.dto.global;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import io.github.byzatic.lib.configio.infrastructure.dto.general.StoragesItem;

public class Global{

	@SerializedName("storages")
	private List<StoragesItem> storages;

	@SerializedName("services")
	private List<ServicesItem> services;

	public Global() {
	}

	private Global(Builder builder) {
		storages = builder.storages;
		services = builder.services;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(Global copy) {
		Builder builder = new Builder();
		builder.storages = copy.getStorages();
		builder.services = copy.getServices();
		return builder;
	}

	public List<StoragesItem> getStorages(){
		return storages;
	}

	public List<ServicesItem> getServices(){
		return services;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Global global = (Global) o;
		return Objects.equals(storages, global.storages) && Objects.equals(services, global.services);
	}

	@Override
	public int hashCode() {
		return Objects.hash(storages, services);
	}

	@Override
	public String toString() {
		return "Global{" +
				"storages=" + storages +
				", services=" + services +
				'}';
	}

	/**
	 * {@code Global} builder static inner class.
	 */
	public static final class Builder {
		private List<StoragesItem> storages;
		private List<ServicesItem> services;

		private Builder() {
		}

		/**
		 * Sets the {@code storages} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param storages the {@code storages} to set
		 * @return a reference to this Builder
		 */
		public Builder setStorages(List<StoragesItem> storages) {
			this.storages = storages;
			return this;
		}

		/**
		 * Sets the {@code services} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param services the {@code services} to set
		 * @return a reference to this Builder
		 */
		public Builder setServices(List<ServicesItem> services) {
			this.services = services;
			return this;
		}

		/**
		 * Returns a {@code Global} built from the parameters previously set.
		 *
		 * @return a {@code Global} built with parameters of this {@code Global.Builder}
		 */
		public Global build() {
			return new Global(this);
		}
	}
}