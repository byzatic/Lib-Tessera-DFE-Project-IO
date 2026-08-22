package io.github.byzatic.tessera.lib.configio.unified.spi.service;

/**
 * Supplies configuration-time metadata for one Tessera service.
 *
 * <p>Implementations must have a public no-argument constructor and must be registered under
 * {@code META-INF/services/io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptorProvider}.</p>
 */
public interface ServiceEditorDescriptorProvider {

    /**
     * Returns the complete editor declaration for the service.
     *
     * @return a non-null immutable declaration
     */
    ServiceEditorDescriptor getDescriptor();
}
