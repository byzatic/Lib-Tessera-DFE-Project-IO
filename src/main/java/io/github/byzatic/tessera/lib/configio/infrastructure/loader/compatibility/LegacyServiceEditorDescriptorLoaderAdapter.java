package io.github.byzatic.tessera.lib.configio.infrastructure.loader.compatibility;

import io.github.byzatic.tessera.lib.configio.infrastructure.utils.LegacyEditorDescriptorMapper;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptor;

import java.util.List;
import java.util.ServiceLoader;

/** Loads legacy service SPI providers and adapts them to the unified contract. */
public final class LegacyServiceEditorDescriptorLoaderAdapter {

    private final LegacyEditorDescriptorMapper descriptorMapper;

    public LegacyServiceEditorDescriptorLoaderAdapter() {
        this.descriptorMapper = new LegacyEditorDescriptorMapper();
    }

    /** Returns immutable unified descriptors discovered through the legacy provider type. */
    public List<ServiceEditorDescriptor> load(ClassLoader classLoader) {
        return ServiceLoader.load(ServiceEditorDescriptorProvider.class, classLoader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(ServiceEditorDescriptorProvider::getDescriptor)
                .map(descriptorMapper::mapService)
                .toList();
    }
}
