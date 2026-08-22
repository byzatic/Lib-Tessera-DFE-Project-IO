package io.github.byzatic.lib.configio.service_spi;

/** Publishes editor metadata from a service JAR through Java SPI. */
public interface ServiceEditorDescriptorProvider {

    /** Returns the immutable descriptor of the service implemented by the JAR. */
    ServiceEditorDescriptor getDescriptor();
}
